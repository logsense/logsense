# when starting a cluster, start the secondary instances using this script. change logsense1... to your current primary
#
java -Xms128m -Xmx1024m -DzkHost=172.29.44.67:9983 -jar start.jar
