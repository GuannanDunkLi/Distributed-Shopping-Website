package com.ecommerce.auth.web;

import com.ecommerce.auth.config.JwtProperties;
import com.ecommerce.auth.entity.UserInfo;
import com.ecommerce.auth.service.AuthService;
import com.ecommerce.auth.utils.JwtUtils;
import com.ecommerce.common.enums.ExceptionEnum;
import com.ecommerce.common.exception.EException;
import com.ecommerce.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties props;

    /*
     * Login authorization
     * @param: username
     * @param: password
     * @param: request
     * @param: response
     * @return ResponseEntity<Void>
     * @author dunklee
     */
    @PostMapping("login")
    public ResponseEntity<Void> login(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String token = authService.login(username, password);
        if (StringUtils.isBlank(token)) {
            throw new EException(ExceptionEnum.USERNAME_OR_PASSWORD_ERROR);
        }

        // Write Token to the cookie (httpOnly() can disable the JS code to operate the cookie
        // Use the 'request' to know the domain of the cookie, prevent cross-domain requests)
        CookieUtils.newBuilder(response).httpOnly().request(request).build(props.getCookieName(), token);
        return ResponseEntity.ok().build();
    }

    /*
     * Verify user
     * @param: token
     * @param: request
     * @param: response
     * @return org.springframework.http.ResponseEntity<com.ecommerce.auth.entity.UserInfo>
     * @author dunklee
     */
    @GetMapping("verify")
    public ResponseEntity<UserInfo> verifyUser(
            @CookieValue("E_TOKEN") String token,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            // Get user information from Token
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, props.getPublicKey());
            // If successful, refresh Token
            String newToken = JwtUtils.generateToken(userInfo, props.getPrivateKey(), props.getExpire());
            // Write a new Token to the cookie
            CookieUtils.newBuilder(response).httpOnly().request(request).build(props.getCookieName(), newToken);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            // Verification is failed
            throw new EException(ExceptionEnum.UNAUTHORIZED);
        }
    }
}
