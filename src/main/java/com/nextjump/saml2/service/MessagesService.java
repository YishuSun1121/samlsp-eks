package com.nextjump.saml2.service;

public interface MessagesService {

    void sendSAMLSSOFailedNotification(String logId, String peerEntityId, String principal, String error);
}
