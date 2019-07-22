package com.atguigu.bigdata.realtime

import com.atguigu.bigdata.realtime.util.MyKafkaUtil
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by wt on 2019-07-20 17:30
  */
object KAFKACONSUMER {
  def main(args: Array[String]): Unit = {


    val conf = new SparkConf().setMaster("local[*]").setAppName("gyh")
    val ssc = new StreamingContext(conf, Seconds(5))

    val Dstream: InputDStream[ConsumerRecord[String, String]] = MyKafkaUtil.getKafkaStream("GMALL_EVENT",ssc)
    Dstream.foreachRDD(rdd=>{
      rdd.foreach(x=>{
        println(x.value())
      })
    })
    ssc.start()
    ssc.awaitTermination()
  }


}
