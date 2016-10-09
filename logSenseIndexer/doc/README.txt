
This is the LogSense Indexer. You give it a directory or a file in the command line. 

Options.

-d <dir> 
   index all files in <dir>

    
   -d C:/logs/foo will take all log files from the directory C:/logs/foo/

-f <file> 
   index <file>

   
    -f jetty-0.log.ohio-wwwssl40.yourcompany.com.1234.gz 

-i <ignoreFiles.txt>

    ignore log files that are listed in the file ignoreFiles.txt

    

-p <processedFiles.txt>
   add each file that is indexed into this file. 
    


  e.g., -i stress00.txt -p stress00.txt

  i.e., typically, you will have the same file for both -i and -p  so that the next time you run the indexer,
  you start from where you left off.



-t <numRecords>
   when testing, you can limit the number of records per indexed file to this number.

   e.g., -t 10000

-u <serverUrl>
   e.g., http://yourmachine.yourcompany.com:8983

   
Most of these switches are designed to work with the -d <dir> flag. If you are testing with a single file (-f <file>), the -u flag will work, others will be ignored.


