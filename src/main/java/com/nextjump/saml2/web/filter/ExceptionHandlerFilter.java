package com.nextjump.saml2.web.filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ExceptionHandlerFilter extends OncePerRequestFilter {
    private final static Log LOG = LogFactory.getLog(ExceptionHandlerFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
//            Enumeration<String> headerNames = request.getHeaderNames();
//            while (headerNames.hasMoreElements()) {
//                String headerName = headerNames.nextElement();
//                String headerValue = request.getHeader(headerName);
//                LOG.debug(String.format("%s=%s", headerName, headerValue));
//            }
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (response.isCommitted()) {

            } else {
                request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
                request.setAttribute(RequestDispatcher.ERROR_MESSAGE, ex.getMessage());
                RequestDispatcher dispatcher = getServletContext()
                        .getRequestDispatcher("/error");
                dispatcher.forward(request, response);
            }
        }
    }
}
