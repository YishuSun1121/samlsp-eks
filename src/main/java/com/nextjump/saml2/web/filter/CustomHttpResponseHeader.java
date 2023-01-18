package com.nextjump.saml2.web.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class CustomHttpResponseHeader implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader(
                "Strict-Transport-Security", "max-age=315360000; includeSubDomains");
        httpServletResponse.setHeader(
                "X-Content-Type-Options", "nosniff");
        filterChain.doFilter(request, response);
    }
}
