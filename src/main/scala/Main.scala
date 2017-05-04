object Main extends App {
  val root = new org.eclipse.jetty.webapp.WebAppContext
  root.setBaseResource(new org.eclipse.jetty.util.resource.ResourceCollection(Array("src/main/webapp")))
  root.setContextPath("/")
  args.headOption.foreach(root.setInitParameter("cms.prefix", _))
  root.setClassLoader(new java.net.URLClassLoader(new Array[java.net.URL](0), this.getClass().getClassLoader()))
  root.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false")

  val port = 57264
  val server = new org.eclipse.jetty.server.Server(port)
  server.setHandler(root)
  server.start

  println(s"http://localhost:${port}/")
  println("Type :quit to quit")

  val repl = new scala.tools.nsc.interpreter.ILoop()
  val settings = new scala.tools.nsc.Settings
  settings.classpath.value = s".:${System.getProperty("sbt-classpath")}"
  repl.process(settings)

  server.stop
}
