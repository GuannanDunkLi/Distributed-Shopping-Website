package com.ecommerce.auth.client;

import com.ecommerce.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

// call the micro-service
@FeignClient("user-service")
public interface UserClient extends UserApi {
}
