package com.atguigu.gmall0218.gmall0218publisher.service.impl;

import com.atguigu.gmall0218.gmall0218publisher.mapper.DauMapper;
import com.atguigu.gmall0218.gmall0218publisher.service.PublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wt on 2019-07-22 10:28
 */
@Service
public class PublisherServiceImpl implements PublisherService {
    @Autowired
    DauMapper dauMapper;

    @Override
    public int getDauTotal(String date) {
        return dauMapper.getDauTotal(date);
    }

    @Override
    public Map getDauHour(String date) {
        List<Map> dauHour = dauMapper.getDauHour(date);
        //把List<Map>转换为Map结构
        
        Map dauHourMap=new HashMap();
        for (Map map : dauHour) {

            
        }
        return dauHourMap;
        
    }
}
