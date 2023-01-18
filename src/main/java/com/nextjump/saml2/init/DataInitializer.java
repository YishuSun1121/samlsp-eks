package com.nextjump.saml2.init;

import com.nextjump.saml2.repository.SAMLIdentityProviderRepository;
import com.nextjump.saml2.service.EncryptionService;
import com.nextjump.saml2.service.MessagesService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {
    private final static Log LOG = LogFactory.getLog(DataInitializer.class);
    @Autowired
    private SAMLIdentityProviderRepository samlIdentityProviderRepository;
    @Autowired
    private EncryptionService encryptionService;
    @Autowired
    private MessagesService messagesService;


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
//        String ssocircleAlias = "ssocircle";
//        SAMLIdentityProvider ssocircle = samlIdentityProviderRepository.findByAlias(ssocircleAlias);
//        if (null == ssocircle) {
//            ssocircle = new SAMLIdentityProvider();
//            ssocircle.setAlias(ssocircleAlias);
//            ssocircle.setMetadataSource(MetadataSource.HTTP);
//            ssocircle.setMetadataUrl("https://idp.ssocircle.com/meta-idp.xml");
//            samlIdentityProviderRepository.save(ssocircle);
//        }
//
//        String str = "orgdir=indeed;ecid=tdicarlucci@indeed.com;countrycode=840";
//        try {
//            LOG.info(encryptionService.encrypt(str));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        messagesService.sendSAMLSSOFailedNotification("zztestlogid", "zztestentityid", "zztest");
    }
}
