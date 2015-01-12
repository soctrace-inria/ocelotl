#!/bin/bash

ARCH=$1
BASE="buildLpaggreg"
LPAGGREG_SRC_DIR=$2
LPAGGREGJNI_SRC_DIR=$3

if [ "$ARCH" = "32" ]; then
  BUILD="build_linux_x86"
  SCRIPT="makelin32"
else 
  BUILD="build_linux_x64"
  SCRIPT="makelin64"
fi

mkdir -p $BASE/tmp/
mkdir -p $BASE/$BUILD/

LPA_NEWREP=$BASE/tmp/lpaggreg_`eval date +%Y%m%d%H%M%S`
LPAJNI_NEWREP=$BASE/tmp/lpaggregjni_`eval date +%Y%m%d%H%M%S`

cp -R $LPAGGREG_SRC_DIR $LPA_NEWREP
cp -R $LPAGGREGJNI_SRC_DIR $LPAJNI_NEWREP

cp $BASE/$SCRIPT/subdir_lpa.mk $LPA_NEWREP/Shared/src/subdir.mk
cp $BASE/$SCRIPT/subdir_lpa.mk $LPA_NEWREP/Static/src/subdir.mk
cp $BASE/$SCRIPT/makefile_lpa_shared $LPA_NEWREP/Shared/makefile

cd $LPA_NEWREP

echo "Building liblpaggreg.so..."
make clean
make 
cp Shared/liblpaggreg.so.3.1 ../../$BUILD/

echo "Building liblpaggreg.a..."
make clean
make static
cp Static/liblpaggreg.a.3.1 ../../$BUILD/liblpaggreg.a

cd ../../..
cp $BASE/$SCRIPT/subdir_lpajni.mk $LPAJNI_NEWREP/Release/src/subdir.mk
cp $BASE/$SCRIPT/makefile_jni $LPAJNI_NEWREP/Release/makefile
cd $LPAJNI_NEWREP

echo "Building liblpaggregjni.so..."
make clean
make
cp Release/liblpaggregjni.so.3.1 ../../$BUILD/liblpaggregjni.so

cd ../..

echo "Cleaning temp files..."
rm -Rf tmp


