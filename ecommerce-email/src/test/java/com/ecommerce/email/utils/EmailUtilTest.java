package com.ecommerce.email.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailUtilTest {
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void htmlEmail() throws Exception {
        Map<String,String> map = new HashMap<>();
        map.put("email", "423327637lgn@gmail.com");
        map.put("code", "xxxx");
        amqpTemplate.convertAndSend("leyou.email.exchange", "email.verify.code", map);
    }
}