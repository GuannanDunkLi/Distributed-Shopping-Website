package com.ecommerce.user.api;

import com.ecommerce.user.pojo.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserApi {
    /*
     * Query users based on username and password
     * @param: username
     * @param: password
     * @return ResponseEntity<User>
     * @author dunklee
     */
    @GetMapping("query")
    User queryUser(@RequestParam("username") String username, @RequestParam("password") String password);
}
