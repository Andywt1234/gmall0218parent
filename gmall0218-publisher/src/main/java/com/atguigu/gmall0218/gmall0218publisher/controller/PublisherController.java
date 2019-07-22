package com.atguigu.gmall0218.gmall0218publisher.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0218.gmall0218publisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Created by wt on 2019-07-22 10:30
 */
@RestController
public class PublisherController {
    @Autowired
    PublisherService publisherService;

    @GetMapping("realtime-total")
    public String getRealtimeTotal(@RequestParam("date") String date) {
        List<Map> totalList = new ArrayList<Map>();
        Map dauMap = new HashMap();
        dauMap.put("id", "dau");
        dauMap.put("name", "新增日活");
        int dauTotal = publisherService.getDauTotal(date);
        dauMap.put("value", dauTotal);


        Map newMidMap = new HashMap();
        newMidMap.put("id", "dau");
        newMidMap.put("name", "新增设备");
        newMidMap.put("value", 233);


        totalList.add(dauMap);
        totalList.add(newMidMap);

        return JSON.toJSONString(totalList);


    }


}
