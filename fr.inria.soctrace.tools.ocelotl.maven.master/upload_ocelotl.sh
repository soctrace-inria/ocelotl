#!/bin/bash
                                                                                        
#####################################################################
# Upload Ocelotl update site.
# 
# This is simply a convenience shortcut for the upload-site.sh script.
#
# IMPORTANT
#
# It works having the framesoc and the soctrace-inria.github.io 
# clones in the same root directory:
# ./somedir/ocelotl
# ./somedir/soctrace-inria.github.io
#
# Original Author: Generoso Pagano
# Adaptation to Ocelotl: Damien Dosimont
#####################################################################

SCRIPT="../../soctrace-inria.github.io/updatesite/upload-site.sh"
REPO="../../ocelotl/fr.inria.soctrace.tools.ocelotl.maven.repository/target/repository/"
PROJECT="ocelotl"
$SCRIPT $REPO $PROJECT
