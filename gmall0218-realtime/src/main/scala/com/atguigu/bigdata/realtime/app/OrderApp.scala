package com.atguigu.bigdata.realtime.app

import com.alibaba.fastjson.JSON
import com.atguigu.bigdata.realtime.bean.OrderInfo
import com.atguigu.bigdata.realtime.util.MyKafkaUtil
import com.atguigu.util.GmallConstant
import org.apache.hadoop.conf.Configuration
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.phoenix.spark._

/**
  * Created by wt on 2019-07-23 09:13
  */
object OrderApp {
  def main(args: Array[String]): Unit = {
    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("order_app")

    val ssc = new StreamingContext(sparkConf,Seconds(5))

    val inputDstream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(GmallConstant.KAFKA_TOPIC_ORDER,ssc)

    //    inputDstream.map(_.value()).foreachRDD(rdd=>
    //      println(rdd.collect().mkString("\n"))
    //    )
    //

    val orderInfoDstrearm: DStream[OrderInfo] = inputDstream.map {
      _.value()
    }.map { orderJson =>
      val orderInfo: OrderInfo = JSON.parseObject(orderJson, classOf[OrderInfo])
      //日期

      val createTimeArr: Array[String] = orderInfo.create_time.split(" ")
      orderInfo.create_date = createTimeArr(0)
      val timeArr: Array[String] = createTimeArr(1).split(":")
      orderInfo.create_hour = timeArr(0)
      // 收件人 电话 脱敏
      orderInfo.consignee_tel = "*******" + orderInfo.consignee_tel.splitAt(7)._2


      // TODO: 练习需求，增加一个字段，在订单表，调试该笔订单是否是该用户的首次下单，"1"表示首次下单，"0"表示 非首次下单
      //1维护一个清单，每次新用户下单，要追加入清单
      //2利用清单进行标示，
      //3同时还要查询本批次
      //4要注意状态信息表的大小

      orderInfo
    }


    orderInfoDstrearm.foreachRDD { rdd =>


      val configuration = new Configuration()
      println(rdd.collect().mkString("\n"))
      rdd.saveToPhoenix("GMALL2019_ORDER_INFO", Seq("ID","PROVINCE_ID", "CONSIGNEE", "ORDER_COMMENT", "CONSIGNEE_TEL", "ORDER_STATUS", "PAYMENT_WAY", "USER_ID","IMG_URL", "TOTAL_AMOUNT", "EXPIRE_TIME", "DELIVERY_ADDRESS", "CREATE_TIME","OPERATE_TIME","TRACKING_NO","PARENT_ORDER_ID","OUT_TRADE_NO", "TRADE_BODY", "CREATE_DATE", "CREATE_HOUR"), configuration, Some("hadoop102,hadoop103,hadoop104:2181"))

    }

    ssc.start()
    ssc.awaitTermination()
  }

}
