package com.nextjump.saml2.ext.decoding;

import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.ws.message.decoder.MessageDecodingException;
import org.opensaml.ws.transport.InTransport;
import org.opensaml.ws.transport.http.HttpServletRequestAdapter;
import org.opensaml.xml.parse.ParserPool;
import org.opensaml.xml.util.DatatypeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * customize HTTPPostDecoder to return the correct ActualReceiverEndpointURI.
 */
public class HTTPPostDecoder extends org.opensaml.saml2.binding.decoding.HTTPPostDecoder {

    private static final Logger LOG = LoggerFactory.getLogger(HTTPPostDecoder.class);

    public HTTPPostDecoder(ParserPool pool) {
        super(pool);
    }

    @Override
    protected String getActualReceiverEndpointURI(SAMLMessageContext messageContext) throws MessageDecodingException {
//        InTransport inTransport = messageContext.getInboundMessageTransport();
//        if (!(inTransport instanceof HttpServletRequestAdapter)) {
//            throw new MessageDecodingException("Message context InTransport instance was an unsupported type");
//        }
//        HttpServletRequest httpRequest = ((HttpServletRequestAdapter) inTransport).getWrappedRequest();
//        StringBuffer urlBuilder = httpRequest.getRequestURL();
//        String result = urlBuilder.toString();
//        LOG.debug("ActualReceiverEndpointURI: " + result);

        // using destination as actual receiver endpoint
        // TODO maybe need make it configurable for special identity provider
        String result = DatatypeHelper.safeTrimOrNullString(getIntendedDestinationEndpointURI(messageContext));
        return result;
    }
}
