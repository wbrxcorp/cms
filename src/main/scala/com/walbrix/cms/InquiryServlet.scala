package com.walbrix.cms

class InquiryServlet extends org.scalatra.ScalatraServlet with org.scalatra.json.JacksonJsonSupport {
  override protected implicit def jsonFormats: org.json4s.Formats = org.json4s.DefaultFormats.withBigDecimal ++ org.json4s.ext.JodaTimeSerializers.all

  get("/") {
    session.setMaxInactiveInterval(3600 * 24) // JSESSIONIDクッキーをブラウザに流すためだけの操作
    Map("success"->true)
  }

  post("/") {
    // TODO: JSESSIONIDを使ってXSRF対策
    // TODO: プログラムで短時間に大量POSTしようとする厨二病くさい奴を弾く
    // TODO:
    // TODO: JSONをメールする
    println(parsedBody)
    Map("success"->true)
  }
}
