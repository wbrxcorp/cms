package com.walbrix.cms

class InquiryServlet extends org.scalatra.ScalatraServlet with org.scalatra.json.JacksonJsonSupport {
  override protected implicit def jsonFormats: org.json4s.Formats = org.json4s.DefaultFormats.withBigDecimal ++ org.json4s.ext.JodaTimeSerializers.all

  var appConfig:com.typesafe.config.Config = _

  val XSRF_TOKEN_SESSION_ATTR_NAME = "inquiry-xsrf-token"

  override def initialize(config: javax.servlet.ServletConfig): Unit = {
    super.initialize(config)
    this.appConfig = edu.gatech.gtri.typesafeconfigextensions.forwebapps.WebappConfigs.webappConfigFactory(servletContext).load
  }

  get("/") {
    val cookie = new javax.servlet.http.Cookie("XSRF-TOKEN", session.getId) // セッションIDをXSRFトークンとして使う
    response.addCookie(cookie)
    Map()
  }

  post("/") {
    Option(request.getHeader("X-XSRF-TOKEN")).filter(_.equals(session.getId)).getOrElse(halt(401, """{"info":"Token mismatch"}"""))
    // TODO: プログラムで短時間に大量POSTしようとする厨二病くさい奴を弾く
    val email = new org.apache.commons.mail.SimpleEmail()
    email.setHostName(appConfig.getString("smtp.host"))
    email.setSmtpPort(appConfig.getInt("smtp.port"))
    email.setFrom(appConfig.getString("inquiry.from"))
    email.setSubject(appConfig.getString("inquiry.subject"))
    email.setMsg(org.json4s.jackson.JsonMethods.pretty(parsedBody) + s"\nIPアドレス: ${request.getRemoteAddr}")
    email.addTo(appConfig.getString("inquiry.recipient"))
    email.setCharset("ISO-2022-JP")
    email.send
    Map("success"->true)
  }
}
