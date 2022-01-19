package com.bb.monitor

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object Analy {

  private val pp = """\"([^\"]*)\"""".r

  private val formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH)

  private val ulist = Map("/abcde/" -> "校验请求合法")

  case class Op(mp: java.util.HashMap[String, String], url: String, opertype: String, ip: String, date: LocalDateTime, summery: String, userid: String, usertype: String, refer: String, restime: String, handletime: String, httpstatus: String, servicetime: String, usertime: String, traceid: String, agent: String)

  def analy(line: String) = {

    try {

      val i = line.indexOf("] ")
      val iptime = line.substring(0, i + 1)
      val ipi = iptime.indexOf("[")
      val ip = iptime.substring(0, ipi - 1)
      val date = LocalDateTime.parse(iptime.substring(ipi).replace("[", "").replace("]", ""), formatter)

      val other = line.substring(i + 2)
      val find = pp.findAllIn(other)
      val list = find.toList
      if (list.size == 10) {
        val u = list(0).replace("\"", "").split(" ")
        if ((u(1).startsWith("/XX-gateway") || u(1).startsWith("/XY-gateway") || u(1).startsWith("/ZY-gateway") || u(1).startsWith("/YZ-gateway")) && (u(0).startsWith("GET") || u(0).startsWith("POST"))) {

          val ii = u(1).indexOf("?")
          val iii = if (ii > 0) {
            (u(1).substring(0, ii), u(1).substring(ii + 1))
          } else (u(1), "")
          var url = iii._1
          val urllist = url.split("/")
          val servicename = urllist(2).toUpperCase
          val swagger = RouteHolder.getRoute().get(servicename)

          if (swagger != null) {
            var urlp = urllist.drop(3).mkString("/")
            if (urlp.startsWith("internal/file/")) {
              // restful 风格的 url
              urlp = "internal/file/{key}"
            }
            val path = swagger.getPath(s"/${urlp}")
            if (path != null) {
              val op = u(0) match {
                case "GET" => Some(path.getGet)
                case "POST" => Some(path.getPost)
                case _ => None
              }
              op.foreach(o => {
                val mp = new java.util.HashMap[String, String]()
                if (iii._2 != "") {
                  iii._2.split("&").foreach(s => {
                    val m = s.split("=")
                    mp.put(m(0), m(1))
                  })
                }

                if (urlp == "internal/file/{key}") {
                  mp.put("key", urllist.last)
                  url = urllist.dropRight(1).mkString("/") + "/{key}"
                }

                val ss = mp.getOrDefault("platform", "XXXX")
                val usertype = if (ss == "XX") {
                  "XY用户"
                } else "ZY用户"


                val ops = Op(mp, url, u(0), ip, date, o.getSummary, list(5), usertype, list(1), list(2), list(3), list(4), list(8), list(6), list(7), list(9))
                handle(ops)

                // println(s"${Console.BLUE}${or} ${Console.RESET} --> ${Console.RED} ${usertype}(${oper_userid}) on ${date} in ${ip} has a request ${Console.RESET} ${u(0)} ${url} with param ${mp}, ${service_handle} accepted, handle ${res_time}, the status is ${res_status}, the service time is ${oper_service_time}, the user center time is ${oper_user_time}, traceid is ${oper_traceId}, refer is ${refer}, agent is ${agent}")
              })
            }
          } else {
            val kv = ulist.get("/" + urllist.drop(2).mkString("/"))
            kv.foreach(v => {

              val ops = Op(new java.util.HashMap[String, String](), url, u(0), ip, date, v, list(5), "XX用户", list(1), list(2), list(3), list(4), list(8), list(6), list(7), list(9))

              handle(ops)

              //println(s"${Console.BLUE}${v} ${Console.RESET} --> ${Console.RED}(${oper_userid}) on ${date} in ${ip} has a request ${Console.RESET} ${u(0)} ${url}, ${service_handle} accepted, handle ${res_time}, the status is ${res_status}, the service time is ${oper_service_time}, the user center time is ${oper_user_time}, traceid is ${oper_traceId}, refer is ${refer}, agent is ${agent}")
            })
          }
        }
      }
    } catch {
      case _: Exception =>
    }
  }

  private def handle(op: Op) = {

  }

  def main(args: Array[String]): Unit = {
    val l = """11.32.126.213 [12/Jun/2019:10:42:20 +0800] "GET /xx-gateway/xx-api/request/count?queryCount=1&platform=abcd HTTP/1.1" 200 126 "https://ip.ssss.com/workflow/list" "0.007" "ip:7000" "200" "170320" "0ms" "2a23e90cc14caa3c" "5ms" "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.157 Safari/537.36""""
    analy(l)
  }

}
