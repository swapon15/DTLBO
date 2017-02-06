rawData.TLBO = read.table("./result.bst.all", header = F)
tlbo.data = as.matrix(rawData.TLBO)
m = t(tlbo.data)
v = colMeans(m)
v = v * -1
plot(v, type = "l")
axis(1, at = c(5000, 10000), lab = c ("*", "*"))

