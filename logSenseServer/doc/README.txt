
0.
Install the server -- see installation.txt for instructions.

1.
To start the server:

cd logSenseSolr
java -Xms128m -Xmx1800m -jar start.jar

if you have a 64bit jvm, use larger values. e.g., -Xmx4096m -- but you also need RAM for that.
If you are running your Tomcat instance on the same machine, you may need to reduce it.

When your data gets big, you will need this.


Check that the server is running:
http://yourmachine:8983/solr/browse

Replace yourmachine with your machine's hostname or IP address.

2.
You add new data to the server by calling the Indexer (separate jar file). Instructions for that are provided separately.
