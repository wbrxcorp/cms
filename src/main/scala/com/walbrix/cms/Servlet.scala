package com.walbrix.cms

import java.io.{File, FileNotFoundException}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import scala.collection.JavaConverters._
import org.apache.commons.io.IOUtils

class Servlet extends HttpServlet {

  var prefix:Option[String] = _
  var config:com.typesafe.config.Config = _
  var documentPath:File = _
  var resourceLocator:com.hubspot.jinjava.loader.ResourceLocator = _
  val yamlFrontMatterExtension = com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension.create()

  val mimeTypes = Map(
    "css"->"text/css",
    "csv"->"text/comma-separated-values",
    "doc"->"application/msword",
    "docx"->"application/msword",
    "eps"->"application/postscript",
    "gif"->"image/gif",
    "gz"->"application/gzip",
    "htm"->"text/html",
    "html"->"text/html",
    "ico"->"image/x-icon",
    "jar"->"application/java-archive",
    "java"->"text/plain",
    "jpeg"->"image/jpeg",
    "jpg"->"image/jpeg",
    "js"->"application/x-javascript",
    "mp3"->"audio/mpeg",
    "ogg"->"application/ogg",
    "pdf"->"application/pdf",
    "png"->"image/png",
    "ppt"->"application/vnd.ms-powerpoint",
    "pptx"->"application/vnd.ms-powerpoint",
    "ps"->"application/postscript",
    "svg"->"image/svg+xml",
    "swf"->"application/x-shockwave-flash",
    "tar"->"application/x-tar",
    "tgz"->"application/x-gtar",
    "tif"->"image/tiff",
    "tiff"->"image/tiff",
    "txt"->"text/plain",
    "wav"->"audio/x-wav",
    "xhtml"->"application/xhtml+xml",
    "xls"->"application/vnd.ms-excel",
    "xlsx"->"application/vnd.ms-excel",
    "xml"->"application/xml",
    "xsl"->"application/xml",
    "zip"->"application/zip"
  )

  def using[A <: { def close():Unit },B]( resource:A )( f:A => B ) = try(f(resource)) finally(resource.close)

  override def init:Unit = {
    // prefix
    prefix = Option(getServletContext.getInitParameter(s"${getServletName}.prefix"))
    config = edu.gatech.gtri.typesafeconfigextensions.forwebapps.WebappConfigs.webappConfigFactory(this.getServletContext).load
    documentPath = new File(config.getString("cms.document_path"))
    resourceLocator = new com.hubspot.jinjava.loader.FileLocator(new java.io.File(config.getString("cms.template_path")))
  }

  private def produceCacheControlHeaders(file:File, response:HttpServletResponse):Unit = {
    // TBD
    // http://blog.redbox.ne.jp/http-header-tuning.html
  }

  private def isCacheValid(file:File, request:HttpServletRequest):Boolean = {
    // TBD
    false
  }

  private def serveStaticFile(file:File, response:HttpServletResponse):Unit = {
    val suffix = file.getName.replaceFirst("^.*\\.(.+)$", "$1")
    produceCacheControlHeaders(file, response)
    val contentType = mimeTypes.get(suffix).getOrElse("text/plain")
    response.setContentType(contentType)
    if (contentType.startsWith("text/")) response.setCharacterEncoding("UTF-8")
    using(response.getOutputStream) { out =>
      using(new java.io.FileInputStream(file))(IOUtils.copy(_, response.getOutputStream))
    }
  }

  private def serveMarkdownAsHtml(markdownFile:File, response:HttpServletResponse):Unit = {
    val options = new com.vladsch.flexmark.util.options.MutableDataSet()
    val extensions = new java.util.HashSet[com.vladsch.flexmark.Extension]()
    extensions.add(yamlFrontMatterExtension)
    options.setFrom(com.vladsch.flexmark.parser.ParserEmulationProfile.GITHUB_DOC)
    val parser = com.vladsch.flexmark.parser.Parser.builder(options).extensions(extensions).build
    val document = parser.parse(IOUtils.toString(new java.io.FileInputStream(markdownFile), "UTF-8"))
    val visitor = new com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor()
    visitor.visit(document)
    val params = visitor.getData.asScala.map { case (key, value) =>
      (key, value.asScala.toSeq match {
        case Seq(x) => x
        case x => x.asJava
      })
    }.toMap

    val content = com.vladsch.flexmark.html.HtmlRenderer.builder(options).build.render(document)

    val jinjava = new com.hubspot.jinjava.Jinjava()
    jinjava.setResourceLocator(resourceLocator)
    val template = params.get("template").getOrElse("default.html")
    val renderedContent = jinjava.render(s"""{% include "${template}" %}""", (params + ("content"->content)).asJava)
    produceCacheControlHeaders(markdownFile, response)
    response.setContentType("text/html")
    response.setCharacterEncoding("UTF-8")
    using(response.getWriter)(_.write(renderedContent))
  }

  override protected def doGet(req:HttpServletRequest, res:HttpServletResponse):Unit = {
    try {
      if (!documentPath.isDirectory) throw new FileNotFoundException(s"Document path ${documentPath} not found")
      // else
      val vhostName = prefix.getOrElse(req.getServerName)
      val requestURI = req.getRequestURI
      val vhostDir = new File(documentPath, vhostName)
      val requested = new File(if (vhostDir.isDirectory) vhostDir else documentPath, requestURI) match {
        case x:File if x.isDirectory => new File(x, "index.html")
        case x:File => x
      }

      if (requested.isFile) {
        if (isCacheValid(requested, req)) {
          res.setStatus(HttpServletResponse.SC_NOT_MODIFIED)
        } else {
          serveStaticFile(requested, res)
        }
        return
      }

      if (requested.getName.endsWith(".html")) {
        val requestedMd = new File(requested.getParentFile, requested.getName.replaceFirst("\\.html$", ".md"))
        if (requestedMd.isFile) {
          if (isCacheValid(requestedMd, req)) {
            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED)
          } else {
            serveMarkdownAsHtml(requestedMd, res)
          }
          return
        }
      }
      // else
      res.sendError(HttpServletResponse.SC_NOT_FOUND, s"${requestURI} not found.")
    }
    catch {
      case e:FileNotFoundException => res.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage)
    }
  }
}
