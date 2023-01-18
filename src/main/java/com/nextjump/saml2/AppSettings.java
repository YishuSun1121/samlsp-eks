package com.nextjump.saml2;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.opensaml.saml2.metadata.SPSSODescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@ConfigurationProperties(value = "com.nextjump.saml2")
@Component
public class AppSettings implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(AppSettings.class);

    private String keystorePath = "classpath:/saml/samlKeystore.jks";
    private String keystorePass = "nalle123";
    private String keystoreKey = "apollo";
    private String keystoreKeyPass = "nalle123";
    private String spEntityId = "nextjump:saml2:sp";
    private String spAlias = "sp";
    private String spEntityBaseUrl;
    //    private String spMetadataFileName = "nextjump_saml2_sp.xml";
    private boolean enableLB = false;
    private String lbSchema;
    private String lbServerName;
    private String lbServerContextPath = "";
    //    private String xmlMetadataFolder = "./metadata";
    private List<String> samlExtraAttributePaths = Lists.newArrayList();
    private List<String> samlExtraAttributeNames = Lists.newArrayList();
    private String basicAuthnUser = "nextjump";
    private String basicAuthnPass = "nextjump@123456";

    private List<String> extraSpEntityIds = Lists.newArrayList();
    private List<String> extraSpAliases = Lists.newArrayList();
    private List<String> extraSpKeystoreKeys = Lists.newArrayList();
    private List<String> extraSpKeystoreKeyPasses = Lists.newArrayList();
    private List<String> extraSpEntityBaseUrls = Lists.newArrayList();
    private List<String> extraSpSsoPaths = Lists.newArrayList(); // /saml2.0/module.php/saml/sp/saml2-acs.php/nxj-sp
    private List<String> extraSpSloPaths = Lists.newArrayList(); // Logout paths
//    private List<String> extraSpLbServerNames = Lists.newArrayList();

    private String messagesServer;
    private List<String> failedMailReceivers = Lists.newArrayList();
    private boolean ignoreLogoutFailed = true;

    public String getLbServerContextPath() {
        return lbServerContextPath;
    }

    public void setLbServerContextPath(String lbServerContextPath) {
        this.lbServerContextPath = lbServerContextPath;
    }

    public boolean isEnableLB() {
        return enableLB;
    }

    public void setEnableLB(boolean enableLB) {
        this.enableLB = enableLB;
    }

    public String getLbSchema() {
        return lbSchema;
    }

    public void setLbSchema(String lbSchema) {
        this.lbSchema = lbSchema;
    }

    public String getLbServerName() {
        return lbServerName;
    }

    public void setLbServerName(String lbServerName) {
        this.lbServerName = lbServerName;
    }

    /**
     * replace the server name of initialUri with request server name.
     *
     * @param request
     * @param initialUri
     * @return
     */
    public String getUrlFromLb(HttpServletRequest request, String initialUri) {
        String serverName = getServerNameFromLb(request);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(initialUri);
        String modifiedUri = builder.host(serverName).toUriString();
        return modifiedUri;
    }

    /**
     * get server name from http request.
     *
     * @param request
     * @return
     */
    public String getServerNameFromLb(HttpServletRequest request) {
//        String serverName = request.getServerName();
//        return serverName;

        // try resolve the local entity id from request
        String requestURI = request.getRequestURI();
        String localEntityId = null;
        for (int i = 0; i < getExtraSpSsoPaths().size(); i++) {
            String extraSpSsoPath = getExtraSpSsoPaths().get(i);
            if (!Strings.isNullOrEmpty(extraSpSsoPath)) {
                if (requestURI.indexOf(extraSpSsoPath) > -1) {
                    localEntityId = getExtraSpEntityIds().get(i);
                    break;
                }
            }
        }
        if (null == localEntityId) {
            for (int i = 0; i < getExtraSpSloPaths().size(); i++) {
                String extraSpSloPath = getExtraSpSloPaths().get(i);
                if (!Strings.isNullOrEmpty(extraSpSloPath)) {
                    if (requestURI.indexOf(extraSpSloPath) > -1) {
                        localEntityId = getExtraSpEntityIds().get(i);
                        break;
                    }
                }
            }
        }

        if (null == localEntityId) {
            // from alias
            int filterIndex = requestURI.indexOf("/alias/");
            if (filterIndex != -1) { // EntityId from URL alias
                String localAlias = requestURI.substring(filterIndex + 7);
                if (localAlias.equals(getSpAlias())) {
                    localEntityId = getSpEntityId();
                } else {
                    int index = getExtraSpAliases().indexOf(localAlias);
                    if (index > -1) {
                        localEntityId = getExtraSpEntityIds().get(index);
                    }
                }
            }
        }
        if (null == localEntityId || localEntityId.equals(getSpEntityId())) {
            // not found any local sp or it's the default sp
            return getLbServerName();
        } else {
            int index = getExtraSpEntityIds().indexOf(localEntityId);
            if (index > -1 && !Strings.isNullOrEmpty(getExtraSpEntityBaseUrls().get(index))) {
                return getExtraSpEntityBaseUrls().get(index).split("//")[1];
            } else {
                return getLbServerName();
            }
        }
    }

