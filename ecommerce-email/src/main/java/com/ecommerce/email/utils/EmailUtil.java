package com.ecommerce.email.utils;

import com.ecommerce.email.config.EmailProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.mail.internet.MimeMessage;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@EnableConfigurationProperties(EmailProperties.class)
public class EmailUtil {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private EmailProperties emailProperties;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "email:to:";

    public void sendHtmlMail(String to, String subject, String content) {
        String key = KEY_PREFIX + to;
        String lastTime = redisTemplate.opsForValue().get(key); // 读取时间
        if (!StringUtils.isEmpty(lastTime)) {
            log.info("Sending email is less than 1 minute, please be patient");
            return;
        }

        // send email
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(emailProperties.getFrom());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            // record email log
            log.info("send to {} successfully",to);
            // After sending the email successfully, write to redis and specify the lifetime to be 1 min.
            redisTemplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()), 1, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("sending email fail");
        }
    }
}