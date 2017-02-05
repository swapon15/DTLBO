#!/bin/bash
read -p 'How many generations in each run? :' GENERATION
read -p 'How many independent runs? : ' TRIAL
read -p 'Which algorithmic data you are processing(SCA, P1, P2N5, P2N25)? :' ALGORITHM
read -p 'How many individuals in each population? : ' INDIVPERPOP

DIVISOR=3
TOTALPOPS=2
MULTIPLIERPERIND=3

GENMINUSONE=$(($GENERATION-1))
TOTALFINALINDV=$(($TRIAL * $INDIVPERPOP))
INDMINUSONE=$(($INDIVPERPOP-1))
HEADERLINES=11
if [ "$ALGORITHM" != "SCA"  ]; then
 MULTIPLIERPERIND=5
 DIVISOR=5
 HEADERLINES=15
fi
LINESPERGEN=$(($MULTIPLIERPERIND*$INDIVPERPOP*$TOTALPOPS + $HEADERLINES ))
echo $LINESPERGEN
START=$(($LINESPERGEN * $GENMINUSONE + $HEADERLINES + 1))
echo $START
END=$(($START + $LINESPERGEN - $HEADERLINES - 1))
echo $END
LINESPERPOP=$(($MULTIPLIERPERIND*$INDIVPERPOP))

b_getting_last_generations() {
for i in ./*custom.stat
  do
    sed -n -e "$START, $END p" -e "$END q" $i > $i.lastGenonly
    head -n${LINESPERPOP} $i.lastGenonly>$i.testonly
    m=$(($LINESPERPOP + 1))
    n=$(($LINESPERPOP * $TOTALPOPS))    
    sed -n -e "$m, $n p" -e "$n q" $i.lastGenonly > $i.candionly
  done
cat *candionly>candi.out
cat *testonly>test.out
rm *only
}

r_take_individuals_from_test() {
cat >test_individuals.R<<END
   file_test    = "test.out"
   file_result  = file("temp_result",open="w");
   con          = file(file_test, open="r")
   line         = readLines(con)
   for (i in 1:length(line)) {
      if (i%%$DIVISOR == 0)
         writeLines(line[i], file_result)
   }
   close(file_result)  
f = read.table("temp_result")
vector = c()
for (i in 1:nrow(f)) {
  v <- c(abs(f[i,1]-f[i,2]), abs(f[i,1]-f[i,3]), abs(f[i,1]-f[i,4]), abs(f[i,2]-f[i,3]), abs(f[i,2]-f[i,4]), abs(f[i,3]-f[i,4]))
   vector[i] <- v[which.max(v)]   
}
k <- transform(f, new.col=vector)
write.table(k, "test_gene_and_difference", row.names = F, col.names = F)    
END
}

b_test_xy_difference() {
cut -f5 -d ' ' test_gene_and_difference>test_xy_difference
a=1
b=0;

while [ $a -lt $TOTALFINALINDV ];do
   let b=a+$INDMINUSONE
   sed  -n  "$a,$b p" test_xy_difference>$a.only
   let a=a+$INDIVPERPOP   
done
paste *.only>test_xy_difference_merged
rm *.only
}

r_test_xy_mean_difference() {
cat>test_xy_mean_difference.R<<END
f = read.table("test_xy_difference_merged")
k = rowMeans(f, na.rm = TRUE)
write.table(k, "test_xy_mean_difference_by_run", row.names=F, col.names=F)
END
}

r_take_individuals_from_candi() {
cat >candi_individuals.R<<END
   file_test    = "candi.out"
   file_result  = file("temp_result_candi",open="w");
   con          = file(file_test, open="r")
   line         = readLines(con)
   for (i in 1:length(line)) {
      if (i%%$DIVISOR == 0)
         writeLines(line[i], file_result)
   }
   close(file_result)  
f = read.table("temp_result_candi")
vector = c()
for (i in 1:nrow(f)) {
  v <- c(abs(f[i,1]-f[i,2]), abs(f[i,1]-f[i,3]), abs(f[i,1]-f[i,4]), abs(f[i,2]-f[i,3]), abs(f[i,2]-f[i,4]), abs(f[i,3]-f[i,4]))
   vector[i] <- v[which.max(v)]   
}
k <- transform(f, new.col=vector)
write.table(k, "candi_gene_and_difference", row.names = F, col.names = F)    
END
}

b_candi_xy_difference() {
cut -f5 -d ' ' candi_gene_and_difference>candi_xy_difference
a=1
b=0;
while [ $a -lt $TOTALFINALINDV ];do
   let b=a+$INDMINUSONE
   sed  -n  "$a,$b p" candi_xy_difference>$a.only
   let a=a+$INDIVPERPOP   
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
        test  = read.table("test_xy_mean_difference_by_run")
        meanCandi = colMeans(candi)
        meanTest  = colMeans(test)
        sigmaCandi = colSds(data.matrix(candi, rownames.force=NA), na.rm=TRUE)
        sigmaTest = colSds(data.matrix(test, rownames.force=NA), na.rm=TRUE)
       
        cat("\n Candidate Mean :", meanCandi)
        cat("\n Candidate Sigma :", sigmaCandi)
        cat ("\n Test Mean :", meanTest)
        cat ("\n Test Sigma :", sigmaTest)
EOF
}       
b_getting_last_generations 
r_take_individuals_from_test
r_take_individuals_from_candi

module add apps/R/3.1.2
R -q -e 'source("test_individuals.R")'
R -q -e 'source("candi_individuals.R")'

b_test_xy_difference
r_test_xy_mean_difference

b_candi_xy_difference
r_candi_xy_mean_difference
r_show_dispersionStat
R -q -e 'source("test_xy_mean_difference.R")'
R -q -e 'source("candi_xy_mean_difference.R")' 
R -q -e 'source("results.R")'

