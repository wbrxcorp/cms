package com.walbrix.cms

class InquiryServlet extends org.scalatra.ScalatraServlet with org.scalatra.json.JacksonJsonSupport {
  override protected implicit def jsonFormats: org.json4s.Formats = org.json4s.DefaultFormats.withBigDecimal ++ org.json4s.ext.JodaTimeSerializers.all

  var appConfig:com.typesafe.config.Config = _

  override def initialize(config: javax.servlet.ServletConfig): Unit = {
    super.initialize(config)
    this.appConfig = edu.gatech.gtri.typesafeconfigextensions.forwebapps.WebappConfigs.webappConfigFactory(servletContext).load
  }

  get("/") {
    session.setMaxInactiveInterval(3600 * 24) // Anti-XSRFに使いたいので JSESSIONIDクッキーを発生させるためだけの操作
    Map()
  }

  post("/") {
    // TODO: JSESSIONIDを使ってXSRF対策
    // TODO: プログラムで短時間に大量POSTしようとする厨二病くさい奴を弾く
    val email = new org.apache.commons.mail.SimpleEmail()
    email.setHostName(appConfig.getString("smtp.host"))
    email.setSmtpPort(appConfig.getInt("smtp.port"))
    email.setFrom(appConfig.getString("inquiry.from"))
    email.setSubject(appConfig.getString("inquiry.subject"))
    email.setMsg(org.json4s.jackson.JsonMethods.pretty(parsedBody))
    email.addTo(appConfig.getString("inquiry.recipient"))
    email.setCharset("ISO-2022-JP")
    email.send
    Map("success"->true)
  }
}
