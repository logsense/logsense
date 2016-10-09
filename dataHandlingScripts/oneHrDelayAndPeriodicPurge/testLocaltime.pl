#! /usr/bin/perl
use 5.010;
use open qw(:locale);
use strict;
use utf8;
use warnings qw(all);

# test how localtime outputs year.

my ($sec,$min,$hour,$mday,$mon,$year,$wday,$yday,$isdst) = localtime;


# $year contains the number of years since 1900. To get a 4-digit year add 1900
$year += 1900;
$year = sprintf("%04d", $year );
        
say "year= $year \n";
