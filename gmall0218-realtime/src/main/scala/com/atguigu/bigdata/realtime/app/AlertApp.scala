package com.atguigu.bigdata.realtime.app

import com.alibaba.fastjson.JSON
import com.atguigu.bigdata.realtime.bean.{CouponAlertInfo, EventInfo}
import com.atguigu.bigdata.realtime.util.{MyEsUtil, MyKafkaUtil}
import com.atguigu.util.GmallConstant
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.util.control.Breaks._

/**
  * Created by wt on 2019-07-23 11:17
  */
object AlertApp {
  def main(args: Array[String]): Unit = {
    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("event_app")

    val ssc = new StreamingContext(sparkConf, Seconds(5))

    val inputDstream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream(GmallConstant.KAFKA_TOPIC_EVENT, ssc)

    //1 格式转换成样例类
    val eventInfoDstream: DStream[EventInfo] = inputDstream.map { record =>
      val jsonstr: String = record.value()
      val eventInfo: EventInfo = JSON.parseObject(jsonstr, classOf[EventInfo])
      eventInfo
    }
    //2 开窗口
    val eventInfoWindowDstream: DStream[EventInfo] = eventInfoDstream.window(Seconds(30), Seconds(5))
    //打印测试
    //eventInfoDstream.foreachRDD(rdd=>println(rdd.collect().mkString("\n")))
    //eventInfoDstream.foreachRDD(rdd=>rdd.foreach(println))
    //3同一设备 分组
    val groupbyMidDstream: DStream[(String, Iterable[EventInfo])] = eventInfoWindowDstream.map(eventInfo => (eventInfo.mid, eventInfo)).groupByKey()

    //4 判断预警
    //    在一个设备之内
    //    1 三次及以上的领取优惠券 (evid coupon) 且 uid都不相同
    //    2 没有浏览商品(evid  clickItem)

    val checkCouponAlertDStream: DStream[(Boolean, CouponAlertInfo)] = groupbyMidDstream.map { case (mid, eventInfoItr) =>
      val couponUidsSet = new java.util.HashSet[String]()
      val itemIdsSet = new java.util.HashSet[String]()
      val eventIds = new java.util.ArrayList[String]()
      var notClickItem: Boolean = true
      breakable(
        for (eventInfo: EventInfo <- eventInfoItr) {
          eventIds.add(eventInfo.evid) //用户行为
          if (eventInfo.evid == "addComment") {
            couponUidsSet.add(eventInfo.uid) //用户领券的uid
            itemIdsSet.add(eventInfo.itemid) //用户领券的商品id
          } else if (eventInfo.evid == "clickItem") {
            notClickItem = false
            break()
          }
        }
      )
      //组合成元组  （标识是否达到预警要求，预警信息对象）
      (couponUidsSet.size() >= 3 && notClickItem, CouponAlertInfo(mid, couponUidsSet, itemIdsSet, eventIds, System.currentTimeMillis()))
    }

   // checkCouponAlertDStream.foreachRDD(rdd=>println(rdd.collect().mkString("\n")+"bbbbbb"))
    //过滤
    val filteredDstream: DStream[(Boolean, CouponAlertInfo)] = checkCouponAlertDStream.filter {
      _._1
    }
   // filteredDstream.foreachRDD(rdd => println(rdd.collect().mkString("\n") + "aaaaaaa"))

    //增加一个id 用于保存到es的时候进行去重操作
    val alertInfoWithIdDstream: DStream[(String, CouponAlertInfo)] = filteredDstream.map { case (flag, alertInfo) =>
      val period: Long = alertInfo.ts / 1000L / 60L
      val id: String = alertInfo.mid + "_" + period.toString
      (id, alertInfo)

    }

    alertInfoWithIdDstream.foreachRDD { rdd =>
      rdd.foreachPartition { alertInfoWithIdIter =>
       // alertInfoWithIdIter.toList.foreach(println)
        MyEsUtil.insertBulk(GmallConstant.ES_INDEX_COUPON_ALERT, alertInfoWithIdIter.toList)
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }

}
