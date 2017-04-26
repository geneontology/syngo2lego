name := "syngo2lego"

organization := "org.geneontology"

version := "0.0.1"

scalaVersion  := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

licenses := Seq("MIT license" -> url("https://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/geneontology/syngo2lego"))

libraryDependencies ++= Seq(
      "org.phenoscape"             %% "scowl"            % "1.3" withJavadoc(),
      "com.propensive"	       %% "rapture"		 % "2.0.0-M7" withJavadoc() 
//     "org.scalactic" %% "scalactic" % "3.0.1",      // Doesn't seem to be compatible with rapture version.  
//      "org.scalatest" %% "scalatest" % "3.0.1"  withJavadoc()  // 2.11.8 may come transitively. Test.
)  // scowl should come transitively from brainscowl

// TODO Add code to build.scala to import brainscowl from GitHub.
// https://play.google.com/books/reader?printsec=frontcover&output=reader&id=xVU2AAAAQBAJ&pg=GBS.PT906

initialCommands := "import org.geneontology.syngo2lego._"

