#! /usr/bin/perl
use 5.010;
use open qw(:locale);
use strict;
use utf8;
use warnings qw(all);

# periodically, clean old log data from the server. This will keep the server spiffy and responsive.  

my ($epochTime, $machine, $hrsAgo, $yourDomain);

if ($#ARGV < 3) {
    say "arg count is $#ARGV";
        die "Usage:  epochTime ohio-logsense00n hrsAgo yourDomain" ;
} else {
        $epochTime = $ARGV[0];
        $machine = $ARGV[1];
        $hrsAgo = $ARGV[2];
        $yourDomain = $ARGV[3];  # e.g., google.com or amazon.com or alphabet.xyz . 
}

# wait until epoch time
&waitUntilEpoch($epochTime);
my ($curlCmd) = &getDeleteUrl($machine, $hrsAgo, $yourDomain);

say "$curlCmd \n";

my $status = system($curlCmd);
say "status = $status \n";

# if epoch time is 30
# wait until 1/2 hour epoch

sub waitUntilEpoch {

	my ($epochTime) = @_;

	# if current minute is 10, we wait for 20 more minutes
	#  10 -> 20
	#  20 -> 10
	#  30 -> 0
	#  40 -> 50
	#  50 -> 40
	#  59 -> 31
	#  0  -> 30
	#  1  -> 29
	# 
	# if min <= 30
	# 	30 - min 
	# else
	#	60 - min + 30

	say "epochTime = $epochTime\n";

	my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime;

	# time to wait in minutes. will convert to seconds later
	my $waitTime;
	if ($min <= $epochTime) {
		$waitTime = $epochTime - $min;
	}
	else {
		$waitTime = 60 - $min + $epochTime;
	}
	say "waitTime= $waitTime\n";

	sleep($waitTime*60);

	say "epochTime = $epochTime \n ";

}

sub getDeleteUrl {
	my ($machine, $hrsAgo, $yourDomain) = @_;

	my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime;

	$mon = $mon+1;

	# 3 hrs ago. if currentHr < hrsAgo, date = date-1, and hr = 24 - (hrsAgo - currentHr)

	if ($hour < $hrsAgo) {
		$mday = $mday-1;
		$hour = 24 - ($hrsAgo - $hour);
	} 
	else {
		$hour = $hour - $hrsAgo;
	}


	# $year contains the number of years since 1900. To get a 4-digit year add 1900
	$year += 1900;
	$year = sprintf("%04d", $year );
	$mon = sprintf("%02d", $mon );
	$mday = sprintf("%02d", $mday );
	$hour = sprintf("%02d", $hour );

# curl http://ohio-logsense002.yourdomain.com:8983/solr/update/?commit=true -H "Content-Type: text/xml" -d
#  "<delete><query>(date:[* TO 2013-12-02T05:00:00Z])</query></delete>"

	my $delQuery = join ('', '"<delete><query>(date:[* TO ', $year, '-', $mon, '-', $mday, 'T', $hour, ':00:00Z])</query></delete>"');

	my $curlCmd = join('', 'curl http://', $machine, '.$yourDomain:8983/solr/update/?commit=true -H "Content-Type: text/xml" -d ', $delQuery);


	my @result= ($curlCmd);
	return @result;

}



