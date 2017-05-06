class ScalatraBootstrap extends org.scalatra.LifeCycle {

  override def init(context: javax.servlet.ServletContext):Unit=  {
    context mount (classOf[com.walbrix.cms.OptOutServlet], "/optout/*")
  }
}
