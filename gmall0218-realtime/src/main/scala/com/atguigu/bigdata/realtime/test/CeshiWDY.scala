package com.atguigu.bigdata.realtime.test


/**
  * Created by wt on 2019-07-24 20:16
  */
object CeshiWDY {
  def main(args: Array[String]): Unit = {

    val list = List(1, 2, 3)
    val ints: List[Int] = list :+ 4
    println(list)
    println(ints)


    val arr = Array(1, 2, 3, 4, 4, 5, 5)
    val arr2 = arr :+ 6
    for (elem <- arr2) {
      println(elem)
    }


  }

}
