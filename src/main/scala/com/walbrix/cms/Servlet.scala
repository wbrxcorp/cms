package com.walbrix.cms

import java.io.{File, FileNotFoundException}
import javax.servlet.http.{HttpServletResponse, HttpServletRequest, HttpServlet}
import scala.collection.JavaConverters._
import org.apache.commons.io.IOUtils

class Servlet extends HttpServlet {

  var rootPrefix:Option[String] = _
  var config:com.typesafe.config.Config = _
  var documentPath:File = _
  var resourceLocator:com.hubspot.jinjava.loader.ResourceLocator = _
  val flexmarkOptions = new com.vladsch.flexmark.util.options.MutableDataSet()
  flexmarkOptions.setFrom(com.vladsch.flexmark.parser.ParserEmulationProfile.GITHUB_DOC)
  val flexmarkExtensions = Set(
    com.vladsch.flexmark.ext.yaml.front.matter.YamlFrontMatterExtension.create(),
    com.vladsch.flexmark.ext.tables.TablesExtension.create(),
    com.vladsch.flexmark.ext.definition.DefinitionExtension.create()
  ).asJava

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

  val datePattern = org.joda.time.format.DateTimeFormat.forPattern("yyyy-MM-dd")
  val scalateEngine = new org.fusesource.scalate.TemplateEngine


  def using[A <: { def close():Unit },B]( resource:A )( f:A => B ) = try(f(resource)) finally(resource.close)

  override def init:Unit = {
    // prefix
    rootPrefix = Option(getServletContext.getInitParameter(s"${getServletName}.prefix"))
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

  private def parseMarkdownFile(markdownFile:File):com.vladsch.flexmark.ast.Node = {
    val parser = com.vladsch.flexmark.parser.Parser.builder(flexmarkOptions).extensions(flexmarkExtensions).build
    using(new java.io.FileInputStream(markdownFile)) { is => parser.parse(IOUtils.toString(is, "UTF-8")) }
  }

  private def extractDocumentAttributes(document:com.vladsch.flexmark.ast.Node, useJavaTypes:Boolean = true):Map[String,AnyRef] = {
    val visitor = new com.vladsch.flexmark.ext.yaml.front.matter.AbstractYamlFrontMatterVisitor()
    visitor.visit(document)
    //pprint.pprintln(visitor.getData)
    visitor.getData.asScala.map { case (key, value) =>
      (key, value.asScala match {
        case Seq(x:String) if key == "date" => datePattern.parseDateTime(x).toDate
        case Seq(x) => x
        case x => if (useJavaTypes) x.asJava else x
      })
    }.toMap
  }

  private def serveSsp(name:String, fileToServe:File, markdownFile:Option[File], request:HttpServletRequest, response:HttpServletResponse):Unit = {
    val documentAttributes = markdownFile.map { file =>
      extractDocumentAttributes(parseMarkdownFile(file), false)
    }.getOrElse(Map())
    val renderedContent = scalateEngine.layout(org.fusesource.scalate.TemplateSource.fromFile(fileToServe), documentAttributes ++ Map (
      "request"->request,
      "name"->name
    ))
    response.setContentType(documentAttributes.get("contentType").getOrElse("text/html").toString)
    response.setCharacterEncoding("UTF-8")
    using(response.getWriter)(_.write(renderedContent))
  }

  private def serveMarkdownAsHtml(name:String, markdownFile:File, request:HttpServletRequest, response:HttpServletResponse):Unit = {
    // TODO: read prefix attributes
    val document = parseMarkdownFile(markdownFile)
    val content = com.vladsch.flexmark.html.HtmlRenderer.builder(flexmarkOptions).extensions(flexmarkExtensions).build.render(document)

    val attributes = extractDocumentAttributes(document) ++ Map (
      "request"->request,
      "content"->content,
      "name"->name
    ) ++ (Option(request.getCookies) match {
      case Some(cookies) => Map("cookies" -> cookies.map { cookie => (cookie.getName, cookie ) }.toMap.asJava)
      case None => Map()
    })

    // redirect_toが指定されていれば無条件でリダイレクトする
    attributes.get("redirect_permanent").foreach { url =>
      if (url.isInstanceOf[String]) {
        response.setStatus(301);
        response.setHeader( "Location", url.asInstanceOf[String] );
        response.setHeader( "Connection", "close" );
      } else {
        response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "redirect url must be a string")
      }
      return
    }

    attributes.get("redirect").foreach { url =>
      if (url.isInstanceOf[String]) {
        response.sendRedirect(url.asInstanceOf[String])
      } else {
        response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "redirect url must be a string")
      }
      return
    }

