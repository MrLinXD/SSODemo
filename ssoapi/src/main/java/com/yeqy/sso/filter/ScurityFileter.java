package com.yeqy.sso.filter;

import com.yeqy.sso.biz.User;
import com.yeqy.sso.util.HttpUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by yeqy on 2017/9/12.
 */
public class ScurityFileter implements Filter {

    private String authPath;

    public void init(FilterConfig filterConfig) throws ServletException {
        authPath = filterConfig.getInitParameter("authPath");
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;


        User user = (User) request.getSession().getAttribute("user");
        if (user != null) {//存在会话(已登录过)
            filterChain.doFilter(request, response);
        } else {//未登录
            String token = request.getParameter("token");
            if (token != null && token.trim().length() > 0) {//有token 验证token
                try {
                    User userInfo = HttpUtil.authToken("http://localhost:8080/auth?token=" + token);
                    if (userInfo != null) {//token有效
                        request.getSession().setAttribute("user", userInfo);
                        filterChain.doFilter(request, response);
                    } else {//token无效
                        toLogin(response, request);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    toLogin(response, request);
                }
            } else {//没登录
                toLogin(response, request);
            }
        }


    }


    private void toLogin(HttpServletResponse response, HttpServletRequest request) throws IOException, ServletException {
        StringBuilder sb = new StringBuilder(authPath).append("?redirect=").append(request.getRequestURL());
        response.sendRedirect(sb.toString());
    }

    public void destroy() {

    }

}
