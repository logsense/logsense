#! /usr/bin/perl
use 5.010;
use open qw(:locale);
use strict;
use utf8;
use warnings qw(all);
use Net::OpenSSH ;

my ($remote_host, $account, $pass, $logFileName);
if ($#ARGV < 2) {
    say "arg count is $#ARGV";
	die "specify host account password"
} else {
	$remote_host = $ARGV[0];
	$account = $ARGV[1];
	$pass = $ARGV[2];
}
$remote_host ||= '10.100.100.100';  # change this to your default server
$account ||= 'yourUserName';
$pass ||= 'yourDefaultPass'; # DON'T put your real password here!

$logFileName='/var/log/jetty/jetty.log'; # change this as appropriate for your system

my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime;
alarm((60-$min)*60);
# alarm(10);

$SIG{ALRM} = sub { 
   say "time to switch log file - $hour:$min";
   my $cleanup = Net::OpenSSH->new($remote_host, master_opts => [-o => "StrictHostKeyChecking=no"], user => $account, password => $pass );
   $cleanup->system('pgrep tail | xargs -L1 kill -9');       
   #abandon ship
   exit -1;
};

my ($pipe, $pid);
my $ssh = Net::OpenSSH->new($remote_host, master_opts => [-o => "StrictHostKeyChecking=no"], user => $account, password => $pass );
#################YOUR REMOTE COMMAND LINE#######################
my $cmd=join(' ', 'tail -f ', $logFileName);

($pipe, $pid) = $ssh->pipe_out($cmd);
while(<$pipe>) {
     print $_;
}
close $pipe;

exit;

