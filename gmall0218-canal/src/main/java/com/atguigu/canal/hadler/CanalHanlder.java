package com.atguigu.canal.hadler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.atguigu.canal.utils.MyKafkaSender;
import com.atguigu.util.GmallConstant;

import java.util.List;

/**
 * Created by wt on 2019-07-22 16:47
 */
public class CanalHanlder {
    String tableName;
    CanalEntry.EventType eventType;
    List<CanalEntry.RowData> rowDataList;

    public CanalHanlder(String tableName, CanalEntry.EventType eventType, List<CanalEntry.RowData> rowDataList) {
        this.tableName = tableName;
        this.eventType = eventType;
        this.rowDataList = rowDataList;
    }
    public void hadle(){
        if(tableName.equals("order_info")&& eventType== CanalEntry.EventType.INSERT){//下单操作
            for (CanalEntry.RowData rowData : rowDataList) {//遍历行集

                List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();//修改后的列集
                JSONObject jsonObject = new JSONObject();
                for (CanalEntry.Column column : afterColumnsList) {
                    System.out.println(column.getName()+"|||||||||"+column.getValue());
                    jsonObject.put(column.getName(),column.getValue());
                }
                MyKafkaSender.send(GmallConstant.KAFKA_TOPIC_ORDER,jsonObject.toJSONString());
            }
        }
    }
}
