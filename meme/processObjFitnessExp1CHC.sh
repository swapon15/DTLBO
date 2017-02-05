#!/bin/bash

if [ "$1" == "" ]; then
 echo "You forgot to put generation number"
 exit 1
fi

if [ "$2" == "" ]; then
  echo "You forgot to put #independent trials"
  exit 1
fi
totalBest=$(($1 * $2))
gen=$1
run=$2
genMinusOne=$(($gen-1))

b_removing_best_run_lines() {
for i in ./*out.stat
  do 
    numLines=$(cat $i | wc -l)      
    neededLines=$((numLines-6))        
    head -n${neededLines} $i > $i.only
  done
cat *.only>merged.out
rm *.only
}

r_take_genes_from_mergedout() {
cat >objective_fitness.R<<END
   file_merged    = "merged.out"
   file_result  = file("temp_result",open="w");
   con          = file(file_merged, open="r")
   line         = readLines(con)
   for (i in 1:length(line)) {
      if (i%%7 == 0)
         writeLines(line[i], file_result)
   }
   close(file_result)  

   f = read.table("temp_result")
   k = rowMeans(f, na.rm=TRUE)* 1
   write.table(k,"obj_fit", row.names=F, col.names=F)
   
END
}

b_splitting_by_run() {
a=1
b=0;
while [ $a -lt $totalBest ];do
   let b=a+$genMinusOne
   sed  -n  "$a,$b p" obj_fit>$a.only
   let a=a+$gen   
done
 paste *.only>bst.candi.all

#paste finalmu_only sigma_only>mu-sigma
rm *.only
}

generate_bst_final() {
cat > gen_bst_final.R <<EOF
f = read.table("bst.candi.all")
k = rowMeans(f, na.rm = TRUE)
write.table(k, "bst.candi.final", row.names = F, col.names = F)
EOF
}

g_plot() {
cat > plot.gnu <<EOF
set style data lines
set xlabel "Evolutionary Time"
set ylabel "Gene value (Optimal = 2.0)"
set key font ", 15"
set xlabel font ",20"
set ylabel font ",20"
set title font ",20"
set xtics font ",20"
set ytics font ",20"
set title "Genotype convergence for TLBO"
plot "bst.candi.final" using 1 title "candidate solution"
pause mouse key "..."
set term png
set output "Genotype convergence for TLBO"
replot

EOF
}
generate_RScript() {
   cat > results.R <<EOF
        library(matrixStats)
        candi = read.table("bst.candi.final")   
        muCandi = colMeans(candi)
        sigmaCandi = colSds(data.matrix(candi,rownames.force=NA), na.rm=TRUE)
        cat("\n Mean Candidate", muCandi)
        cat("\n Sigma Candidate",sigmaCandi)         
EOF
}

b_removing_best_run_lines 
r_take_genes_from_mergedout
module add apps/R/3.1.2
R -q -e 'source("objective_fitness.R")'
b_splitting_by_run 


generate_bst_final
R -q -e 'source("gen_bst_final.R")'

g_plot
gnuplot < plot.gnu
generate_RScript
R -q -e 'source("results.R")'

