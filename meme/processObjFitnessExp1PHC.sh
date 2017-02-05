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
    neededLines=$((numLines-10))        
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
      if (i%%11 == 0)
         writeLines(line[i], file_result)
   }
   close(file_result)  

   f = read.table("temp_result")
   k = rowMeans(f, na.rm=TRUE)*2
   write.table(k,"obj_fit", row.names=F, col.names=F)
   
END
}

b_mu_sigma_seperate() {
a=1
b=0;
while [ $a -lt $totalBest ];do
   let b=a+$genMinusOne
   sed  -n  "$a,$b p" temp_result>$a.only
   let a=a+$gen   
done

for i in ./*.only
 do
  cut --fields=1 -d ' ' $i>$i.mu.all
 done
cat *.mu.all>mu.all 

for i in ./*.only
  do
   cut -f2 -d ' ' $i>$i.sigma.all
  done
paste *.sigma.all>ssigma.all

rm *.only
rm *.mu.all
rm *.sigma.all
}

r_mu_sigma_single() {
cat>mu_sigma.R<<END
 t = read.table("mu.all")
 k = rowMeans(t,na.rm=TRUE)
 write.table(k, "test.mu", row.names=F, col.names=F);
 
 f = read.table("ssigma.all");
 k = rowMeans(f, na.rm = TRUE)  
 write.table(k, "sigma_only", row.names = F, col.names = F);
END
}

b_mu_seperate() {
a=1
b=0;
while [ $a -lt $totalBest ];do
   let b=a+$genMinusOne  
   sed  -n  "$a,$b p" test.mu>$a.testmu
   let a=a+$gen   
done
paste *.testmu>finalmu
rm *.testmu
}

b_final_mu() {
cat >finalmu.R<<END
t = read.table("finalmu")
k = rowMeans(t, na.rm=TRUE) 
write.table(k, "finalmu_only", row.names=F, col.names=F)
END
}
b_addFourlines_merged() {
for i in {1..4}
do
 sed -s -i '1i\\' merged.out
done
}
b_splitting_by_run() {
a=1
b=0;
while [ $a -lt $totalBest ];do
   let b=a+$genMinusOne
   sed  -n  "$a,$b p" obj_fit>$a.only
   let a=a+$gen   
done
if [ "$1" = "candi" ]; then
 paste *.only>bst.candi.all
fi

if [ "$1" = "prob" ]; then
 paste *.only>bst.test.all
fi

paste finalmu_only sigma_only>mu-sigma
rm *.only
}

generate_bst_final() {
cat > gen_bst_final.R <<EOF
f = read.table("bst.test.all")
k = rowMeans(f, na.rm = TRUE)
write.table(k, "bst.test.final", row.names = F, col.names = F)

f = read.table("bst.candi.all")
k = rowMeans(f, na.rm = TRUE)
write.table(k, "bst.candi.final", row.names = F, col.names = F)
EOF
}
paste_final(){
paste *.final>plot.final
}
g_plot() {
cat > plot.gnu <<EOF
set style data lines
set xlabel "Evolutionary Time"
set ylabel "Performance (sum of coords)"
set key font ", 15"
set xlabel font ",20"
set ylabel font ",20"
set title font ",20"
set xtics font ",20"
set ytics font ",20"
set title "Generic"
plot "plot.final" using 1 title "candidate",\
"plot.final" using 2 title "test"
pause mouse key "..."
plot "mu-sigma" using 1 title "Learner's genotype (Meu)",\
"mu-sigma" using 2 title "Learner's genotype (Sigma)"
pause mouse key "..."
set term png
set output "Generic"
replot

EOF
}
generate_RScript() {
   cat > results.R <<EOF
        library(matrixStats)
        test = read.table("bst.test.final")   
	candi = read.table("bst.candi.final")   
        muTest = colMeans(test)
        sigmaTest = colSds(data.matrix(test, rownames.force=NA), na.rm=TRUE)
        muCandi = colMeans(candi)
        sigmaCandi = colSds(data.matrix(candi,rownames.force=NA), na.rm=TRUE)
        cat("\n Mean practice problem ", muTest)
        cat("\n Sigma practice problem ",sigmaTest)
        cat("\n Mean Learners", muCandi)
        cat("\n Sigma Learners",sigmaCandi)         
EOF
}

b_removing_best_run_lines 
r_take_genes_from_mergedout
module add apps/R/3.1.2
R -q -e 'source("objective_fitness.R")'
b_mu_sigma_seperate
r_mu_sigma_single
R -q -e 'source("mu_sigma.R")'
b_mu_seperate
b_final_mu
R -q -e 'source("finalmu.R")'
b_splitting_by_run candi 
b_addFourlines_merged
r_take_genes_from_mergedout
R -q -e 'source("objective_fitness.R")'
b_splitting_by_run prob

generate_bst_final
R -q -e 'source("gen_bst_final.R")'
paste_final
g_plot
gnuplot < plot.gnu
generate_RScript
R -q -e 'source("results.R")'

