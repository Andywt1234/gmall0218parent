package com.atguigu.bigdata.realtime.bean

/**
  * Created by wt on 2019-07-20 11:23
  */
case class StartUpLog(mid:String,
                      uid:String,
                      appid:String,
                      area:String,
                      os:String,
                      ch:String,
                      logType:String,
                      vs:String,
                      var logDate:String,
                      var logHour:String,
                      var ts:Long
                     ) {

}
