# 
#Processing stat files from SimpleShortStatistics
#

files <- list.files(pattern = "*.out.stat")
fileCount <- length(files)
count <- fileCount - 1;
df <- data.frame(row.names = 1)
for (i in 0: count)
{
  f1 <- "job."
  f1 <- paste(f1,i,sep="")
  f1 <- paste(f1, ".out.stat", sep = "")
  f1 <- gsub('"', '', f1)
  data <- read.table(f1, header = FALSE)
  df <- rbind(df,data[nrow(data) - 1, ])
}
cat("Mean Best: ",mean(df$V3), "Stdv Best : ", sd(df$V3), "\n")
cat("Mean Avg: ",mean(df$V2), "Stdv Avg : ", sd(df$V2))