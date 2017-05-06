package com.walbrix.cms

class OptOutServlet extends org.scalatra.ScalatraServlet {
  get("/") {
    val cookie = new javax.servlet.http.Cookie("optout", "yes")
    cookie.setMaxAge(365 * 24 * 3600)
    cookie.setPath("/")
    response.addCookie(cookie)
    redirect(params.getOrElse("redirectTo", "/"))
  }
}
