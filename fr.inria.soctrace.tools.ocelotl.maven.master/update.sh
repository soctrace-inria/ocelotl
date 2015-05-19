#!/bin/bash

for i in framesoc*
do
cd $i
git pull
cd ..
done
for i in ocelotl*
do
cd $i
git pull
cd ..
done
for i in soctrace*
do
cd $i
git pull
cd ..
done
