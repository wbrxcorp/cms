package com.walbrix.cms

trait AppConfigSupport extends org.scalatra.ScalatraServlet {
  def appConfig:com.typesafe.config.Config = servletContext.getAttribute(AppConfigSupport.APPCONFIG_ATTR_NAME).asInstanceOf[com.typesafe.config.Config]
}

object AppConfigSupport {
  val APPCONFIG_ATTR_NAME = "com.walbrix.cms.app_config"

  def init(context:javax.servlet.ServletContext):Unit = {
    context.setAttribute(APPCONFIG_ATTR_NAME, edu.gatech.gtri.typesafeconfigextensions.forwebapps.WebappConfigs.webappConfigFactory(context).load)
  }
}
