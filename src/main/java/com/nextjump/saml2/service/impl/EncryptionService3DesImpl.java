package com.nextjump.saml2.service.impl;

import com.nextjump.saml2.service.EncryptionService;
import org.springframework.beans.factory.InitializingBean;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

//@Service
public class EncryptionService3DesImpl implements EncryptionService, InitializingBean {

    private final byte[] secretKey = "zztestzztestzztestzztest".getBytes();
    private final byte[] iv = "a76nb5h9".getBytes();
    private Cipher encryptCipher = null;

    @Override
    public String encrypt(String source) throws Exception {
        byte[] sourceBytes = source.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedBytes = encryptCipher.doFinal(sourceBytes);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "TripleDES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        encryptCipher = Cipher.getInstance("TripleDES/CBC/PKCS5Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
    }
}
