#!/bin/bash

BASE="buildLpaggreg"

#Import configuration
source config

LPAGGREG_PROJECT_DIR=lpaggreg
LPAGGREGJNI_PROJECT_DIR=lpaggregjni
LPAGGREGJNI_PROJECT_DIR=external/$LPAGGREGJNI_PROJECT_DIR
LPAGGREG_PROJECT_DIR=$LPAGGREGJNI_PROJECT_DIR/external/$LPAGGREG_PROJECT_DIR

cd $OCELOTL_PROJECT_DIR
OCELOTL_PROJECT_DIR=`pwd`
cd -
cd $LPAGGREGJNI_PROJECT_DIR
LPAGGREGJNI_PROJECT_DIR=`pwd`
make clean
cd -
cd $LPAGGREG_PROJECT_DIR
LPAGGREG_PROJECT_DIR=`pwd`
make clean
make static-linux
make static-linux-x86
make static-win64
make static-win32
echo "TARGET_PACKAGE=fr.inria.lpaggreg.jni" > $LPAGGREGJNI_PROJECT_DIR/configuration
./install.sh static-linux $LPAGGREGJNI_PROJECT_DIR/otl-linux $LPAGGREGJNI_PROJECT_DIR/otl-linux
./install.sh static-linux-x86 $LPAGGREGJNI_PROJECT_DIR/otl-linux-x86 $LPAGGREGJNI_PROJECT_DIR/otl-linux-x86
./install.sh static-win64 $LPAGGREGJNI_PROJECT_DIR/otl-win64 $LPAGGREGJNI_PROJECT_DIR/otl-win64
./install.sh static-win32 $LPAGGREGJNI_PROJECT_DIR/otl-win32 $LPAGGREGJNI_PROJECT_DIR/otl-win32
cd $LPAGGREGJNI_PROJECT_DIR
LPAGGREGJNI_PROJECT_DIR=`pwd`
make otl-linux
make otl-linux-x86
make otl-win64
make otl-win32
cp otl-linux/liblpaggregjni.so $OCELOTL_PROJECT_DIR/fr.inria.lpaggreg.linux_x64/liblpaggregjni.so
cp otl-linux-x86/liblpaggregjni.so $OCELOTL_PROJECT_DIR/fr.inria.lpaggreg.linux_x86/liblpaggregjni.so
cp otl-win64/lpaggregjni.dll $OCELOTL_PROJECT_DIR/fr.inria.lpaggreg.win64/lpaggregjni.dll
cp otl-win32/lpaggregjni.dll $OCELOTL_PROJECT_DIR/fr.inria.lpaggreg.win32/lpaggregjni.dll

