#!/bin/bash
ecj_data_processing() {
  for i in ./*.out.stat
    do
     cut -f3 -d ' ' $i > ./$i.bstonly  
     #-f says which field you want to extract, -d says what is the field delimeter that is used in the input file 
    done
  paste ./*.out.stat.bstonly > result.bst.all
  rm *.stat.bstonly
}
ecj_data_processing
