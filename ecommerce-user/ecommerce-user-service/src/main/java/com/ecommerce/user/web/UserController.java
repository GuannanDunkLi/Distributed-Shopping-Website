package com.ecommerce.user.web;

import com.ecommerce.user.pojo.User;
import com.ecommerce.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
public class UserController {
    @Autowired
    private UserService userService;

    /*
     * Check user data（username or email）
     * @param: data
     * @param: type
     * @return ResponseEntity<Boolean>
     * @author dunklee
     */
    @GetMapping("check/{data}/{type}")
    public ResponseEntity<Boolean> checkUserData(@PathVariable("data") String data, @PathVariable("type") Integer type) {
        Boolean boo = this.userService.checkData(data, type);
        if (boo == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(boo);
    }

    /*
     * send verify codde
     * @param: email
     * @return ResponseEntity<Void>
     * @author dunklee
     */
    @PostMapping("code")
    public ResponseEntity<Void> sendVerifyCode(@RequestParam("email")String email) {
        userService.sendVerifyCode(email);
        return ResponseEntity.noContent().build();
    }

    /*
     * user registration
     * @param: user
     * @param: code
     * @return ResponseEntity<Void>
     * @author dunklee
     */
    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, String code) {
        userService.register(user, code);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /*
     * Query user by username and password
     * @param: username
     * @param: password
     * @return ResponseEntity<User>
     * @author dunklee
     */
    @GetMapping("query")
    public ResponseEntity<User> queryUser(@RequestParam("username") String username, @RequestParam("password") String password) {
        return ResponseEntity.ok(userService.queryUser(username, password));
    }
}