# LogSense Root Cause Analyzer

LogSense is an affordable (free) log analysis tool. It was used extensively at the Fortune 1 company to troubleshoot issues, including bugs, performance bottlenecks, and security events.

It helped resolved over 30 separate incidents in the first few months of deployment. 

You can use it for scenarious such as the following:

* when you release new code;
* when new capacity is added;
* when seriously high traffic is expected;
* when a serious problem is happening on your website;
* when a suspected fraud has been perpetrated;
* during performance testing -- you will identify the root cause for issues quickly.
* during QA testing -- not only will your QA team quickly  identify the issues, it will help you communicate the issues more efficiently to your development team.
* during development -- it will assist software engineers debug their systems, including debugging complex interactions with 3rd party systems.


## Example things you can do with LogSense

#### Thread related calls

Inspect the log for a given user session across multiple requests, including what happened at various backend servers.

Inspect the details of a specific user request, including database calls, search calls, and other backend calls, ordered in the correct logical sequence regardless of how it appeared in the logs. Connect various logs together from different backend and frontend servers using associative mapping.

Essentially, handle hierarchies in your logs.

#### Exceptions

Find all exceptions in your live server logs in the past 10 minutes.
Constrain them by log source, data center, specific set of machines, etc.
Skip those that you already know about, or if they are not of current interest. Save these as your preference for future search.

If a NullPointerException (or some other exception) is caused by different things, find all unique issues with one click.

#### High Benches
Inspect the histogram if time taken by various end-to-end calls or subsystems within -- e.g., in 100msec buckets.
Drill down this histogram for higher benches and inspect the corresponding logs.

#### Inspect specific subsystems
Find all calls to a caching layer or a data layer. Constrain it by end-to-end calls that take longer. Drill down for details.

#### Inspect potential fraud
If a customer is logged on over concurrent user sessions from different browser types, are they trying to game your system? Have they found a vulnerability in your caching code, perhaps? You can zoom into these sessions, with data from your various backend servers, with just a few clicks.

#### Compare performance over time as your code evolves

Generate statistical data on the number of sql calls for each url. Compare these over time as your code evolves so you know when and if there are additional sql calls.

Compare different server instances. Apply machine learning to automatically identify anomalies, and inspect them further with search. For example, a patch may have been applied to a subset of servers, or a license may have  expired.

You will still need your expertise to solve your problem. LogDr will help you focus on your problem.

## Get It For Yourself

There are currently three parts to the system, a backend server, an indexer, and agentless scripts to pull data from remote servers. You start the backend server on your Linux box (or Windows/Mac). When you wish to index a new log file, call the indexer. 


The agentless script will let you index logs on remote machines live, without having to install any agent on those machines. To run it, you will need to grab the corresponding perl dependencies from CPAN (www.cpan.org). Also check out Perlbrew -- it provides a convenient way to manage your perl libraries. 

The indexer will take a directory or a file. You can also save the files you have already processed, and skip them on next invocation. Please see logSense/doc/README.txt for Indexer options.

Steps to run the server are in logSenseSolr/doc/README.txt

If you are interested in developing the server, please see logSenseSolr/README.developer.txt

Currently, the indexer handles specific log file formats. One of the things to add is a system to let the user specify parsers via a UI (instead of writing Java code).


The build process are created for Solr 4.x. Since then, newer releases of Solr has changed their file-system structure. Please use Solr 4.x for now. e.g, download from http://archive.apache.org/dist/lucene/solr/4.8.1

## Contributors

LogSense was created by A. Mukherjee, P. G. Shankar, and S-J Lu under the guidance and encouragement of S. Samu, A. Ramaratnam, and G. Patra. Numerous people contributed requirements including developers, Quality Engineering, the Performance team, DevOps, and Business. While the list is large, we wish to especially thank K. Mohan, M. Nehamkin,  R. Vishwanathan, R. Shah, A. Sahoo, V. Giri, V. Solanki and P. Sambandam. Special thanks to P. Newcomb who provided valuable server resources.

## License

LogSense is released under the [MIT License](https://opensource.org/licenses/MIT). Copyright 2026, LogSense.

