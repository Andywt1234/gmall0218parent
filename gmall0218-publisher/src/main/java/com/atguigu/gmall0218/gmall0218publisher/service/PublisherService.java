package com.atguigu.gmall0218.gmall0218publisher.service;

import java.util.Map;

/**
 * Created by wt on 2019-07-22 10:26
 */
public interface PublisherService {
    /**
     * 查询总数
     * @param date
     * @return
     */
    public int getDauTotal(String date);

    /**
     * 分时统计
     * @param date
     * @return
     */
    public Map getDauHour(String date);

}
