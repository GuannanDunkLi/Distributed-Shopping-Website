package com.ecommerce.order.interceptors;

import com.ecommerce.auth.entity.UserInfo;
import com.ecommerce.auth.utils.JwtUtils;
import com.ecommerce.common.utils.CookieUtils;
import com.ecommerce.order.config.JwtProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtProperties props;

    // Define a thread domain to store the login user object
    private static final ThreadLocal<UserInfo> t1 = new ThreadLocal<>();

    public UserInterceptor(JwtProperties props) {
        this.props = props;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Get the token in the cookie
        String token = CookieUtils.getCookieValue(request, props.getCookieName());
        if (StringUtils.isBlank(token)) {
            // user didn't login
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        // user did login
        try {
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, props.getPublicKey());
            // Put into the thread domain
            t1.set(userInfo);
            return true;
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // After completing the interception, the user information is deleted from the thread domain.
        t1.remove();
    }

    /*
     * Get login user
     * @param:
     * @return com.ecommerce.auth.entity.UserInfo
     * @author dunklee
     */
    public static UserInfo getLoginUser() {
        return t1.get();
    }
}
