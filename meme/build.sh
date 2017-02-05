#!/bin/bash

# TODO verify that 
# - we are given 2 parameters
# - these parameters are absolute paths; i.e. start with '/'
# - that they both exist as folders
# if any of this fails, display usage and quit
if [ ! -d $1 ] 
then 
    echo "Sources folder does not exist"
    exit 1
fi 

if [ ! -d $2 ] 
then 
    echo "Experiment folder does not exist"
    exit 1
fi 

# First parameter is the folder where we find what we need to run the experiment
# i.e. ECJ source tree, commons-math...
DIRSOURCES=$1
#typically this would be in /trunk/ExperimentalResults/2016-ICTAI/sources/ in our repository

# 2nd parameter is the folder where the experiment will run 
DIREXP=$2

DIRECJ="$DIRSOURCES/ecj/ec/"
DIRTLBO="$DIRSOURCES/meme/"
# this is the list of temporary files we produce during this script
# each separated by a space e.g. "one two three"
# these are removed at the end 
TMPLIST=""
DIRTMP=".codebase"



# we move to the experiment folder and will move back to our cwd at the end of the script
DIRSTART=`pwd`
cd $DIREXP

# removing results from previous runs
rm *.jar *.stat 
rm -rf $DIRTMP  



# we move into the temporary build folder 

mkdir $DIRTMP
cd $DIRTMP
cp -r $DIRTLBO ./
cp -r $DIRECJ ./

mv ./meme ./ec/ 

# building
javac -cp . ./ec/meme/*.java 
if [ $? != 0 ] 
then 
	echo "Take another look to the source..."
	exit
fi

 
# building ECJ.jar 
jar cfm ${DIREXP}/.codebase/ECJ.jar ./ec/meme/manifest.mf ./ec/*.class ./ec/*/*.class ./ec/*/*/*.class 

# done in the temporary build folder, going back to DIREXP
cd $DIREXP

# removing all temporary files produced 
if [ ! $TMPLIST = "" ]
then
    rm $TMPLIST
fi

#rm -rf ./.codebase/

# we move back to wherever we were when we started the script
cd $DIRSTART 
