#!/bin/bash

STARTDIR=`pwd`
cd `dirname $0`

java -jar sftp-cmds-1.0-SNAPSHOT.jar --config $1

#go back to directory where we were started
cd $STARTDIR