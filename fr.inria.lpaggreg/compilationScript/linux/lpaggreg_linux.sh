#!/bin/bash

ARCH=$1
BASE="buildLpaggreg"
LPAGGREG_SRC_DIR=$2
LPAGGREGJNI_SRC_DIR=$3

if [ "$ARCH" = "32" ]; then
  BUILD="32"
else 
  BUILD=""
fi

mkdir -p $BASE/build_linux_x$ARCH/

cd $LPAGGREG_SRC_DIR

echo "Building liblpaggreg.a..."
make clean
make static$BUILD
cp Static$BUILD/liblpaggreg.a.3.1 ../../$BASE/build_linux_x$ARCH/liblpaggreg.a

cd -

cd $LPAGGREGJNI_SRC_DIR

echo "Building liblpaggregjni.so..."
make clean
make ocelotl$BUILD
cp Ocelotl$BUILD/liblpaggregjni.so.3.1 ../../$BASE/build_linux_x$ARCH/liblpaggregjni.so

cd -

echo "Cleaning temp files..."
rm -Rf tmp

