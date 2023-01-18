package com.nextjump.saml2.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nextjump.saml2.AppSettings;
import com.nextjump.saml2.enums.MetadataSource;
import com.nextjump.saml2.exception.AppExceptionCode;
import com.nextjump.saml2.exception.AppValidationException;
import com.nextjump.saml2.ext.metadata.ModelMetadataProvider;
import com.nextjump.saml2.model.SAMLIdentityProvider;
import com.nextjump.saml2.repository.AbstractRepository;
import com.nextjump.saml2.repository.SAMLIdentityProviderRepository;
import com.nextjump.saml2.service.SAMLIdentityProviderService;
import com.nextjump.saml2.service.request.SAMLIdentityProviderCreateRequest;
import com.nextjump.saml2.service.request.SAMLIdentityProviderQueryRequest;
import com.nextjump.saml2.view.SAMLIdentityProviderDetail;
import com.nextjump.saml2.view.SAMLIdentityProviderView;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.metadata.provider.HTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.ParserPool;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.ExtendedMetadataDelegate;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SAMLIdentityProviderServiceImpl extends AbstractBaseService<SAMLIdentityProvider> implements SAMLIdentityProviderService {
    private final static Log LOG = LogFactory.getLog(SAMLIdentityProviderServiceImpl.class);
    private final ExecutorService signalExecutor = Executors.newSingleThreadExecutor();
    private final Map<String, MetadataProvider> metadataProviders = Maps.newLinkedHashMap();
    @Autowired
    private SAMLIdentityProviderRepository samlIdentityProviderRepository;
    @Autowired
    @Lazy
    private HttpClient httpClient;
    @Resource(name = "backgroundTaskTimer")
    private Timer backgroundTaskTimer;
    @Autowired
    private ParserPool parserPool;
    @Autowired
    private AppSettings appSettings;
    @Autowired
    @Lazy
    private MetadataManager metadataManager;
    @Autowired
    @Lazy
    private com.nextjump.saml2.ext.key.JKSKeyManager keyManager;

    @Override
    @Transactional(readOnly = true)
    public Page<SAMLIdentityProviderView> queryPage(SAMLIdentityProviderQueryRequest param) {
        return samlIdentityProviderRepository.findAll(new Specification<SAMLIdentityProvider>() {
            @Override
            public Predicate toPredicate(Root<SAMLIdentityProvider> root, CriteriaQuery<?> query,
                                         CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicateList = Lists.newArrayList();

                return criteriaBuilder.and(predicateList.toArray(new Predicate[]{}));
            }
        }, param.resolvePageable()).map(SAMLIdentityProviderView::from);
    }

    @Override
    @Transactional
    public SAMLIdentityProviderDetail create(SAMLIdentityProviderCreateRequest param) {
        validate(param);
        if (!Strings.isNullOrEmpty(param.getEntityId())) {
            SAMLIdentityProvider sameEntityId = samlIdentityProviderRepository.findByEntityId(param.getEntityId());
            if (null != sameEntityId) {
                throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, String.format("entityId %s existing",
                        param.getAlias()));
            }
        }
        SAMLIdentityProvider sameAlias = samlIdentityProviderRepository.findByAlias(param.getAlias());
        if (null != sameAlias) {
            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, String.format("alias %s existing",
                    param.getAlias()));
        }
        SAMLIdentityProvider model = new SAMLIdentityProvider();
        BeanUtils.copyProperties(param, model);
        handleMetadataContent(param, model);
        final SAMLIdentityProvider afterSave = samlIdentityProviderRepository.saveAndFlush(model);
        final SAMLIdentityProviderDetail result =
                SAMLIdentityProviderDetail.from(afterSave);
        signalExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    metadataManager.addMetadataProvider(generateMetadata(result));
                    metadataManager.refreshMetadata();
                } catch (MetadataProviderException e) {
                    LOG.warn("add new metadata to metadataManager failed", e);
                }
            }
        });
        return result;
    }

    @Override
    public ExtendedMetadataDelegate generateMetadata(SAMLIdentityProviderDetail view) throws MetadataProviderException {
        if (metadataProviders.containsKey(view.getId())) {
            return (ExtendedMetadataDelegate) metadataProviders.get(view.getId());
        }
        MetadataProvider metadataProvider = null;
        switch (view.getMetadataSource()) {
            case HTTP:
                HTTPMetadataProvider httpMetadataProvider = new HTTPMetadataProvider(
                        this.backgroundTaskTimer, httpClient, view.getMetadataUrl());
                httpMetadataProvider.setParserPool(parserPool);
                metadataProvider = httpMetadataProvider;
                break;
//            case FILE:
//                FilesystemMetadataProvider filesystemMetadataProvider = new FilesystemMetadataProvider(
//                        this.backgroundTaskTimer, new File(view.getMetadataUrl())
//                );
//                filesystemMetadataProvider.setParserPool(parserPool);
//                metadataProvider = filesystemMetadataProvider;
//                break;
            case XML:
            case ATTRIBUTE:
                ModelMetadataProvider modelMetadataProvider = new ModelMetadataProvider(
                        this.backgroundTaskTimer, view.getId(), samlIdentityProviderRepository
                );
//                FilesystemMetadataProvider xmlContentMetadataProvider = new FilesystemMetadataProvider(
//                        this.backgroundTaskTimer, new File(model.getMetadataUrl())
//                );
                modelMetadataProvider.setParserPool(parserPool);
                metadataProvider = modelMetadataProvider;
                break;
        }

        ExtendedMetadata extendedMetadata = new ExtendedMetadata();
        extendedMetadata.setIdpDiscoveryEnabled(true);
        extendedMetadata.setSigningAlgorithm("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
        extendedMetadata.setSignMetadata(true);
        extendedMetadata.setEcpEnabled(true);
        extendedMetadata.setAlias(view.getAlias());
        if (!Strings.isNullOrEmpty(view.getOldSignX509Certificate())) {
            String oldSignKeyName = keyManager.generateExtraCertificateKey(view.getAlias());
            extendedMetadata.setSigningKey(oldSignKeyName);
            try {
                keyManager.addExtraCertificate(oldSignKeyName,
                        new ByteArrayInputStream(Base64.getDecoder().decode(view.getOldSignX509Certificate())),
                        view.getOldValidBefore());
            } catch (CertificateException e) {
                e.printStackTrace();
                throw new MetadataProviderException("Can't create extra certificate for " + oldSignKeyName);
            }
        }

        ExtendedMetadataDelegate extendedMetadataDelegate =
                new ExtendedMetadataDelegate(metadataProvider, extendedMetadata);
        extendedMetadataDelegate.setMetadataTrustCheck(false);
        extendedMetadataDelegate.setMetadataRequireSignature(false);
        backgroundTaskTimer.purge();
        metadataProviders.put(view.getId(), extendedMetadataDelegate);
        return extendedMetadataDelegate;
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        SAMLIdentityProvider model = requiredModelById(id);
        try {
            metadataManager.removeMetadataProvider(metadataProviders.get(id));
            metadataManager.refreshMetadata();
        } catch (Exception ex) {
            LOG.warn("remove from metadata manager failed", ex);
        }
        metadataProviders.remove(id);
        samlIdentityProviderRepository.delete(model);
    }

    @Override
    @Transactional(readOnly = true)
    public SAMLIdentityProviderView queryOneByAlias(String alias) {
        return SAMLIdentityProviderView.from(samlIdentityProviderRepository.findByAlias(alias));
    }

    @Override
    @Transactional
    public SAMLIdentityProviderDetail update(String id, SAMLIdentityProviderCreateRequest param) {
        validate(param);
        SAMLIdentityProvider model = requiredModelById(id);
        if (!Strings.isNullOrEmpty(param.getEntityId())) {
            SAMLIdentityProvider sameEntityId = samlIdentityProviderRepository.findByEntityId(param.getEntityId());
            if (null != sameEntityId && !sameEntityId.getId().equals(model.getId())) {
                throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, String.format("entityId %s existing",
                        param.getAlias()));
            }
        }

        SAMLIdentityProvider sameAlias = samlIdentityProviderRepository.findByAlias(param.getAlias());
        if (null != sameAlias && !sameAlias.getId().equals(model.getId())) {
            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, String.format("alias %s existing",
                    param.getAlias()));
        }

        // remove it from metadata manager first
        try {
            metadataManager.removeMetadataProvider(metadataProviders.get(id));
        } catch (Exception ex) {
            LOG.warn("remove from metadata manager failed", ex);
        }
        metadataProviders.remove(id);
        BeanUtils.copyProperties(param, model);
        handleMetadataContent(param, model);
        final SAMLIdentityProvider afterSave = samlIdentityProviderRepository.saveAndFlush(model);
        final SAMLIdentityProviderDetail result =
                SAMLIdentityProviderDetail.from(afterSave);
        signalExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final ExtendedMetadataDelegate metadata =
                            generateMetadata(result);
                    metadataManager.addMetadataProvider(metadata);
                    metadataManager.refreshMetadata();
                    metadataManager.getObservers().forEach(observer -> {
                        observer.onEvent(metadata);
                    });
                } catch (MetadataProviderException e) {
                    LOG.warn("add new metadata to metadataManager failed", e);
                }
            }
        });
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public SAMLIdentityProviderDetail getOne(String id) {
        SAMLIdentityProviderDetail view = SAMLIdentityProviderDetail.from(requiredModelById(id));
        return view;
    }

    private void handleMetadataContent(SAMLIdentityProviderCreateRequest param, SAMLIdentityProvider model) {
//        if (param.getMetadataSource() == MetadataSource.XML) {
//            try {
//                String filePath = appSettings.getXmlMetadataFolder() + "/" + param.getAlias() + ".xml";
//                new File(filePath).getParentFile().mkdirs();
//                Files.write(param.getMetadataXmlContent().getBytes(StandardCharsets.UTF_8),
//                        new File(filePath));
//                model.setMetadataUrl(filePath);
//            } catch (IOException e) {
//                LOG.warn("write metadata xml content failed", e);
//                throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, "write metadata xml content
//                failed");
//            }
//        } else
        if (param.getMetadataSource() == MetadataSource.ATTRIBUTE) {
            validate4MetadataSourceAttribute(param);
            model.setMetadataXmlContent(generateMetadataXml(model));
        }
    }

    @Override
    AbstractRepository getRepository() {
        return samlIdentityProviderRepository;
    }

    private void validate(SAMLIdentityProviderCreateRequest param) {
        if (Strings.isNullOrEmpty(param.getAlias())) {
            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, "empty alias not allowed");
        }
        if (null == param.getMetadataSource()) {
            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, "null metadataSource not allowed");
        }
        if (param.getMetadataSource() == MetadataSource.HTTP && Strings.isNullOrEmpty(param.getMetadataUrl())) {
            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, "empty metadataUrl for HTTP not allowed");
        }
        if (param.getMetadataSource() == MetadataSource.XML && Strings.isNullOrEmpty(param.getMetadataXmlContent())) {
            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, "empty metadataXmlContent for XML not " +
                    "allowed");
        }
    }

    private void validate4MetadataSourceAttribute(SAMLIdentityProviderCreateRequest param) {
        if (Strings.isNullOrEmpty(param.getEntityId())) {
            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, "empty entityId not allowed");
        }
        if (Strings.isNullOrEmpty(param.getSignX509Certificate())) {
            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, "empty signX509Certificate not allowed");
        }
