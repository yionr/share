package cn.yionr.share.filter;

import cn.yionr.share.dao.UserDao;
import cn.yionr.share.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "permissionFilter", urlPatterns = {"/upload.do"})
public class PermissionFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(PermissionFilter.class);

    UserDao userDao;

    @Autowired
    public PermissionFilter(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (((HttpServletRequest) servletRequest).getRequestURI().contains(".do")) {
//            如果是自建请求
            HttpSession session = ((HttpServletRequest) servletRequest).getSession();
            User user;
            if (session.getAttribute("email") != null) {
                user = userDao.queryUser((String) session.getAttribute("email"));
                if (user.getPassword().equals(session.getAttribute("password"))) {
//                账号密码都正确
                    log.info("用户操作");
                    servletRequest.setAttribute("visitor", false);
                } else {
//                密码错误
                    log.info("用户密码错误");
                }
            } else {
//            游客
                log.info("游客操作");
                servletRequest.setAttribute("visitor", true);
            }
        } else {
//            其余请求
        }
        filterChain.doFilter(servletRequest, servletResponse);


    }

    @Override
    public void destroy() {

    }
}
