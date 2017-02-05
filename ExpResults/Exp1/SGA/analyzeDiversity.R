#
# Extract genotypes from final generations
#
function <- processRawFiles(rawFile, generateFile)
{
  divisor <- 3
  file_raw    = fawFile
  file_indivs  = file(generateFile,open="w");
  con          = file(file_raw, open="r")
  line         = readLines(con)
  for (i in 1:length(line)) {
    if (i%%divisor == 0)
      writeLines(line[i], file_indivs)
  }
  close(file_indivs)
  close(con)
}

processRawFiles("sga.raw.out", "sga.indivs.out")    

indivs.SGA <- read.table("sga.indivs.out", header = FALSE)
cat("Mean :", mean(data.matrix(indivs.SGA)), "\n")
cat ("sdev : ", sd(data.matrix(indivs.SGA)), "\n")

