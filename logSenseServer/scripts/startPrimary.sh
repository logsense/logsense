# when starting a cluster, execute this script first
# note: this file was created originally on windows, and may
# have invisible windows characters. At the time, did not set
# git to change windows cr/lf. if this doesn't work, please
# type this command on the command line on your linux box
# or enter it into a shell script locally. don't copy/paste, else you will get the
# invisible chars
java -Xms128m -Xmx1024m -Dbootstrap_confdir=./solr/collection1/conf -Dcollection.configName=myconf -DzkRun -DnumShards=7 -jar start.jar
