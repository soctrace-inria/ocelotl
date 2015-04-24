This script automatically compiles the lpaggreg library for the following platform: linux x86 and x86_64, and windows x86 and x86_64. It then automatically copies each generated file to its respective project in Ocelotl. The windows versions require the headers for jni (usually installed with a version of JDK) and mingw-w64.

This repository contains:
	* build.sh: the script to call to compile the library
	* readme.txt: this file

#Setup:
Before using the script, you must edit four variables in the build_lpaggreg.sh file:
	* LPAGGREG_PROJECT_DIR: the path to the git project of the lpaggreg library
	* LPAGGREGJNI_PROJECT_DIR: the path to the git project of the JNI version the lpaggreg library
	* JNI_INCLUDE_DIR: the path to the jni.h header 
	* OCELOTL_PROJECT_DIR: the path to project of Ocelotl
