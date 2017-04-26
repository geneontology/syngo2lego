import sbt._


object MyBuild extends Build 
  { lazy val root = Project ( "root" , file ( "." )) dependsOn 
                  ( brainscowl ) 
                  lazy val brainscowl = RootProject (
                      uri ( "git://github.com/dosumis/brainscowl.git" )) }