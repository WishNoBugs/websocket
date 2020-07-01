package com.wanbang.youyibang.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TestController {
    @GetMapping("/hello")
    @ResponseBody
    public String hello(){
        return "hello world";
    }
    @RequestMapping("/socket")
    public ModelAndView socket(){
        return new ModelAndView("socket") ;
    }
}
