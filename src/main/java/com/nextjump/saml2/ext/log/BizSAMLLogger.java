package com.nextjump.saml2.ext.log;

import com.google.common.base.Strings;
import com.nextjump.saml2.AppSettings;
import com.nextjump.saml2.model.SAMLOperationLog;
import com.nextjump.saml2.repository.SAMLOperationLogRepository;
import com.nextjump.saml2.service.MessagesService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.ws.message.encoder.MessageEncodingException;
import org.opensaml.ws.transport.http.HTTPInTransport;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.util.XMLHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.context.SAMLMessageContext;
import org.springframework.security.saml.log.SAMLLogger;
import org.springframework.security.saml.util.SAMLUtil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;

public class BizSAMLLogger implements SAMLLogger {
    private final static Log LOG = LogFactory.getLog(BizSAMLLogger.class);
    @Autowired
    private SAMLOperationLogRepository samlOperationLogRepository;

    @Autowired
    private MessagesService messagesService;

    @Autowired
    private AppSettings appSettings;

    @Override
    public void log(String operation, String result, SAMLMessageContext context) {
        log(operation, result, context, SecurityContextHolder.getContext().getAuthentication(), null);
    }

    @Override
    public void log(String operation, String result, SAMLMessageContext context, Exception e) {
        log(operation, result, context, SecurityContextHolder.getContext().getAuthentication(), e);
    }

    @Override
    public void log(String operation, String result, SAMLMessageContext context, Authentication a, Exception e) {
        if (operation == null) operation = "";
        if (result == null) result = "";
        if (context == null) context = new SAMLMessageContext();

        SAMLOperationLog model = new SAMLOperationLog();
        // Log operation
        StringBuilder sb = new StringBuilder();
        sb.append(operation);
        model.setOperation(operation);
        // Log result
        sb.append(";");
        sb.append(result);
        model.setResult(result);
        // Log peer address
        sb.append(";");
        if (context.getInboundMessageTransport() != null) {
            HTTPInTransport transport = (HTTPInTransport) context.getInboundMessageTransport();
            String realIp = transport.getHeaderValue("X-Forwarded-For".toLowerCase());
            if (Strings.isNullOrEmpty(realIp)) {
                realIp = transport.getHeaderValue("X-Real-IP".toLowerCase());
            }
            if (Strings.isNullOrEmpty(realIp)) {
                realIp = transport.getPeerAddress();
            }
            model.setPeerAddress(realIp);
            sb.append(realIp);
        }
        // Log local entity ID
        sb.append(";");
        if (context.getLocalEntityId() != null) {
            model.setLocalEntityId(context.getLocalEntityId());
            sb.append(context.getLocalEntityId());
        }
        // Log peer entity ID
        sb.append(";");
        if (context.getPeerEntityId() != null) {
            model.setPeerEntityId(context.getPeerEntityId());
            sb.append(context.getPeerEntityId());
        }

        // Log NameID or principal when available
        sb.append(";");
        if (a != null) {
            if (a.getCredentials() != null && a.getCredentials() instanceof SAMLCredential) {
                SAMLCredential credential = (SAMLCredential) a.getCredentials();
                if (credential.getNameID() != null) {
                    model.setPrincipal(credential.getNameID().getValue());
                } else {
                    model.setPrincipal(a.getPrincipal().toString());
                }
            } else {
                model.setPrincipal(a.getPrincipal().toString());
            }
            sb.append(model.getPrincipal());
        }

        // Log http request headers when available
        sb.append(";");
        if (context.getInboundMessageTransport() != null) {
            if (context.getInboundMessageTransport() instanceof HttpServletRequestAdapter) {
                StringBuilder headers = new StringBuilder();
                HttpServletRequestAdapter transport = (HttpServletRequestAdapter) context.getInboundMessageTransport();
                Enumeration<String> names = transport.getWrappedRequest().getHeaderNames();
                while (names.hasMoreElements()) {
                    String name = names.nextElement();
                    String value = transport.getHeaderValue(name);
                    headers.append(name).append("=").append(value).append("|");
                }
                model.setHttpRequestHeaders(headers.toString());
                sb.append(headers);
            }
        }

        // Log SAML message
        sb.append(";");
        try {
            if (context.getInboundSAMLMessage() != null) {
                String messageStr =
                        XMLHelper.nodeToString(SAMLUtil.marshallMessage(context.getInboundSAMLMessage()));
                model.setMessage(messageStr);
            }
            if (context.getOutboundSAMLMessage() != null) {
                String messageStr =
                        XMLHelper.nodeToString(SAMLUtil.marshallMessage(context.getOutboundSAMLMessage()));
                model.setMessage(messageStr);
            }
        } catch (MessageEncodingException e1) {
            LOG.warn("Error marshaling message during logging", e1);
        }

        // Log error
        sb.append(";");
        if (null != e) {
            StringWriter errorWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(errorWriter));
            model.setError(errorWriter.getBuffer().toString());
            sb.append(model.getError());
        }

        model = samlOperationLogRepository.save(model);

        if (model.getResult().equals("FAILURE") && model.getOperation().equals("LogoutRequest") && appSettings.isIgnoreLogoutFailed()) {
            // ignore the LogoutRequest failed case
        } else if (model.getResult().equals("FAILURE")) {
            messagesService.sendSAMLSSOFailedNotification(model.getId(), model.getPeerEntityId(),
                    model.getPrincipal(), model.getError());
        }
        LOG.info(sb.toString());
    }
}
