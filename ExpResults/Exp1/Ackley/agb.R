rawData.SGA    = read.table("./result.bst.all1",   header=F)
rawData.DE     = read.table("./result.bst.all2", header=F)
rawData.TLBO   = read.table("./result.bst.all3", header=F)

sgaFinal.Gen   =  as.vector(t(rawData.SGA[nrow(rawData.SGA),]))
deFinal.Gen   =  as.vector(t(rawData.DE[nrow(rawData.DE),]))
tlboFinal.Gen  =  as.vector(t(rawData.TLBO[nrow(rawData.TLBO),]))

cat("SGA Mean : ", mean(sgaFinal.Gen), " SGA stdev : ", sd(sgaFinal.Gen), "\n")
cat("DE Mean : ", mean(deFinal.Gen), " DE stdev : ", sd(deFinal.Gen), "\n")
cat("TLBO Mean : ", mean(tlboFinal.Gen), " TLBO stdev : ", sd(tlboFinal.Gen), "\n")

sga = as.vector(sgaFinal.Gen)
de = as.vector(deFinal.Gen)
tlbo = as.vector(tlboFinal.Gen)

data = list(g1 = sga, g2 = de, g3  = tlbo)
print(kruskal.test(data))
