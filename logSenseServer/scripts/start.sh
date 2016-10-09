# this script will start the LogSense server instance in isolation (simple mode, no clustering). reduce the max jvm size if it gets in the way of your Tomcat instance
java -Xms128m -Xmx1024m -jar start.jar