//    public String getSpMetadataFileName() {
//        return spMetadataFileName;
//    }
//
//    public void setSpMetadataFileName(String spMetadataFileName) {
//        this.spMetadataFileName = spMetadataFileName;
//    }

    public String getSpEntityId() {
        return spEntityId;
    }

    public void setSpEntityId(String spEntityId) {
        this.spEntityId = spEntityId;
    }

    public String getKeystoreKey() {
        return keystoreKey;
    }

    public void setKeystoreKey(String keystoreKey) {
        this.keystoreKey = keystoreKey;
    }

    public String getKeystoreKeyPass() {
        return keystoreKeyPass;
    }

    public void setKeystoreKeyPass(String keystoreKeyPass) {
        this.keystoreKeyPass = keystoreKeyPass;
    }

    public String getKeystorePass() {
        return keystorePass;
    }

    public void setKeystorePass(String keystorePass) {
        this.keystorePass = keystorePass;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public String getSpEntityBaseUrl() {
        return spEntityBaseUrl;
    }

    public void setSpEntityBaseUrl(String spEntityBaseUrl) {
        this.spEntityBaseUrl = spEntityBaseUrl;
    }

    public List<String> getSamlExtraAttributePaths() {
        return samlExtraAttributePaths;
    }

    public void setSamlExtraAttributePaths(List<String> samlExtraAttributePaths) {
        this.samlExtraAttributePaths = samlExtraAttributePaths;
    }

    public List<String> getSamlExtraAttributeNames() {
        return samlExtraAttributeNames;
    }

    public void setSamlExtraAttributeNames(List<String> samlExtraAttributeNames) {
        this.samlExtraAttributeNames = samlExtraAttributeNames;
    }

    public String getBasicAuthnUser() {
        return basicAuthnUser;
    }

    public void setBasicAuthnUser(String basicAuthnUser) {
        this.basicAuthnUser = basicAuthnUser;
    }

    public String getBasicAuthnPass() {
        return basicAuthnPass;
    }

    public void setBasicAuthnPass(String basicAuthnPass) {
        this.basicAuthnPass = basicAuthnPass;
    }

    public List<String> getExtraSpEntityIds() {
        return extraSpEntityIds;
    }

    public void setExtraSpEntityIds(List<String> extraSpEntityIds) {
        this.extraSpEntityIds = extraSpEntityIds;
    }

    public List<String> getExtraSpAliases() {
        return extraSpAliases;
    }

    public void setExtraSpAliases(List<String> extraSpAliases) {
        this.extraSpAliases = extraSpAliases;
    }

    public List<String> getExtraSpKeystoreKeys() {
        return extraSpKeystoreKeys;
    }

    public void setExtraSpKeystoreKeys(List<String> extraSpKeystoreKeys) {
        this.extraSpKeystoreKeys = extraSpKeystoreKeys;
    }

    public List<String> getExtraSpKeystoreKeyPasses() {
        return extraSpKeystoreKeyPasses;
    }

    public void setExtraSpKeystoreKeyPasses(List<String> extraSpKeystoreKeyPasses) {
        this.extraSpKeystoreKeyPasses = extraSpKeystoreKeyPasses;
    }

    public List<String> getExtraSpEntityBaseUrls() {
        return extraSpEntityBaseUrls;
    }

    public void setExtraSpEntityBaseUrls(List<String> extraSpEntityBaseUrls) {
        this.extraSpEntityBaseUrls = extraSpEntityBaseUrls;
    }

    public String getSpAlias() {
        return spAlias;
    }

    public void setSpAlias(String spAlias) {
        this.spAlias = spAlias;
    }

    public List<String> getExtraSpSsoPaths() {
        return extraSpSsoPaths;
    }

    public void setExtraSpSsoPaths(List<String> extraSpSsoPaths) {
        this.extraSpSsoPaths = extraSpSsoPaths;
    }

    public List<String> getExtraSpSloPaths() {
        return extraSpSloPaths;
    }

    public void setExtraSpSloPaths(List<String> extraSpSloPaths) {
        this.extraSpSloPaths = extraSpSloPaths;
    }

    public String getMessagesServer() {
        return messagesServer;
    }

    public void setMessagesServer(String messagesServer) {
        this.messagesServer = messagesServer;
    }

    public List<String> getFailedMailReceivers() {
        return failedMailReceivers;
    }

    public void setFailedMailReceivers(List<String> failedMailReceivers) {
        this.failedMailReceivers = failedMailReceivers;
    }

    public boolean isIgnoreLogoutFailed() {
        return ignoreLogoutFailed;
    }

    public void setIgnoreLogoutFailed(boolean ignoreLogoutFailed) {
        this.ignoreLogoutFailed = ignoreLogoutFailed;
    }

//    public List<String> getExtraSpLbServerNames() {
//        return extraSpLbServerNames;
//    }
//
//    public void setExtraSpLbServerNames(List<String> extraSpLbServerNames) {
//        this.extraSpLbServerNames = extraSpLbServerNames;
//    }

    public String resolveEntityIdFromSSOUrl(String url) {
        String entityId = null;
        for (int i = 0; i < getExtraSpSsoPaths().size(); i++) {
            String sso = getExtraSpSsoPaths().get(i);
            if (url.indexOf(sso) > -1) {
                entityId = getExtraSpEntityIds().get(i);
                break;
            }
        }
        return entityId;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (getSamlExtraAttributeNames().size() != getSamlExtraAttributePaths().size()) {
            throw new IllegalArgumentException("samlExtraAttributePaths' length not match samlExtraAttributeNames " +
                    "length");
        }
        LOG.info("samlExtraAttributePaths=" + String.join(",", samlExtraAttributePaths));
        LOG.info("samlExtraAttributeNames=" + String.join(",", samlExtraAttributeNames));

        if (!getExtraSpEntityIds().isEmpty()) {
            if (getExtraSpEntityIds().size() != getExtraSpAliases().size()
                    || getExtraSpEntityIds().size() != getExtraSpKeystoreKeys().size()
                    || getExtraSpEntityIds().size() != getExtraSpKeystoreKeyPasses().size()
                    || getExtraSpEntityIds().size() != getExtraSpEntityBaseUrls().size()) {
                throw new IllegalArgumentException("extraSpEntityIds, extraSpAliases, extraSpKeystoreKeys," +
                        "extraSpKeystoreKeyPasses, extraSpEntityBaseUrls should have same size");
            }
        }
    }
}
