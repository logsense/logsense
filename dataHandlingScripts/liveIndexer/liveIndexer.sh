#!/bin/bash 
if [ $# = 0 ];
then
   echo "liveIndexer.sh remotehost "; exit -1
fi

# change this for your system
server=http://ohio-logsense000.yourDomain.com:8983

echo logsense server=$server, indexing $1

#provide a filename so the indexer knows the meta data -- machine, logType, datacenter etc
# e.g.,
fileName=jetty.log.$1.1234567890

# using passwords like below is not safe. the shell command can be seen by "ps." use key based login if available.
read -p "Enter User: " myuser
read -s -p "Enter Password for $myuser: " mypassword
COUNTER=0
while [  $COUNTER -lt 500 ]; do
   date_stamp=`date +"%Y-%m-%d-%H:%M:%S"`
   echo The counter is $COUNTER $date_stamp
   # perl  remotessh.pl $1 $myuser $mypassword 
   perl  liveIndexer.pl $1 $myuser $mypassword | java -jar logSense-1.0-SNAPSHOT-jar-with-dependencies.jar  -u $server -stdin -f $fileName
   let COUNTER=COUNTER+1 
   sleep 10;

   echo logsense server=$server, indexing $1

done
