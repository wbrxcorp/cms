class ScalatraBootstrap extends org.scalatra.LifeCycle {

  override def init(context: javax.servlet.ServletContext):Unit=  {
    com.walbrix.cms.AppConfigSupport.init(context)
    context.mount(classOf[com.walbrix.cms.OptOutServlet], "/optout/*")
    context.mount(classOf[com.walbrix.cms.InquiryServlet], "/inquiry/*")
  }
}
