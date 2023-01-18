package com.nextjump.saml2.ext.storage;

import org.springframework.security.saml.storage.SAMLMessageStorage;
import org.springframework.security.saml.storage.SAMLMessageStorageFactory;

import javax.servlet.http.HttpServletRequest;

public class MemorySAMLStorageFactory implements SAMLMessageStorageFactory {
    private final MemorySAMLMessageStorage storage = new MemorySAMLMessageStorage();

    @Override
    public SAMLMessageStorage getMessageStorage(HttpServletRequest request) {
        return storage;
    }
}
