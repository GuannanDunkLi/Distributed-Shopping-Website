package com.ecommerce.order.web;

import com.ecommerce.order.service.OrderService;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiResponse;
//import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.spec.MGF1ParameterSpec;
import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("notify")
public class NotifyController {
    @Autowired
    private OrderService orderService;

    /*
     * WeChat payment success callback
     * @param: result
     * @return Map<String,String>
     * @author dunklee
     */
    @PostMapping(value = "pay" , produces = "application/xml")
    public Map<String,String> successNotify (@RequestBody Map<String,String> result){
        // Process callback information
        orderService.handleNotify(result);
        log.info("[payment callback] receiving callback information successfully, the result is:{}", result);

        Map<String, String> msg = new HashMap<>();
        msg.put("return_code","SUCCESS");
        msg.put("return_msg","OK");
        return msg;
    }
}