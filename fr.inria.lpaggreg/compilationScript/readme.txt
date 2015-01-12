This script automatically compiles the lpaggreg library for the following platform: linux x86 and x86_64, and windows x86 and x86_64. It then automatically copies each genretated file to into its respective project in Ocelotl. The windows versions requires the headers for jni (usually installed with a version of JDK) and mingw-w64 (can be installed through the depot of Ubuntu).

This repository contains:
	* build_lpaggreg.sh: the script to call to compile the library
	* linux: contains the scripts for compiling the linux versions of the library
	* win: contains the scripts for compiling the windows versions of the library
	* readme.txt: this file

#Setup:
Before using the script, you must edit five variables in the build_lpaggreg.sh file:
	* LPAGGREG_PROJECT_DIR: the path to the git project of the lpaggreg library
	* LPAGGREGJNI_PROJECT_DIR: the path to the git project of the JNI version the lpaggreg library
	* JNI_INCLUDE_DIR: the path to the jni.h header 
	* OCELOTL_PROJECT_DIR: the path to the git project of Ocelotl
