package com.atguigu.gmall0218.gmall0218publisher.mapper;

import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

/**
 * Created by wt on 2019-07-22 09:48
 */
@Controller
public interface DauMapper {
    int getDauTotal(String date);

    List<Map> getDauHour(String date);
}
