package com.nextjump.saml2.service.impl;

import com.nextjump.saml2.AppSettings;
import com.nextjump.saml2.service.EncryptionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.saml.key.JKSKeyManager;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;

@Service
public class EncryptionServiceSignImpl implements EncryptionService, InitializingBean {
    private final static Log LOG = LogFactory.getLog(EncryptionServiceSignImpl.class);
    @Autowired
    private JKSKeyManager keyManager;
    @Autowired
    private AppSettings appSettings;
    @Value("classpath:saml/apollo.pkcs8")
    private Resource privateKeyResource;
    private Signature signature;

    @Override
    public String encrypt(String source) throws Exception {
        byte[] sourceBytes = source.getBytes(StandardCharsets.UTF_8);
        signature.update(sourceBytes);
        byte[] signBytes = signature.sign();
        String signStr = Base64.getEncoder().encodeToString(signBytes);
        String result = source + ";signature=" + signStr;
        LOG.info("After encrypt: " + result);
        return Base64.getEncoder().encodeToString(result.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        signature = Signature.getInstance("SHA1withRSA");
        signature.initSign((RSAPrivateKey) keyManager.getKeyStore().getKey(appSettings.getKeystoreKey(),
                appSettings.getKeystoreKeyPass().toCharArray()));

//        byte[] privateKeyBytes = ByteStreams.toByteArray(privateKeyResource.getInputStream());
//        String privatKeyStr = new String(privateKeyBytes);
//        privatKeyStr = privatKeyStr.replaceAll("-----BEGIN PRIVATE KEY-----", "");
//        privatKeyStr = privatKeyStr.replaceAll("-----END PRIVATE KEY-----", "");
//        privatKeyStr = privatKeyStr.replaceAll("\\n", "");
//        privateKeyBytes = Base64.getDecoder().decode(privatKeyStr);
//        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
//        KeyFactory fac = KeyFactory.getInstance("RSA");
//        RSAPrivateKey privateKey = (RSAPrivateKey) fac.generatePrivate(keySpec);
//        signature.initSign(privateKey);
    }
}
