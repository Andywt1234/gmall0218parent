package com.atguigu.bigdata.realtime.app

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by wt on 2019-07-24 10:08
  */
object SaleApp {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("sale_app")
    val ssc = new StreamingContext(conf,Seconds(5))

  }

}
