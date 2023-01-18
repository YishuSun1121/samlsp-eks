package com.nextjump.saml2.controllers;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.nextjump.saml2.AppSettings;
import com.nextjump.saml2.exception.AppExceptionCode;
import com.nextjump.saml2.exception.AppValidationException;
import com.nextjump.saml2.service.EncryptionService;
import com.nextjump.saml2.service.SAMLIdentityProviderService;
import com.nextjump.saml2.service.UserService;
import com.nextjump.saml2.stereotypes.CurrentUser;
import com.nextjump.saml2.view.SAMLIdentityProviderView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
public class LandingController {
    private static final Logger LOG = LoggerFactory
            .getLogger(LandingController.class);
    @Autowired
    private EncryptionService encryptionService;
    @Autowired
    private UserService userService;
    @Autowired
    private SAMLIdentityProviderService samlIdentityProviderService;
    @Autowired
    private MetadataManager metadata;
    @Autowired
    private AppSettings appSettings;

    @RequestMapping("/landing")
    public String landing(@CurrentUser User user, Model model) throws Exception {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null)
                LOG.debug("Current authentication instance from security context is null");
            else
                LOG.debug("Current authentication instance from security context: "
                        + this.getClass().getSimpleName());
            if (null != user) {
                SAMLCredential samlCredential = (SAMLCredential) auth.getCredentials();

                String idpEntityId = samlCredential.getRemoteEntityID();
                ExtendedMetadata extendedMetadata = metadata.getExtendedMetadata(idpEntityId);
                String alias = extendedMetadata.getAlias();
                SAMLIdentityProviderView samlIdentityProvider = samlIdentityProviderService.queryOneByAlias(alias);

                String ecid = samlCredential.getAttributeAsString("ecid");
                String orgdir;
                if (!Strings.isNullOrEmpty(samlIdentityProvider.getOrgDirVal())) {
                    orgdir = samlIdentityProvider.getOrgDirVal();
                } else {
                    orgdir = samlCredential.getAttributeAsString("orgdir");
                }
                if (Strings.isNullOrEmpty(ecid) && Strings.isNullOrEmpty(orgdir)) {
                    //using nameId
                    String nameID = samlCredential.getNameID().getValue();
                    //expected format: orgdir=XX;ecid=XX
                    String[] attrArray = nameID.split(";");
                    if (attrArray.length != 2) {
                        throw new AppValidationException(AppExceptionCode.INVALID_PARAMS, String.format(
                                "Wrong nameID format %s", nameID
                        ));
                    }

                    orgdir = attrArray[0].split("=")[1];
                    ecid = attrArray[1].split("=")[1];
                }
                String authenticationStr = String.format("orgdir=%s;ecid=%s", orgdir, ecid);
                String countrycode = samlCredential.getAttributeAsString("countrycode");
                if (!Strings.isNullOrEmpty(countrycode)) {
                    authenticationStr += ";countrycode=" + countrycode;
                }

                //  Get all attributes from SAML Credential
//            List<Attribute> attributeList = samlCredential.getAttributes();
//            StringBuilder allAttributes = new StringBuilder();
//            for (org.opensaml.saml2.core.Attribute attribute : attributeList) {
//                String attrbuteName = attribute.getName();
//                for (org.opensaml.xml.XMLObject attributeValue : attribute.getAttributeValues()) {
//                    String value = attributeValue.getDOM().getTextContent();
//                    allAttributes.append(attrbuteName).append("=").append(value);
//                }
//            }
//            LOG.info("passed attributes" + allAttributes);

                // Get extra attributes
                Map<String, String> extraAttrs = Maps.newHashMap();
                for (int i = 0; i < appSettings.getSamlExtraAttributePaths().size(); i++) {
                    String path = appSettings.getSamlExtraAttributePaths().get(i);
                    String name = appSettings.getSamlExtraAttributeNames().get(i);

                    String value = samlCredential.getAttributeAsString(path);
                    if (Strings.isNullOrEmpty(value)) {
                        continue;
                    }
                    extraAttrs.put(name, value);
                }
                String extraAttrsStr = Joiner.on(";").withKeyValueSeparator("=").join(extraAttrs);
                if (!Strings.isNullOrEmpty(extraAttrsStr)) {
                    authenticationStr += ";" + extraAttrsStr;
                }

                LOG.info("authenticationStr=" + authenticationStr);

                model.addAttribute("username", authenticationStr);
                model.addAttribute("encData", encryptionService.encrypt(
                        authenticationStr
                ));
                model.addAttribute("idpDebugging", samlIdentityProvider.isDebugging());
            }
        } catch (Exception ex) {
            LOG.error("", ex);
            throw ex;
        }
        return "pages/landing";
    }

}
