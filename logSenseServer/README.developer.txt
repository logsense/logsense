First time installation
-----------------------

Download Solr from the apache website. untar it.

let $SOLR_PARENT_DIR be the directory solr-4.4.0/ 

Edit local.properties file and provide full path to this directory. Then run ant build.xml -- with target as forceCopy which is the current default:

Or you can create yourName.properties and run ant this way:
ant -Dbuild.env=yourName build.xml

Updating your Solr installation
--------------------------------

For future builds, run ant with target as "copy" 

You will find the logSenseSolr directory under $SOLR_PARENT_DIR


The build script does the following. 

1. 
copy $SOLR_PARENT_DIR/example to $SOLR_PARENT_DIR/logSenseSolr

let $SOLR_DIR = $SOLR_PARENT_DIR/logSenseSolr

2.
unzip $SOLR_DIR/webapps/solr.war to $SOLR_DIR/solr-webapp/webapp

3.
copy the following:

img/*  ->  $SOLR_DIR/solr-webapp/webapp/img/

solrConf/*  -> $SOLR_DIR/solr/collection1/conf/


--

the ant target forceCopy will replace the original solr config files, even if they are newer
use it first. 

for second time onwards, use the ant target "copy"

we can remove the dependency between copy and prepare -- this will eliminate
the unzip step.
