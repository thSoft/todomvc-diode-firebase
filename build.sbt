enablePlugins(ScalaJSPlugin)

name := "Diode React TodoMVC"

scalaVersion := "2.11.8"

workbenchSettings

bootSnippet := "TodoMVCApp().main();"

testFrameworks += new TestFramework("utest.runner.Framework")

emitSourceMaps := true

/* create javascript launcher. Searches for an object extends JSApp */
persistLauncher := true

val diodeVersion = "0.6.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.0",
  "com.github.japgolly.scalajs-react" %%% "core" % "0.11.1",
  "com.github.japgolly.scalajs-react" %%% "extra" % "0.11.1",
  "me.chrons" %%% "diode" % diodeVersion,
  "me.chrons" %%% "diode-devtools" % diodeVersion,
  "me.chrons" %%% "diode-react" % diodeVersion,
  "hu.thsoft" %%% "firebase-scalajs" % "2.4.1",
  "com.lihaoyi" %%% "upickle" % "0.3.8"
)
EclipseKeys.withSource := true

jsDependencies ++= Seq(
  "org.webjars.bower" % "react" % "15.1.0" / "react-with-addons.js" commonJSName "React" minified "react-with-addons.min.js",
  "org.webjars.bower" % "react" % "15.1.0" / "react-dom.js" commonJSName "ReactDOM" minified "react-dom.min.js" dependsOn "react-with-addons.js"
)
