#!/bin/bash
echo n,m,T
for i in `seq 1 2000 12001`
do
  j=0
  if [[ $i -gt 2 ]];
  then
    j=$(($i-1))
  else 
    j=$i
  fi
  for k in `seq 1 10 51`
  do
      l=0
      if [[ $k -gt 2 ]];
      then
        l=$(($k-1))
      else 
        l=$k
      fi
      java -jar build/libs/StageAdapt-0.1-all.jar -gS -n $j -m $l
   done
done
