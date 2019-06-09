package com.ecommerce.auth.config;

import com.ecommerce.auth.utils.RsaUtils;
import com.ecommerce.common.enums.ExceptionEnum;
import com.ecommerce.common.exception.EException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@Slf4j
@Data
@Primary
@Component
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties {
    private String secret;
    private String pubKeyPath;
    private String priKeyPath;
    private Integer expire;
    private String cookieName;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    // Once the object is instantiated, it should read the public and private keys.
    @PostConstruct
    public void init() {
        try {
            // determine whether the public key and the private key exist.
            File pubKey = new File(pubKeyPath);
            File priKey = new File(priKeyPath);

            // If it does not exist, then generate the public key and the private key.
            if (!pubKey.exists() || !priKey.exists()) {
                RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
            }

            // read keys directly
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
            this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            log.error("Initialization of the public key and the private key failed", e);
            throw new EException(ExceptionEnum.INIT_KEYS_ERROR);
        }
    }
}