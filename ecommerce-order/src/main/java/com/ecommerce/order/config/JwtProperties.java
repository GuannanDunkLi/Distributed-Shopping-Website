package com.ecommerce.order.config;

import com.ecommerce.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

@Slf4j
@Data
// 不知道为什么会扫描到target的这个类，所以加Primary
@Primary
// 不加Component注释ConfigurationProperties会报错
@Component
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {
    private String pubKeyPath;
    private String cookieName;

    private PublicKey publicKey;

    // 对象一旦实例化后，就应该读取公钥和私钥
    @PostConstruct
    public void init() {
        try {
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥失败",e);
            throw new RuntimeException();
        }
    }
}