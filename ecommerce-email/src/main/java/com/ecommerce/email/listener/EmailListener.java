package com.ecommerce.email.listener;

import com.ecommerce.email.config.EmailProperties;
import com.ecommerce.email.utils.EmailUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

@Slf4j
@Component
@EnableConfigurationProperties(EmailProperties.class)
public class EmailListener {
    @Autowired
    private EmailUtil emailUtil;
    @Autowired
    private EmailProperties emailProperties;

    /*
     * Email send verification code
     * @param: spuId
     * @return void
     * @author dunklee
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "email.verify.code.queue", durable = "true"),
            exchange = @Exchange(name = "leyou.email.exchange", type = ExchangeTypes.TOPIC),
            key = "email.verify.code"
    ))
    public void ListenInsertOrUpdate(Map<String, String> msg) {
        if (CollectionUtils.isEmpty(msg)) {
            return;
        }
        String toEmail = msg.get("email");
        if (StringUtils.isEmpty(toEmail)) {
            return;
        }
        // send email
        emailUtil.sendHtmlMail(toEmail, emailProperties.getRegisterSubject(), emailProperties.getContent() + msg.get("code"));
    }
}