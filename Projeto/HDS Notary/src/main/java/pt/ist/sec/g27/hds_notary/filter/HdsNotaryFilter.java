package pt.ist.sec.g27.hds_notary.filter;

import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class HdsNotaryFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ResponseWrapper responseWrapper = new ResponseWrapper((HttpServletResponse) response);

        chain.doFilter(request, responseWrapper);

        ClientHttpRequestFactory a = new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory());


        byte[] dataStream = responseWrapper.getDataStream();
    }

    @Override
    public void destroy() {

    }
}
