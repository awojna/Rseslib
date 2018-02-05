#!/bin/bash

########################################################################
#
# Script preparing sources and Weka package of Rseslib
#
# 1. Update Rseslib version, date and URL in Description.props
# 2. Run maven with the goal 'package'
# 3. Run ./make-pkg.sh  <new rseslib version>
#
########################################################################

version=$1

# clean old files
rm -f target/rseslib-$version-src.zip
rm -f target/Rseslib$version.zip
rm -rf target/weka

# prepare sources
git archive --format zip --output target/rseslib-$version-src.zip master src/ data/ COPYING

# prepare weka package
mkdir target/weka
cp Description.props target/weka/
cp target/rseslib-$version.jar target/weka/rseslib.jar
cd target/weka
zip -r ../Rseslib$version.zip *
cd ../..
