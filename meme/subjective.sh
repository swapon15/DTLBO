#!/bin/bash
for i in *.out.stat
do
 cut -f3 -d ' ' $i > ./$i.bstonly
done

paste ./*.bstonly >result.all
