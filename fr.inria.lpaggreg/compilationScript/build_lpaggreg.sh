#!/bin/bash

BASE="buildLpaggreg"
LPAGGREG_PROJECT_DIR=../lpaggreg
LPAGGREGJNI_PROJECT_DIR=../lpaggregjni
JNI_INCLUDE_DIR=/usr/local/include/
OCELOTL_PROJECT_DIR=/home/youenn/tmp/ocelotl

linux/lpaggreg_linux.sh 64 $LPAGGREG_PROJECT_DIR $LPAGGREGJNI_PROJECT_DIR
cp $BASE/build_linux_x64/liblpaggregjni.so $OCELOTL_PROJECT_DIR/fr.inria.lpaggreg.linux_x64/liblpaggregjni.so

linux/lpaggreg_linux.sh 32 $LPAGGREG_PROJECT_DIR $LPAGGREGJNI_PROJECT_DIR
cp $BASE/build_linux_x86/liblpaggregjni.so $OCELOTL_PROJECT_DIR/fr.inria.lpaggreg.linux_x86/liblpaggregjni.so

win/lpaggreg_win32.sh 64 $LPAGGREG_PROJECT_DIR $LPAGGREGJNI_PROJECT_DIR $JNI_INCLUDE_DIR
cp $BASE/build_win32_x64/lpaggregjni.dll $OCELOTL_PROJECT_DIR/fr.inria.lpaggreg.win64/lpaggregjni.dll

win/lpaggreg_win32.sh 32 $LPAGGREG_PROJECT_DIR $LPAGGREGJNI_PROJECT_DIR $JNI_INCLUDE_DIR
cp $BASE/build_win32_x86/lpaggregjni.dll $OCELOTL_PROJECT_DIR/fr.inria.lpaggreg.win32/lpaggregjni.dll
