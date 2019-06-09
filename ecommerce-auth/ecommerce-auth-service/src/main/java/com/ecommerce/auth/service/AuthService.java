package com.ecommerce.auth.service;

import com.ecommerce.auth.client.UserClient;
import com.ecommerce.auth.config.JwtProperties;
import com.ecommerce.auth.entity.UserInfo;
import com.ecommerce.auth.utils.JwtUtils;
import com.ecommerce.common.enums.ExceptionEnum;
import com.ecommerce.common.exception.EException;
import com.ecommerce.user.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableConfigurationProperties(JwtProperties.class)
public class AuthService {
    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties props;

    public String login(String username, String password) {
        try {
            // Verify username and password
            User user = userClient.queryUser(username, password);
            if (user == null) {
                return null;
            }

            // Generate Token
            UserInfo userInfo = new UserInfo(user.getId(), username);
            String token = JwtUtils.generateToken(userInfo, props.getPrivateKey(), props.getExpire());
            return token;
        } catch (Exception e) {
            log.error("【Authorization center】incorrect username and password，username is：{}", username,e);
            throw new EException(ExceptionEnum.USERNAME_OR_PASSWORD_ERROR);
        }
    }
}
