#!/bin/bash
                                                                                        
#####################################################################
# Change the version number of Ocelotl.
# 
# This is simply a convenience shortcut for the change_version.sh
# script.
#
# IMPORTANT
#
# It works having the Ocelotl and the soctrace-inria.github.io 
# clones in the same root directory:
# ./somedir/ocelotl
# ./somedir/soctrace-inria.github.io
#
# Original Author: Generoso Pagano
# Adaptation to Ocelotl : Damien Dosimont
#####################################################################

MASTER="."
FEATURE="../fr.inria.soctrace.tools.ocelotl.feature/feature.xml"
CATEGORY="../fr.inria.soctrace.tools.ocelotl.maven.repository/category.xml"

# Usage help function
function usage() {
    echo "usage: ./change_version.sh <maven master> <feature> <category> <new version>"
    echo "<maven master>: absolute path of the maven master project folder"
    echo "<feature>: absolute path of the project feature.xml"
    echo "<category>: absolute path of the project update site category.xml"
    echo "<new version>: new version in the format x.y.z (where x,y,z are positive integers)"
}                                     

# Return 0 if the params are OK, 1 otherwise
function check_params() {                    
    
    # Parameter check            
    if [ $# -lt 1 ]; then
	return 1
    fi

    NEW=$1

    if [ ! -d "$MASTER" ]; then
	echo "Error: directory $MASTER does not exist."
	return 1
    fi

    if [ ! -f "$FEATURE" ]; then
	echo "Error: file $FEATURE does not exist."
	return 1
    fi

    if [ ! -f "$CATEGORY" ]; then
	echo "Error: file $CATEGORY does not exist."
	return 1
    fi

    if [[ ! $NEW =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
	echo "Wrong version format: $NEW"
	return 1
    fi

    return 0
}

function print_params() {
    echo "MASTER: $MASTER"
    echo "FEATURE: $FEATURE"
    echo "CATEGORY: $CATEGORY"
    echo "VERSION: $NEW"
}

function main() {

    BV="Bundle-Version:.*"
    NBV="Bundle-Version: ${NEW}"

    OLDDIR=`pwd`
    cd $MASTER

    echo "Updating MANIFEST.MF in all plugins"
    find .. -wholename "*META-INF/MANIFEST.MF" | xargs sed -i s/"$BV"/"$NBV"/

    echo "Updating ocelotl feature.xml"
    sed -i /xml/!s/"version=\".*.*.*\""/"version=\"${NEW}\""/ $FEATURE 

    echo "Updating repository category.xml"
    sed -i s/"\_.*.*.*.jar"/"\_${NEW}.jar"/ $CATEGORY
    sed -i /xml/!s/"version=\".*.*.*\""/"version=\"${NEW}\""/ $CATEGORY

    echo "Update pom.xml in all modules"
    mvn versions:set -DnewVersion="${NEW}" -DgenerateBackupPoms=false

    cd $OLDDIR
}

# ENTRY POINT

check_params $@
if [ $? -ne 0 ]; then
    usage
    exit
fi

print_params

main


