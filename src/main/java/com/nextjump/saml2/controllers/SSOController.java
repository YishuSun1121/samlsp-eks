package com.nextjump.saml2.controllers;

import com.nextjump.saml2.AppSettings;
import com.nextjump.saml2.model.SAMLIdentityProvider;
import com.nextjump.saml2.repository.SAMLIdentityProviderRepository;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.context.SAMLContextProvider;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.metadata.ExtendedMetadata;
import org.springframework.security.saml.metadata.MetadataManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/saml")
public class SSOController {
    // Logger
    private static final Logger LOG = LoggerFactory
            .getLogger(SSOController.class);

    @Autowired
    private AppSettings appSettings;
    @Autowired
    private MetadataManager metadata;
    @Autowired
    private SAMLContextProvider contextProvider;
    @Autowired
    private SAMLIdentityProviderRepository samlIdentityProviderRepository;


    @RequestMapping(value = "/discovery", method = RequestMethod.GET)
    public String idpSelection(HttpServletRequest request, HttpServletResponse response, Model model) throws MetadataProviderException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            LOG.debug("Current authentication instance from security context is null");
        else
            LOG.debug("Current authentication instance from security context: "
                    + this.getClass().getSimpleName());
        if (auth == null
                || (auth instanceof AnonymousAuthenticationToken)
                || (auth instanceof UsernamePasswordAuthenticationToken)) {

            SAMLMessageContext context = contextProvider.getLocalAndPeerEntity(request, response);
            String localEntityId = context.getLocalEntityId();
            String localAlias = metadata.getExtendedMetadata(localEntityId).getAlias();

            Set<String> entityIds = metadata.getIDPEntityNames();
            Map<String, String> idpMap = new HashMap<>();
            for (String entityId : entityIds) {
                LOG.info("Configured Identity Provider for SSO: " + entityId);
                ExtendedMetadata extendedMetadata = metadata.getExtendedMetadata(entityId);
                String alias = extendedMetadata.getAlias();
                SAMLIdentityProvider samlIdentityProvider = samlIdentityProviderRepository.findByAlias(alias);
                if (null == samlIdentityProvider.getSpAliases() || samlIdentityProvider.getSpAliases().isEmpty() || samlIdentityProvider.getSpAliases().contains(localAlias)) {
                    idpMap.put(alias, entityId);
                }
            }
            model.addAttribute("idpMap", idpMap);
            return "pages/discovery";
        } else {
            LOG.warn("The current user is already logged.");
            if (appSettings.isEnableLB()) {
                return String.format("redirect:%s://%s%s/landing", appSettings.getLbSchema(),
//                        appSettings.getLbServerName(),
                        appSettings.getServerNameFromLb(request),
                        appSettings.getLbServerContextPath());
            } else {
                return "redirect:/landing";
            }
        }
    }

}
