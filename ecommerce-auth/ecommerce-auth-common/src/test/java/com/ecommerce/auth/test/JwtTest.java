package com.ecommerce.auth.test;

import com.ecommerce.auth.entity.UserInfo;
import com.ecommerce.auth.utils.JwtUtils;
import com.ecommerce.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {
    private static final String pubKeyPath = "D:\\tmp\\rsa.pub";
    private static final String priKeyPath = "D:\\tmp\\rsa.pri";
    private PublicKey publicKey;
    private PrivateKey privateKey;

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Test
    public void testGenerateToken() throws Exception {
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU1ODA3MjE3Mn0.GFb4kMsn7qd1APOZT9wW_924qsAD9lGbhpgTecyanjs67732c03FvsoAMNF9vcvMF7jH7bQ6FhN-OEb-rEFHlWCH0WLddkNFg5uSInapvISAjRpby45GSmwvhjIAVIsNnmil3mO1sEipuKAWbEK_Z0am8UwY8a-OhW-fUzOGUL0".trim();

        // parsing token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }


    public static final String getDomainName(String url) {
        String domainName = null;

        String serverName = url;

        serverName = serverName.toLowerCase();
        serverName = serverName.substring(7);
        final int end = serverName.indexOf("/");
        //serverName = serverName.substring(0, end);
        final String[] domains = serverName.split("\\.");
        int len = domains.length;
        if (len > 3) {
            // www.xxx.com.cn
            domainName = domains[len - 3] + "." + domains[len - 2] + "." + domains[len - 1];
        } else if (len <= 3 && len > 1) {
            // xxx.com or xxx.cn
            domainName = domains[len - 2] + "." + domains[len - 1];
        } else {
            domainName = serverName;
        }


        if (domainName != null && domainName.indexOf(":") > 0) {
            String[] ary = domainName.split("\\:");
            domainName = ary[0];
        }
        return domainName;
    }

    @Test
    public void cookied() {
        System.out.println(getDomainName("http://www.baidu.com"));//baidu.com
    }
}
