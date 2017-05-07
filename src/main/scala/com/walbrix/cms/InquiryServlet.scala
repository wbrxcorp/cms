package com.walbrix.cms

class InquiryServlet extends org.scalatra.ScalatraServlet with org.scalatra.json.JacksonJsonSupport with AppConfigSupport {
  override protected implicit def jsonFormats: org.json4s.Formats = org.json4s.DefaultFormats.withBigDecimal ++ org.json4s.ext.JodaTimeSerializers.all

  get("/") {
    val cookie = new javax.servlet.http.Cookie("XSRF-TOKEN", session.getId) // セッションIDをAnti-XSRFトークンとして使う
    response.addCookie(cookie)
    Map()
  }

  post("/") {
    // XSRF対策
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
