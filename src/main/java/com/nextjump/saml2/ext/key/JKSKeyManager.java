package com.nextjump.saml2.ext.key;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;

public class JKSKeyManager extends org.springframework.security.saml.key.JKSKeyManager {
    private final static Log LOG = LogFactory.getLog(JKSKeyManager.class);

    private final static String IDP_OLD_CERT_PREFIX = "idp_old_";

    private final Map<String, BasicX509Credential> extraCredentials = new LinkedHashMap<>();
    private final Map<String, Long> extraCredentialValidBeforeTimes = new LinkedHashMap<>();
    private final CertificateFactory cf;

    public JKSKeyManager(KeyStore keyStore, Map<String, String> passwords, String defaultKey) {
        super(keyStore, passwords, defaultKey);
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            e.printStackTrace();
            throw new RuntimeException("Initialize X509 CertificateFactory failed", e);
        }
    }

    public JKSKeyManager(Resource storeFile, String storePass, Map<String, String> passwords, String defaultKey) {
        super(storeFile, storePass, passwords, defaultKey);
        try {
            cf = CertificateFactory.getInstance("X.509");
        } catch (CertificateException e) {
            e.printStackTrace();
            throw new RuntimeException("Initialize X509 CertificateFactory failed", e);
        }
    }

    @Override
    public Credential resolveSingle(CriteriaSet criteriaSet) throws SecurityException {
        EntityIDCriteria entityIDCriteria = criteriaSet.get(EntityIDCriteria.class);
        if (null != entityIDCriteria && entityIDCriteria.getEntityID().startsWith(IDP_OLD_CERT_PREFIX)) {
            LOG.info(String.format("resolve extra certificate for %s", entityIDCriteria.getEntityID()));
            if (extraCredentialValidBeforeTimes.containsKey(entityIDCriteria.getEntityID())) {
                Long validBefore = extraCredentialValidBeforeTimes.get(entityIDCriteria.getEntityID());
                if (validBefore < System.currentTimeMillis()) {
                    return null;
                }
            }
            return extraCredentials.get(entityIDCriteria.getEntityID());
        }
        return super.resolveSingle(criteriaSet);
    }

    public void addExtraCertificate(String key, InputStream inStream, Long validBefore) throws CertificateException {
        LOG.info(String.format("added extra certificate for %s", key));
        X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
        BasicX509Credential credential = new BasicX509Credential();
        credential.setEntityCertificate(cert);
        extraCredentials.put(key, credential);
        if (null != validBefore) {
            extraCredentialValidBeforeTimes.put(key, validBefore);
        }
    }

    public String generateExtraCertificateKey(String alias) {
        return IDP_OLD_CERT_PREFIX + alias;
    }
}
