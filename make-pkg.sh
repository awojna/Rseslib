#!/bin/bash

########################################################################
#
# Script preparing library, sources and Weka package of Rseslib
#
# 1. Update Rseslib version in pom.xml
# 2. Update Rseslib version, date and URL in Description.props
# 3. Run maven with the goal 'package'
# 4. Run ./make-pkg.sh  <upload working directory> <new rseslib version>
#
########################################################################

basedir=$1
version=$2

rm -rf $basedir
mkdir $basedir

# make library
cp target/rseslib-$version.jar $basedir/

# make sources
git archive --format zip --prefix=rseslib-$version-src/ --output $basedir/rseslib-$version-src.zip master

# make weka package
mkdir $basedir/weka
cp Description.props $basedir/weka/
cp target/rseslib-$version.jar $basedir/weka/rseslib.jar
cd $basedir/weka
zip -r ../Rseslib$version.zip *
