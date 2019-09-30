@ECHO OFF
SET TEST_CURRENT_DIR=%CLASSPATH%
if "%TEST_CURRENT_DIR%" == "%CLASSPATH%" ( SET CLASSPATH=.;F:\JavaEE\antlr\antlr-4.7.2-complete.jar;%CLASSPATH% )
java org.antlr.v4.gui.TestRig %*