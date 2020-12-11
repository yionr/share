package cn.yionr.share.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(filterName = "permissionFilter" , urlPatterns = "/*")
public class PermissionFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        System.out.println();
        if ("POST".equals(((HttpServletRequest)servletRequest).getMethod())){
//            如果是POST请求的话
            filterChain.doFilter(servletRequest, servletResponse);
        }
        else{
//            GET请求不必校验
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }
}
