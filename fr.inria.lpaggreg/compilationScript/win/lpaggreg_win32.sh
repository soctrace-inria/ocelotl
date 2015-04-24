#!/bin/bash

ARCH=$1
BASE="buildLpaggreg"
LPAGGREG_SRC_DIR=$2/src
LPAGGREGJNI_SRC_DIR=$3/Release/src
JNI_INCLUDE_DIR=$4

if [ "$ARCH" = "32" ]; then
  echo "Compiling for windows x86"
  BUILD="build_win32_x86"
  COMP=i686-w64-mingw32-g++
else 
  echo "Compiling for windows x64"
  BUILD="build_win32_x64"
  COMP=x86_64-w64-mingw32-g++
fi

mkdir -p $BASE/tmp/
mkdir -p $BASE/$BUILD/

LPA_NEWREP=$BASE/tmp/lpaggreg_`eval date +%Y%m%d%H%M%S`
LPAJNI_NEWREP=$BASE/tmp/lpaggregjni_`eval date +%Y%m%d%H%M%S`
cp -R $LPAGGREG_SRC_DIR $LPA_NEWREP
cp -R $LPAGGREGJNI_SRC_DIR $LPAJNI_NEWREP
cp -R $LPAGGREGJNI_SRC_DIR/../.. $LPAJNI_NEWREP



cp --parents $LPAGGREG_SRC_DIR/*.h $LPAJNI_NEWREP/src/

cd $LPAGGREG_SRC_DIR

echo "Building liblpaggreg.a..."
$COMP -O1 -Wall -c *.cpp
ar rs liblpaggreg.a *.o

cp liblpaggreg.a ../../../$LPAJNI_NEWREP/src

cd ../../../$LPAJNI_NEWREP

cd swig
bash ./swig.sh
cd ../src

echo "Building lpaggreg.dll..."
$COMP -c lpaggreg_wrap.cxx -I $JNI_INCLUDE_DIR -I .

$COMP -Wl,-s -shared -Wl,-subsystem,windows -mthreads -Wl,--out-implib,liblpaggreg.a -o lpaggregjni.dll lpaggreg_wrap.o -D_JNI_IMPLEMENTATION_ -Wl,--kill-at -static -Wl,--whole-archive -L . -l lpaggreg -Wl,--no-whole-archive

cp lpaggregjni.dll ../../../$BUILD

cd ../../..

echo "Cleaning temp files..."
rm -R tmp	




