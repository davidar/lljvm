#!/bin/sh

rm -rf tmp && mkdir tmp && cd tmp
mkdir a b
ARCHIVE=../$1*.tar.gz
BASENAME=`basename $ARCHIVE .tar.gz`
tar -zxvf $ARCHIVE -C a
cp -Rv ../$1 b/$BASENAME
diff -ur a b > ../$BASENAME.patch
cd .. && rm -rf tmp
