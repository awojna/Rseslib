#!/bin/bash

########################################################################
#
# Script preparing sources, release package and Weka package of Rseslib
#
# 1. Update Rseslib version in src/main/java/rseslib/qmak/UI/QAboutDialog.java
# 2. Update Rseslib version in src/main/java/rseslib/simplegrid/common/Communication.java
# 3. Update Rseslib version in pom.xml
# 4. Update Rseslib version, date and URL in Description.props
# 5. Run maven with the goal 'package'
# 6. Run ./make-pkg.sh <new rseslib version>
#
########################################################################

version=$1

rm -rf target/rseslib-$version
rm -rf target/weka
rm -rf target/rseslib-$version-src.zip
rm -rf target/rseslib-$version.zip
rm -rf target/rseslib-$version-weka.zip

# make sources
git archive --format zip --prefix=rseslib-$version-src/ --output target/rseslib-$version-src.zip master

# prepare release package
mkdir target/rseslib-$version
cp target/weka-stable-3.8.6.jar target/rseslib-$version/weka.jar
cp target/jcommon-0.9.6.jar target/rseslib-$version/
cp target/jfreechart-0.9.21.jar target/rseslib-$version/
cp qmak.config target/rseslib-$version/
cp scripts/* target/rseslib-$version/
cp -R data target/rseslib-$version/

# prepare weka package
mkdir target/weka
cp Description.props target/weka/

# make release package
cd target
cp rseslib-$version.jar rseslib-$version/rseslib.jar
zip -r rseslib-$version.zip rseslib-$version

# make weka package
cp rseslib-$version.jar weka/rseslib.jar
cd weka
zip -r ../rseslib-$version-weka.zip *
