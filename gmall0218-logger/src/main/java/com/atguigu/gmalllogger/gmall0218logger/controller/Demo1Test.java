package com.atguigu.gmalllogger.gmall0218logger.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by wt on 2019-07-19 19:38
 */
@Controller
public class Demo1Test {
    @ResponseBody
    @RequestMapping("test")
    public String test(){
        return "hello gmall";
    }
}
