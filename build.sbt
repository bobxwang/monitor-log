name := "monitor-log"

version := "0.1"

scalaVersion := "2.12.8"

// https://mvnrepository.com/artifact/commons-io/commons-io
libraryDependencies += "commons-io" % "commons-io" % "2.6"

// https://mvnrepository.com/artifact/io.swagger/swagger-parser
libraryDependencies += ("io.swagger" % "swagger-parser" % "1.0.44")
  .exclude("com.google.guava", "guava")
  .exclude("javax.validation", "validation-api")

// https://mvnrepository.com/artifact/com.squareup.okhttp3/okhttp
libraryDependencies += "com.squareup.okhttp3" % "okhttp" % "3.14.2"

assemblyJarName in assembly := "monitor-log.jar"

// 包重命名
assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("org.apache.commons.io.**" -> "shadeio.@1")
    .inLibrary("commons-io" % "commons-io" % "2.6")
    .inProject
)