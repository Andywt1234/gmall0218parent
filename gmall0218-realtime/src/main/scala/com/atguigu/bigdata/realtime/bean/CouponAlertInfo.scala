package com.atguigu.bigdata.realtime.bean

/**
  * Created by wt on 2019-07-23 11:38
  */
case class CouponAlertInfo(mid:String,
                           uids:java.util.HashSet[String],
                           itemIds:java.util.HashSet[String],
                           events:java.util.List[String],
                           ts:Long)  {

}

