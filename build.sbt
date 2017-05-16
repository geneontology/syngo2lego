name := "syngo2lego"

organization := "org.geneontology"

version := "0.0.1"

scalaVersion  := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

licenses := Seq("MIT license" -> url("https://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/geneontology/syngo2lego"))

libraryDependencies ++= Seq(
	"org.slf4j" % "slf4j-api" % "1.7.5",
    "org.slf4j" % "slf4j-simple" % "1.7.5",
  	"org.backuity.clist" %% "clist-core"   % "3.2.2",
  	"org.backuity.clist" %% "clist-macros" % "3.2.2" % "provided",
    "org.phenoscape"             %% "scowl"            % "1.3" withJavadoc(),
    "com.propensive"	       %% "rapture"		 % "2.0.0-M7" withJavadoc() 
//     "org.scalactic" %% "scalactic" % "3.0.1",      // Doesn't seem to be compatible with rapture version.  
//      "org.scalatest" %% "scalatest" % "3.0.1"  withJavadoc()  // 2.11.8 may come transitively. Test.
)  

// DONE Add code to build.scala to import brainscowl from GitHub.
// https://play.google.com/books/reader?printsec=frontcover&output=reader&id=xVU2AAAAQBAJ&pg=GBS.PT906

initialCommands := "import org.geneontology.syngo2lego._"

