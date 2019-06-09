package com.ecommerce.cart.service;

import com.ecommerce.auth.entity.UserInfo;
import com.ecommerce.cart.interceptor.UserInterceptor;
import com.ecommerce.cart.pojo.Cart;
import com.ecommerce.common.enums.ExceptionEnum;
import com.ecommerce.common.exception.EException;
import com.ecommerce.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "ly:cart:uid:";

    /*
     * Add cart to Redis
     * @param: cart
     * @return void
     * @author dunklee
     */
    public void addCart(Cart cart) {
        // Get user information
        UserInfo loginUser = UserInterceptor.getLoginUser();

        String key = KEY_PREFIX + loginUser.getId();
        // Get product ID
        String hashKey = cart.getSkuId().toString();
        // Get number
        Integer num = cart.getNum();

        // Get the object of the hash operation
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(hashKey)) {
            // If redis has this item, edit the number.
            cart = JsonUtils.toBean(hashOps.get(hashKey).toString(), Cart.class);
            cart.setNum(num + cart.getNum());
        }
        // put into redis
        hashOps.put(hashKey, JsonUtils.toString(cart));
    }

    public List<Cart> queryCartList() {
        // Get login user
        UserInfo loginUser = UserInterceptor.getLoginUser();

        String key = KEY_PREFIX + loginUser.getId();
        if (!redisTemplate.hasKey(key)) {
            throw new EException(ExceptionEnum.CART_NOT_FOUND);
        }

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        List<Object> carts = hashOps.values();

        return carts.stream().map(s -> JsonUtils.toBean(s.toString(), Cart.class)).collect(Collectors.toList());
    }

    public void updateNum(Long id, Integer num) {
        // Get login user
        UserInfo loginUser = UserInterceptor.getLoginUser();

        String key = KEY_PREFIX + loginUser.getId();
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);

        if (!hashOps.hasKey(id.toString())) {
            throw new EException(ExceptionEnum.CART_NOT_FOUND);
        }

        // Query products from the shopping cart
        Cart cart = JsonUtils.toBean(hashOps.get(id.toString()).toString(), Cart.class);
        cart.setNum(num);

        // put into Redis
        hashOps.put(id.toString(), JsonUtils.toString(cart));
    }

    public void deleteCart(Long id) {
        // Get login user
        UserInfo loginUser = UserInterceptor.getLoginUser();

        String key = KEY_PREFIX + loginUser.getId();

        // Delete
        redisTemplate.opsForHash().delete(key, id.toString());
    }
}
