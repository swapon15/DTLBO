#
# Extract genotypes from final generations
#
divisor <- 3
file_test    = "test.out"
file_result  = file("temp_result",open="w");
con          = file(file_test, open="r")
line         = readLines(con)
for (i in 1:length(line)) {
  if (i%%divisor == 0)
    writeLines(line[i], file_result)
}
close(file_result)    
close(con)
df <- read.table("temp_result", header = FALSE)
cat("Mean :", mean(data.matrix(df)))
cat ("sdev : ", sd(data.matrix(df)))