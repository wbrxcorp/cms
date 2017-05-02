addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "3.0.1" excludeAll(
  ExclusionRule(organization = "org.mortbay.jetty")
))
addSbtPlugin("org.ensime" % "sbt-ensime" % "1.12.9")
