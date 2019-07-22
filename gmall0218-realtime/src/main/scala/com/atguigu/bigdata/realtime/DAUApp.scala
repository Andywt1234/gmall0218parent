package com.atguigu.bigdata.realtime

import java.text.SimpleDateFormat
import java.util.Date

import com.alibaba.fastjson.JSON
import com.atguigu.bigdata.realtime.bean.StartUpLog
import com.atguigu.bigdata.realtime.util.{MyKafkaUtil, RedisUtil}
import com.atguigu.util.GmallConstant
import org.apache.hadoop.conf.Configuration
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis
import org.apache.phoenix.spark._
/**
  * Created by wt on 2019-07-20 11:18
  */
object DAUApp {
  def main(args: Array[String]): Unit = {
    val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("gmall2019")
    val ssc = new StreamingContext(sparkConf, Seconds(10))

    val inputStream = MyKafkaUtil.getKafkaStream(GmallConstant.KAFKA_TOPIC_STARUP, ssc)
    // TODO: 1.结构转换为case class并添加了两个字段
    val startUpLongDstream :DStream[StartUpLog]= inputStream.map(record => {
      val jsonString: String = record.value()
      val startUpLog: StartUpLog = JSON.parseObject(jsonString, classOf[StartUpLog])
      val format = new SimpleDateFormat("yyyy-MM-dd HH")
      val dateTimeStr: String = format.format(new Date(startUpLog.ts))
      val dateTimeArray: Array[String] = dateTimeStr.split(" ")
      startUpLog.logDate = dateTimeArray(0)
      startUpLog.logHour = dateTimeArray(1)
      startUpLog
    })
    // TODO: 2.缓存，重复使用 
    startUpLongDstream.cache()
    //打印测试
    /*startUpLongDstream.foreachRDD(rdd=>{
      rdd.foreach(println)
    })*/
    // TODO: 3.去重，根据今天访问过的用户清单进行过滤
    val filteredDstream=startUpLongDstream.transform(rdd=>{
      println("过滤前："+rdd.count())
      val jedis: Jedis = RedisUtil.getJedisClient
      val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
      val dateString: String = dateFormat.format(new Date())
      val key: String = "dau:"+dateString
      val midSet: java.util.Set[String] = jedis.smembers(key)
      println(key)
      println(midSet)
      jedis.close()
      val midBC: Broadcast[java.util.Set[String]] = ssc.sparkContext.broadcast(midSet)
      val filteredRDD: RDD[StartUpLog] = rdd.filter { startupLog =>
        !midBC.value.contains(startupLog.mid)
      }
      println("过滤后："+filteredRDD.count())
      filteredRDD
    })
    //本批次内进行去重
    val distinctDstream: DStream[StartUpLog] = filteredDstream.map(startuplog => (startuplog.mid, startuplog)).groupByKey().flatMap { case (mid, startupLogItr) =>
      startupLogItr.take(1)
    }

    // 问题 ：没有周期性查询redis 而只执行了一次
    //    val jedis: Jedis = RedisUtil.getJedisClient
    //    val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
    //    val dateString: String = dateFormat.format(new Date())
    //    val key: String = "dau:"+  dateString
    //    val midSet: util.Set[String] = jedis.smembers(key)
    //    val midBC: Broadcast[util.Set[String]] = ssc.sparkContext.broadcast(midSet)
    //    startupLogDstream.filter{startupLog=>
    //      !midBC.value.contains(startupLog.mid)
    //    }


    //  问题： 连接 操作jedis次数过多
    //    startupLogDstream.filter{startupLog=>
    //      val jedis: Jedis = RedisUtil.getJedisClient
    //      val key: String = "dau:"+startupLog.logDate
    //      !jedis.sismember(key,startupLog.mid)
    //    }


    // 3 把所有今天访问过的用户保存起来

    distinctDstream.foreachRDD{rdd=>
      rdd.foreachPartition{ startupItr=>   //利用foreachPartition 减少创建连接的次数
        val jedis: Jedis = RedisUtil.getJedisClient
        for (startupLog <- startupItr ) {
          val key: String = "dau:"+startupLog.logDate
          println(key+"quzhi")
          jedis.sadd(key,startupLog.mid)
       //   println(startupLog)
        }
        jedis.close()
      }
    }
    // 4 保存到hbase
    distinctDstream.foreachRDD{rdd=>
      rdd.saveToPhoenix("gmall0281_dau",Seq("MID", "UID", "APPID", "AREA", "OS", "CH", "TYPE", "VS", "LOGDATE", "LOGHOUR", "TS"),new Configuration,Some("hadoop102,hadoop103,hadoop104:2181"))

    }
    ssc.start()
    ssc.awaitTermination()
  }
}
