
machine=ohio-logsense000
epoch=42
hrsAgo=3
COUNTER=0
yourDomain=foo.com  ## SUPPLY THIS!

while [  $COUNTER -lt 500 ]; do

	echo clearing old data for $machine epoch=$epoch hrsAgo=$hrsAgo

	perl cleanOldData.pl $epoch $machine $hrsAgo $yourDomain

	sleep 120;

    let COUNTER=COUNTER+1

done

