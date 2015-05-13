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

SCRIPT="../../soctrace-inria.github.io/updatesite/change_version.sh"
MASTER="."
FEATURE="../fr.inria.soctrace.tools.ocelotl.feature/feature.xml"
CATEGORY="../fr.inria.soctrace.tools.ocelotl.maven.repository/category.xml"

# parameter check is done in the change_version.sh script
NEW=$1
QUALIFIER=$2
$SCRIPT $MASTER $FEATURE $CATEGORY $NEW

