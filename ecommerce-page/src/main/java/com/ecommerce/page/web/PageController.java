package com.ecommerce.page.web;

import com.ecommerce.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class PageController {
    @Autowired
    private PageService pageService;

    @GetMapping("item/{id}.html")
    public String toItemPage(@PathVariable("id") Long spuId, Model model) {
        // 加载所需的数据
        Map<String, Object> modelMap = pageService.loadModel(spuId);
        // 放入模型
        model.addAllAttributes(modelMap);
        return "item";
    }
}
