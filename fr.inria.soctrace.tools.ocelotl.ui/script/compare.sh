#!/bin/bash

#Requires to install ImageMagick
#Make sure that no space are present in the directory path  or in the file name

DIR=$1
cd $DIR
echo $DIR
num=0
suffix=.png
noCacheSuffix=_noCache.png
diffSuffix=_showDiff.png

for entry in `ls | grep $noCacheSuffix`; do
    baseName=${entry%$noCacheSuffix}
    cache=$baseName$suffix
    output=$baseName$diffSuffix
    compare $cache $entry $output
done