//        if (Strings.isNullOrEmpty(param.getSsoUrl())) {
//            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, "empty ssoUrl not allowed");
//        }
//        if (null == param.getSsoBinding()) {
//            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, "null ssoBinding not allowed");
//        }
    }

    private String generateMetadataXml(SAMLIdentityProvider model) {
        if (MetadataSource.ATTRIBUTE != model.getMetadataSource()) {
            throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, "only support generate metadata xml for" +
                    " MetadataSource Attribute");
        }

        StringBuffer result = new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        result.append("<EntityDescriptor entityID=\"").append(model.getEntityId()).append("\" xmlns=\"urn:oasis:names" +
                ":tc:SAML:2.0:metadata\">");
        result.append("<IDPSSODescriptor WantAuthnRequestsSigned=\"false\" " +
                "protocolSupportEnumeration=\"urn:oasis:names:tc:SAML:2.0:protocol\">\n" +
                "        <KeyDescriptor use=\"signing\">\n" +
                "            <ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">\n" +
                "                <ds:X509Data>\n" +
                "                    <ds:X509Certificate>");
        result.append(model.getSignX509Certificate()).append("\n");
        result.append("                   </ds:X509Certificate>\n" +
                "                </ds:X509Data>\n" +
                "            </ds:KeyInfo>\n" +
                "        </KeyDescriptor>");
        if (!Strings.isNullOrEmpty(model.getEncryptionX509Certificate())) {
            result.append("        <KeyDescriptor use=\"encryption\">\n" +
                    "            <ds:KeyInfo xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">\n" +
                    "                <ds:X509Data>\n" +
                    "                    <ds:X509Certificate>");
            result.append(model.getEncryptionX509Certificate()).append("\n");
            result.append("                    </ds:X509Certificate>\n" +
                    "                </ds:X509Data>\n" +
                    "            </ds:KeyInfo>\n" +
                    "            <EncryptionMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#aes128-cbc\">\n" +
                    "                <xenc:KeySize xmlns:xenc=\"http://www.w3" +
                    ".org/2001/04/xmlenc#\">128</xenc:KeySize>\n" +
                    "</EncryptionMethod>\n" +
                    "        </KeyDescriptor>");
        }
        if (!Strings.isNullOrEmpty(model.getSsoUrl())) {
            switch (model.getSsoBinding()) {
                case HTTPRedirect:
                    result.append("<SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2" +
                            ".0:bindings:HTTP-Redirect\" " +
                            "Location=\"").append(model.getSsoUrl()).append("\"/>");
                    break;
                case HTTPPost:
                    result.append("<SingleSignOnService Binding=\"urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST\" " +
                            "Location=\"").append(model.getSsoUrl()).append("\"/>");
                    break;
            }
        }
        result.append("</IDPSSODescriptor>");

        result.append("</EntityDescriptor>");
        return result.toString();
    }

}
