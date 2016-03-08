package io.aiur.oss.db.jdbc.jdbc.binding;

import org.springframework.data.rest.webmvc.support.DelegatingHandlerMapping;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExecutionChain;

import javax.inject.Inject;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by dave on 11/3/15.
 */
@Component
public class JdbcEventFilter implements Filter {

    private static final ThreadLocal<Boolean> CONTEXT = new ThreadLocal<>();

    @Inject
    private DelegatingHandlerMapping restHandlerMapping;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        HttpServletResponse res = (HttpServletResponse) servletResponse;

        Boolean isRestRepoExecution = Boolean.FALSE;
        try {
            try {
                HandlerExecutionChain handler = restHandlerMapping.getHandler(req);
                isRestRepoExecution = handler != null;
            } catch (Exception e) {}

            CONTEXT.set(isRestRepoExecution);
            filterChain.doFilter(req, res);
        }finally{
            CONTEXT.remove();
        }
    }

    @Override
    public void destroy() {}

    public static Boolean isRestRepoExecution(){
        Boolean val = CONTEXT.get();
        return val == null ? Boolean.FALSE : val;
    }
}
