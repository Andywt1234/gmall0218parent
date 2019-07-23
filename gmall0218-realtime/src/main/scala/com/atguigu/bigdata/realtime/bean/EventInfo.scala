package com.atguigu.bigdata.realtime.bean

/**
  * Created by wt on 2019-07-23 11:21
  */
case class EventInfo(mid:String,
                     uid:String,
                     appid:String,
                     area:String,
                     os:String,
                     ch:String,
                     `type`:String,
                     evid:String ,
                     pgid:String ,
                     npgid:String ,
                     itemid:String,
                     var logDate:String,
                     var logHour:String,
                     var ts:Long
                    )

