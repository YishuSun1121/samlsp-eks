package com.nextjump.saml2.service.impl;

import com.google.common.base.Strings;
import com.nextjump.saml2.AppSettings;
import com.nextjump.saml2.service.MessagesService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MessagesServiceImpl implements MessagesService {
    private final static Log LOG = LogFactory.getLog(MessagesServiceImpl.class);

    @Autowired
    private AppSettings appSettings;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendSAMLSSOFailedNotification(String logId, String peerEntityId, String principal, String error) {
        if (Strings.isNullOrEmpty(appSettings.getMessagesServer())) {
            LOG.warn(String.format("messageServer is empty, skip send the notification"));
            return;
        }
        if (appSettings.getFailedMailReceivers().isEmpty()) {
            LOG.warn(String.format("failedMailReceivers is empty, skip send the notification"));
            return;
        }

        String toArrayStr = "[";
        for (String receiver : appSettings.getFailedMailReceivers()) {
            toArrayStr += "\"" + receiver + "\",";
        }
        toArrayStr = toArrayStr.substring(0, toArrayStr.length() - 1);
        toArrayStr += "]";

        String body = String.format("{\"type\":\"EMAIL\",\"subject\":\"%s\",\"content\":\"%s\",\"tos\":%s}",
                "SAML SSO Failed for " + peerEntityId + " at " + DateTime.now(),
                String.format("<p>Log id: %s<p><p>Idp entityId: %s<p><p>Principal: %s<p><p>Error: " +
                                "<pre>%s</pre></p><p>note: can " +
                                "using the" +
                                " log id to get more detail</p>", logId, peerEntityId,
                        principal, StringEscapeUtils.escapeJson(error)),
                toArrayStr
        );
        LOG.debug(body);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            headers.add("Accept", "*/*");
            HttpEntity<String> requestEntity = new HttpEntity<String>(body, headers);
            ResponseEntity<String> resp = restTemplate.postForEntity(appSettings.getMessagesServer(), requestEntity,
                    String.class);
            LOG.info(resp.getBody());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
