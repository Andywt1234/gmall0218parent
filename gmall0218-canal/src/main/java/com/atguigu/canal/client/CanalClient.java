package com.atguigu.canal.client;


import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.atguigu.canal.hadler.CanalHanlder;
import com.google.protobuf.InvalidProtocolBufferException;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created by wt on 2019-07-22 15:59
 */
public class CanalClient {
    public static void main(String[] args)  {
        //创建连接器
        CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress("hadoop102", 11111), "example", "", "");
        while (true) {
            // 连接
            canalConnector.connect();
            //抓取的表
            canalConnector.subscribe("gmall0218.*");
            Message message = canalConnector.get(100);
            if(message.getEntries().size()==0){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else {
                for (CanalEntry.Entry entry : message.getEntries()) {
                    //每个entry对应一个sql
                    //过滤一下entry，因为不是每个sql都是对数据进行修改的,比如开关事物
                    if(entry.getEntryType().equals(CanalEntry.EntryType.ROWDATA)){
                        CanalEntry.RowChange rowChange=null;
                        try {
                            rowChange.parseFrom (entry.getStoreValue());
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                        }
                        List<CanalEntry.RowData> rowDatasList = rowChange.getRowDatasList();
                        //得到事件的类型，insert，update,delete,drop
                        CanalEntry.EventType eventType = rowChange.getEventType();
                        //得到表明
                        String tableName = entry.getHeader().getTableName();

                        CanalHanlder canalHanlder = new CanalHanlder(tableName, eventType, rowDatasList);
                    }
                }
            }

        }
    }
}
