#!/bin/bash

BASE="buildLpaggreg"

#Import configuration
source config

DOWNLOAD=download
LPAGGREG_PROJECT_DIR=lpaggreg
LPAGGREGJNI_PROJECT_DIR=lpaggregjni
LPAGGREG_PROJECT_DIR=$DOWNLOAD/$LPAGGREG_PROJECT_DIR
LPAGGREGJNI_PROJECT_DIR=$DOWNLOAD/$LPAGGREGJNI_PROJECT_DIR

mkdir -p $DOWNLOAD

#Update or clone LPAggreg
if [ -d "$LPAGGREG_PROJECT_DIR" ]; then
	cd $LPAGGREG_PROJECT_DIR
	git pull
	cd -
else
	cd $DOWNLOAD
	git clone $LPAGGREG_GITHUB_DIR
	cd -
fi

#Update or clone LPAggregjni
if [ -d "$LPAGGREGJNI_PROJECT_DIR" ]; then
	cd $LPAGGREGJNI_PROJECT_DIR
	git pull
	cd -
else
	cd $DOWNLOAD
	git clone $LPAGGREGJNI_GITHUB_DIR
	cd -
fi


#Compile linux x86_64
#linux/lpaggreg_linux.sh 64 $LPAGGREG_PROJECT_DIR $LPAGGREGJNI_PROJECT_DIR
#cp $BASE/build_linux_x64/liblpaggregjni.so $OCELOTL_PROJECT_DIR/fr.inria.lpaggreg.linux_x64/liblpaggregjni.so

#Compile linux x86
#linux/lpaggreg_linux.sh 32 $LPAGGREG_PROJECT_DIR $LPAGGREGJNI_PROJECT_DIR
#cp $BASE/build_linux_x32/liblpaggregjni.so $OCELOTL_PROJECT_DIR/fr.inria.lpaggreg.linux_x86/liblpaggregjni.so

#Compile win x86_64
win/lpaggreg_win32.sh 64 $LPAGGREG_PROJECT_DIR $LPAGGREGJNI_PROJECT_DIR $JNI_INCLUDE_DIR
cp $BASE/build_win32_x64/lpaggregjni.dll $OCELOTL_PROJECT_DIR/fr.inria.lpaggreg.win64/lpaggregjni.dll

#Compile linux x86
win/lpaggreg_win32.sh 32 $LPAGGREG_PROJECT_DIR $LPAGGREGJNI_PROJECT_DIR $JNI_INCLUDE_DIR
cp $BASE/build_win32_x86/lpaggregjni.dll $OCELOTL_PROJECT_DIR/fr.inria.lpaggreg.win32/lpaggregjni.dll
