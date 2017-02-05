#!/bin/bash
if [ "$1" == "" ]; then
 echo "You forgot to put generation number"
 exit 1
fi

if [ "$2" == "" ]; then
  echo "You forgot to put #independent trials"
  exit 1
fi

gen=$1
run=$2
indPerGen=100

genMinusOne=$(($gen-1))
totalIndv=$(($run * $indPerGen))
indPerGenMinusOne=$(($indPerGen-1))
b_getting_last_generations() {
x=$(($genMinusOne*157+8))
y=$(($x+149))
echo "$x and $y"
for i in ./*custom.stat
  do
    sed -n -e "$x, $y p" -e "$y q" $i > $i.lastGenonly
    head -n300 $i.lastGenonly>$i.candionly
  done
cat *candionly>candi.out
rm *only
}

r_take_individuals_from_candi() {
cat >candi_individuals.R<<END
   file_test    = "candi.out"
   file_result  = file("temp_result_candi",open="w");
   con          = file(file_test, open="r")
   line         = readLines(con)
   for (i in 1:length(line)) {
      if (i%%3 == 0)
         writeLines(line[i], file_result)
   }
   close(file_result)  
f = read.table("temp_result_candi")
k <- transform(f, new.col=abs(V1))
write.table(k, "candi_gene_and_difference", row.names = F, col.names = F)    
END
}

b_candi_xy_difference() {
cut -f1 -d ' ' candi_gene_and_difference>candi_xy_difference
a=1
b=0;
while [ $a -lt $totalIndv ];do
   let b=a+$indPerGenMinusOne
   sed  -n  "$a,$b p" candi_xy_difference>$a.only
   let a=a+$indPerGen   
done
paste *.only>candi_xy_difference_merged
rm *.only
}

r_candi_xy_mean_difference() {
cat>candi_xy_mean_difference.R<<END
f = read.table("candi_xy_difference_merged")
k = rowMeans(f, na.rm = TRUE)
write.table(k, "candi_xy_mean_difference_by_run", row.names=F, col.names=F)
END
}
r_show_dispersionStat() {
	cat > results.R <<EOF
        library(matrixStats)
        candi = read.table("candi_xy_mean_difference_by_run")
      
        meanCandi = colMeans(candi)
       
        sigmaCandi = colSds(data.matrix(candi, rownames.force=NA), na.rm=TRUE)
       
       
        cat("\n Candidate Mean :", meanCandi)
        cat("\n Candidate Sigma :", sigmaCandi)
        
EOF
}       
b_getting_last_generations 
r_take_individuals_from_candi

module add apps/R/3.1.2
R -q -e 'source("candi_individuals.R")'
b_candi_xy_difference
r_candi_xy_mean_difference
r_show_dispersionStat
R -q -e 'source("candi_xy_mean_difference.R")' 
R -q -e 'source("results.R")'

