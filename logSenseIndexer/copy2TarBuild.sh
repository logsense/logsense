#
#
# spedify location of your logSense indexer dir
# e.g., /Users/foo/myProjects/logSenseIndexer
indexerDir=whatever

srcDir=${indexerDir}/target

# destination where the build will go
# e.g.,
# R - logSense1
# Q - logSense2
# W - logSense3
# X - logSense4
# etc



# tarBuild for X
for drive in X 
do

destDrive=/cygdrive/${drive}
cp $srcDir/logSenseIndexer-1.0-SNAPSHOT-jar-with-dependencies.jar ${destDrive}/tarBuild/indexer/.


done