    val jinjava = new com.hubspot.jinjava.Jinjava()
    jinjava.setResourceLocator(resourceLocator)
    val template = attributes.get("template").getOrElse("default.html")
    val renderedContent = jinjava.render(s"""{% include "${template}" %}""", attributes.asJava)
    //produceCacheControlHeaders(markdownFile, response)
    response.setContentType("text/html")
    response.setCharacterEncoding("UTF-8")
    using(response.getWriter)(_.write(renderedContent))
  }

  private def determineNameAndFile(req:HttpServletRequest, res:HttpServletResponse):Option[(String, File)] = {
    if (!documentPath.isDirectory) throw new FileNotFoundException(s"Document path ${documentPath} not found")

    // else
    val requestURI = req.getRequestURI
    val suggestedRootPrefix = rootPrefix.getOrElse(req.getServerName)

    // nameを確定する
    val givenName = (if (new File(documentPath, suggestedRootPrefix).isDirectory) { // ルートプレフィクス候補に該当するディレクトリがあるか？
      new File(suggestedRootPrefix, requestURI)
    } else {
      new File(requestURI)
    }).getPath

    val name = if (new File(documentPath, givenName).isDirectory) {
      if (!requestURI.endsWith("/")) {
        // ディレクトリに対するアクセスの場合で、/ で終端していない場合は終端させるようリダイレクトする
        res.sendRedirect(s"${req.getRequestURL}/")
        return None
      }
      Seq("index.ssp", "index.html").map(new File(givenName, _).getPath).filter(name=>name == "index.html" || name.endsWith("/index.html") || new File(documentPath, name).isFile).headOption.getOrElse(throw new SecurityException("Forbidden"))
    } else {
      givenName
    }

    Some(name, new File(documentPath, name))

  }

  override protected def doGet(req:HttpServletRequest, res:HttpServletResponse):Unit = {
    try {
      val (name, fileToServe) = determineNameAndFile(req, res).getOrElse(return) // 内部ドキュメント名と実ファイルオブジェクトを決定する。リダイレクトの場合はそのままリターン

      if (fileToServe.isFile) {
        if (name.endsWith(".ssp")) { // Scala Server Pagesの処理
          val mdFileToServe = Option(new File(fileToServe.getPath.replaceFirst("\\.ssp$", ".md"))).filter(_.isFile)

          serveSsp(name, fileToServe, mdFileToServe, req, res)
        }
        else if (isCacheValid(fileToServe, req)) { // static contents
          res.setStatus(HttpServletResponse.SC_NOT_MODIFIED)
        } else {
          serveStaticFile(fileToServe, res)
        }
        return
      }

      if (name.endsWith(".html")) { // 存在しない.htmlファイルにリクエストには代わりに対応するMarkdownファイルをHTML変換して返す
        val mdFileToServe = new File(fileToServe.getPath.replaceFirst("\\.html$", ".md"))
        if (mdFileToServe.isFile) {
          serveMarkdownAsHtml(name, mdFileToServe, req, res)
          return
        }
      }
      // else
      res.sendError(HttpServletResponse.SC_NOT_FOUND, s"${req.getRequestURI} not found.")
    }
    catch {
      case e:FileNotFoundException => res.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage)
      case e:SecurityException => res.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage)
    }
  }

  override protected def doPost(req:HttpServletRequest, res:HttpServletResponse):Unit = {
    try {
      val (name, fileToServe) = determineNameAndFile(req, res).getOrElse(return) // 内部ドキュメント名と実ファイルオブジェクトを決定する。リダイレクトの場合はそのままリターン
      if (!fileToServe.isFile) throw new FileNotFoundException()
      if (!name.endsWith(".ssp")) { //
        res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED) // ssp以外はPOST禁止
        return
      }
      //else
      val mdFileToServe = Option(new File(fileToServe.getPath.replaceFirst("\\.ssp$", ".md"))).filter(_.isFile)
      serveSsp(name, fileToServe, mdFileToServe, req, res)
    }
    catch {
      case e:FileNotFoundException => res.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage)
      case e:SecurityException => res.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage)
    }
  }
}
