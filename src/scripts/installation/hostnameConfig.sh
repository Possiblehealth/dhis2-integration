#!/bin/bash
while read -r line
do
    echo "$line" | grep 'uts' &> /dev/null
    if [ $? == 0 ]; then
      uts_id=`echo "$line" | awk -F" " '{print $4}'`
      nsenter --target $uts_id --uts hostname $1
    fi
done < <(lsns)
