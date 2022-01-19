package com.bb.monitor

import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import io.swagger.models.Swagger
import io.swagger.parser.Swagger20Parser
import okhttp3.{OkHttpClient, Request}

object RouteHolder {

  private val map = new util.HashMap[String, Swagger]()

  /**
    * 获取所有在注册中心的路由信息，key 是服务名，value 是 swagger
    *
    * @return
    */
  def getRoute() = {

    if (map.size() == 0) {
      val rss = getString("http://ip:port/eureka/apps")
      val objectMapper = new ObjectMapper
      val jsonNode = objectMapper.readTree(rss)
      val apps = jsonNode.get("applications").get("application").asInstanceOf[ArrayNode]
      val sp = new Swagger20Parser
      apps.forEach(x => {
        val name = x.get("name").asText().toUpperCase
        if (name != "***") {
          val url = if (name == "**") x.get("instance").get(0).get("homePageUrl").asText() + "v2/api-docs?group=api" else x.get("instance").get(0).get("homePageUrl").asText() + "v2/api-docs"
          val rs = getString(url)
          val swagger = sp.parse(rs)
          map.put(name, swagger)
        }
      })
    }

    map
  }

  private def getString(url: String, contentType: String = "application/json") = {

    val ok = new OkHttpClient
    val request = new Request.Builder()
      .addHeader("Content-Type", contentType)
      .addHeader("Accept", contentType)
      .url(url)
      .get()
      .build()
    val res = ok.newCall(request).execute()
    res.body().string()
  }
}
