package com.ecommerce.user.service;

import com.ecommerce.common.enums.ExceptionEnum;
import com.ecommerce.common.exception.EException;
import com.ecommerce.common.utils.NumberUtils;
import com.ecommerce.user.mapper.UserMapper;
import com.ecommerce.user.pojo.User;
import com.ecommerce.user.utils.CodecUtils;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "user:verify:email:";

    public Boolean checkData(String data, Integer type) {
        User user = new User();
        // Determine the type of verification data
        switch (type) {
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setEmail(data);
                break;
            default:
                throw new EException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }
        return userMapper.selectCount(user) == 0;
    }

    public void sendVerifyCode(String email) {
        // Randomly generate 6-digit verification code
        String code = NumberUtils.generateCode(6);
        // Generate key
        String key = KEY_PREFIX + email;

        // Put the verification code into Redis and set the validity period to 5min.
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);

        // Send message to mq
        Map<String, String> map = new HashMap<>();
        map.put("email", email);
        map.put("code", code);
        amqpTemplate.convertAndSend("leyou.email.exchange", "email.verify.code", map);
    }


    public void register(User user, String code) {
        // Take the verification code from redis
        String key = KEY_PREFIX + user.getEmail();
        String value = redisTemplate.opsForValue().get(key);

        if (!StringUtils.equals(code, value)) {
            throw new EException(ExceptionEnum.VERIFY_CODE_NOT_MATCHING);
        }

        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        // Encrypt password
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), user.getSalt()));
        user.setCreated(new Date());
        int count = userMapper.insert(user);
        if (count != 1) {
            throw new EException(ExceptionEnum.INVALID_USER_DATA_TYPE);
        }
        // Remove the verification code from Redis
        redisTemplate.delete(key);
    }

    public User queryUser(String username, String password) {
        User record = new User();
        record.setUsername(username);

        // query the user based on the username
        User user = userMapper.selectOne(record);

        if (user == null) {
            throw new EException(ExceptionEnum.USER_NOT_EXIST);
        }

        if (!StringUtils.equals(CodecUtils.md5Hex(password, user.getSalt()), user.getPassword())) {
            throw new EException(ExceptionEnum.PASSWORD_NOT_MATCHING);
        }
        return user;
    }
}