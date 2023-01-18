package com.nextjump.saml2.ext.storage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.opensaml.xml.XMLObject;
import org.springframework.security.saml.storage.SAMLMessageStorage;

import java.util.concurrent.TimeUnit;

public class MemorySAMLMessageStorage implements SAMLMessageStorage {
    private final Cache<String, XMLObject> data = CacheBuilder
            .newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    @Override
    public void storeMessage(String messageId, XMLObject message) {
        data.put(messageId, message);
    }

    @Override
    public XMLObject retrieveMessage(String messageID) {
        XMLObject result = data.getIfPresent(messageID);
        data.invalidate(messageID);
        return result;
    }
}
