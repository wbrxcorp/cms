object Main extends App {
  val root = new org.eclipse.jetty.webapp.WebAppContext
  root.setBaseResource(new org.eclipse.jetty.util.resource.ResourceCollection(Array("src/main/webapp")))
  root.setContextPath("/")
  args.headOption.foreach(root.setInitParameter("cms.prefix", _))
  root.setClassLoader(new java.net.URLClassLoader(new Array[java.net.URL](0), this.getClass().getClassLoader()))
  root.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false")

  val server = new org.eclipse.jetty.server.Server(57264)
  server.setHandler(root)
  server.start
}
