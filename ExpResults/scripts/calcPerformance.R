
fitnessAnalysis <- function(algo, rawData, numEpoch) {
  
  ## Determines number of generation per epoch for each algorithm
  ## rawData is a generation x bstFitness dataframe
  
  y    = rowMeans(as.matrix(rawData));
  fBog = colMeans(as.matrix(y))
  if (algo == "tlbo")
    cat("TLBO F_BOG ", fBog, "\n")
  else
    cat ("DE F_BOG ", fBog, "\n")
  
  numGen = nrow(rawData)%/% numEpoch
  
  if(algo == "tlbo"){
    numGen = (nrow(rawData) + 1)%/% numEpoch
  } 
  
  count = 1
  listAlgo = vector("list", numEpoch)
  
  ## creates a list of bstFitness of the algorithms for each epoch
  for (indexStart in seq (1, nrow(rawData), by = numGen)) {
    indexEnd   = numGen * count
    if (count == numEpoch && algo == "tlbo") indexEnd = indexEnd - 1
    x1 = rawData[ indexStart : indexEnd ,]
    #x1 generates 1/N x \sum_{j=1}^{j=N} F_(BOG_{ij}) part of the formula
    x1 = rowMeans(as.matrix(x1), na.rm = TRUE)
    listAlgo[[count]] = x1
    count = count + 1
  }
  return (listAlgo)      
}

bestOfMean <- function(algo1, algo2, numEpoch) {
  for (epoch in 1:numEpoch){
    print(t.test(as.numeric(unlist(algo1[epoch])), as.numeric(unlist(algo2[epoch]))), paired = FALSE, var.equal = FALSE, conf.level = 0.99) 
    sdAlgo1 = sd(as.numeric(unlist(algo1[epoch]), na.rm = TRUE))
    sdAlgo2   = sd(as.numeric(unlist(algo2[epoch])), na.rm = TRUE)
    cat("Standard Deviation (Algo1/TLBO) : ", sdAlgo1, " Standard Deviation (Algo/DE) : ", sdAlgo2, "\n")
  }
}

#This function takes optimal value and all the best fitness in every
#generations. Then it calculate the difference between the optimum
# and the value of the best individual achieved at the end of each
# epoch (right before the moment of change)
bestErrorBeforeChange <- function(bstFitness, listAlgo, algo, numEpoch) {
  tot.Error = 0
  index = length(listAlgo[[1]])
  
  for (epoch in 1: numEpoch) {
    if (algo == "tlbo" && epoch == numEpoch)  index = index - 1
    best.Error.Before.Change =  (bstFitness - listAlgo[[epoch]][index])
    tot.Error = tot.Error +  best.Error.Before.Change  
  }
  eB = tot.Error / numEpoch
  if (algo == "tlbo") {
    cat("Best Error Before Changes (Algo1/TLBO) :", eB, "\n")
  } else {
    cat("Best Error Before Changes (Algo2/DE) :", eB, "\n")
  }
}

optimisationAccuracy <- function (bstFitness, rawData, algo, minFitness) {
  accGen      = 0
  denominator = bstFitness - minFitness
  bstByGen    = rowMeans(as.matrix(rawData)) 
  bstByGen    = as.data.frame(bstByGen)
  
  for (generation in 1: nrow(bstByGen))
    {
    nominator      = (as.numeric(bstByGen[generation, ]) - minFitness)
    accGen         = accGen + (nominator/denominator)
    }
  
  if (algo == "tlbo") 
    cat("\n Normalisation Accuracy (TLBO) :", accGen/nrow(bstByGen) )
  else  
    cat("\n Normalisation Accuracy (DE) :"  , accGen/nrow(bstByGen) )

}
#Draw plot for average of best fitness under every epoch. 1 epoch = 600 function evaluation

plotEvalVsFitness <- function(rawDataAlgo1, rawDataAlgo2, numEpoch, numEvalinEpoch){
  
  algo1FitnessByGen = rowMeans(rawDataAlgo1[1:nrow(rawDataAlgo1),], na.rm = TRUE)
  algo2FitnessByGen = rowMeans(rawDataAlgo2[1:nrow(rawDataAlgo2),], na.rm = TRUE)
  
  #plot(algo1FitnessByGen , type = "o", pch=20, ann = FALSE, xaxt = 'n')
  #lines(algo2FitnessByGen, type = "o", pch=21, ann = FALSE, xaxt = 'n')
  #title(xlab = "#Evaluations", ylab = "Fitness", main = "Fitness of DE in different epoch for various replacement rate of immigrant")
  #legend(3000, 3800, c("rate = 0%", "rate = 15%"), pch = 20:21)
  #axis(1, at = atAlgo1, labels = lbl)
  
  par(mfrow = c(2,1))
  lbl = c(1:numEpoch) * numEvalinEpoch
  atAlgo1 = c(1:numEpoch) * (numEvalinEpoch %/%2)
  
  plot(algo1FitnessByGen, type = "s", xaxt = 'n', main = "#Evaluation vs Fitness for TLBO (RI = 0%)", xlab = "#Function Evaluation", ylab = "Fitness")
  axis(1, at = atAlgo1, labels = lbl)
  
  plot(algo2FitnessByGen, type = "s", xaxt = 'n', main = "#Evaluation vs Fitness for DE (RI = 0%", xlab = "#Function Evaluation", ylab = "Fitness")
  axis(1, at = lbl, labels = lbl)
}
# From this function, you can analyze three optimality based perfomance adopted from 
# Shengxiang Yang, Xin Yao, Evolutionary Computation for Dynamics Optimization Problems,
# page 16.

  summarizeOptimality = function(){
  numEpoch    = 6
  bstFitness  = 3905.93
  minFitness  = 0
  numEvalinEpoch = 600
  algo1          = "tlbo"
  algo2          = "de"
  
  rawDataAlgo1   = read.table("deresultbstall0.stat", header = FALSE)
  rawDataAlgo2   = read.table("deresultbstall", header = FALSE)
  listAlgo1      = fitnessAnalysis( algo1, rawDataAlgo1, numEpoch )
  listAlgo2      = fitnessAnalysis( algo2, rawDataAlgo2, numEpoch)
  bestOfMean(listAlgo1, listAlgo2, numEpoch)
  bestErrorBeforeChange(bstFitness , listAlgo1, algo1, numEpoch )
  bestErrorBeforeChange(bstFitness , listAlgo2, algo2, numEpoch )
  optimisationAccuracy (bstFitness, rawDataAlgo1, algo1, minFitness)
  optimisationAccuracy (bstFitness, rawDataAlgo2, algo2, minFitness)
  plotEvalVsFitness (rawDataAlgo1, rawDataAlgo2, numEpoch, numEvalinEpoch)
  }
  
  ### START : BEHAVIORAL ANALYSIS #########
  momentOfInertia <- function(rawData) {
    C = c() # centroid
    for (ci in 1:ncol(rawData)) 
      {
      x = as.matrix(rawData[1:nrow(rawData),ci])
      C[ci] = colMeans(x) # do we calculate abs(x)  or just x?
      }
    
    moI = 0
    for (dim in 1:ncol(rawData))
      {
      for (p in 1:nrow(rawData)) 
        {
        moI = moI + (rawData[p, dim] - C[dim]) * (rawData[p, dim] - C[dim]) 
        }
      }
    return(moI)
  }
  
  getMomentOfInertia <- function(howManyGen) {
    
    moiAlgo1 = c(1:howManyGen)
    moiAlgo2 = c(1:howManyGen)
    
    for ( x in 1:howManyGen) {
      
      rawDataAlgo1 = read.table("tlbo0400.stat", header = FALSE)
      moiAlgo1 =  momentOfInertia(rawDataAlgo1)
      rawDataAlgo2 = read.table("de0400.stat", header = FALSE)
      moiAlgo2 =  momentOfInertia(rawDataAlgo2)
    }
    return (list(moiAlgo1, moiAlgo2))
  }
  
  #Calcuation of Reconvery Rate and Absolute Recovery Rate are 
  #based on the phd thesis of Nguen11Phd.pdf page 128

  calcRR <- function(rawData, algo, m, epochId, whichRR)
    {
      pi           = 0
      nominator    = 0
      denominator  = 0
      recoverRate  = 0
      globalOptima = 3905.93
      
      
      if (algo == "tlbo") 
        pi = (nrow(rawData) + 1 ) %/% m 
        
      else 
        pi = nrow(rawData) %/% m
      
      indexStart = (epochId - 1) * pi + 1
      indexEnd = pi * epochId
      
      if (algo == "tlbo" && epochId == m)
        {
          indexEnd = indexEnd - 2
        }
      
      fBest.i.1    = rawData[indexStart,]
      fBest.i.pi   = max(rawData[indexStart : indexEnd, ]) #best so far in this epoch until pi generation
      
      if(whichRR == "RR") 
        deNominator = pi * (fBest.i.pi - fBest.i.1)
      else 
        deNominator = pi * (globalOptima - fBest.i.1)
        
      
      
      nominator = 0
      for (j in indexStart:indexEnd)
        {
          bstSoFar = max(rawData[indexStart:j,])
          nominator = nominator + (bstSoFar - fBest.i.1 )
        }
      
      recoverRate = nominator/deNominator
      return (recoverRate) 
  }
  getRR <- function(numEpoch, whichRR){
    
    rrAlgo1 = c()
    rrAlgo2 = c()
    algo1   = "tlbo"
    algo2   = "de"
    
    rawDataAlgo1 = read.table("resultbstall", header = FALSE)
    rawDataAlgo2 = read.table("deresultbstall", header = FALSE)
 
    rawDataAlgo1 = as.data.frame(rowMeans(as.matrix(rawDataAlgo1))) 
    rawDataAlgo2 = as.data.frame(rowMeans(as.matrix(rawDataAlgo2)))
    
    for (epochid in 1:numEpoch)
    {
      rrAlgo1[epochid]  = calcRR (rawDataAlgo1, algo1, numEpoch, epochid, whichRR)
      rrAlgo2[epochid]  = calcRR (rawDataAlgo2, algo2, numEpoch, epochid, whichRR)
    }
    return (list(rrAlgo1, rrAlgo2))
  }
  summarizeBehavior = function() {
    numEpoch = 6 
    rr  = "RR"
    arr = "ARR"
    
    #moi   = getMomentOfInertia(howManyGen = 1)
    #cat("TLBO (MOI) : ", moi[[1]], " DE (MOI) : ", moi[[2]], "\n")
    recoveryRates    = getRR(numEpoch, rr)
    rrTLBO           = mean(as.numeric(unlist(recoveryRates[[1]])))
    rrDE             = mean(as.numeric(unlist(recoveryRates[[2]])))
    
    absoluteRR   = getRR(numEpoch, arr)
    arrTLBO          = mean(as.numeric(unlist(absoluteRR[[1]])))
    arrDE            = mean(as.numeric(unlist(absoluteRR[[2]])))
    
    cat("(x = rrTLBO) : ", rrTLBO, " , (y = arrTLBO) : ", arrTLBO, "\n")
    cat("(x = rrDE)   : "  , rrDE, " , (y = arrDE)   : ", arrDE  , "\n")
    x = y = seq(0.1:1.0, by = 0.1)
    plot(x, y, type = "l")
    points(rrTLBO, arrTLBO, pch = 20, col = "red")
    #points(rrDE  , arrDE  , pch = 20, col = "blue")
    #legend(0.2, 07, c("TLBO","DE"),col = c("red", "blue"),pch = 20)
    #title(xlab = "Recovery Rate", ylab = "Absolute Recovery Rate", main = "RR-APR Graph (RI=0.15%)")
    
    #tlbo0 = c(0.9743, 0.6654)
    #de0 = c(0.8895, 0.3950)
    
    #tlbo15 = c(0.9699,0.7403)
    #de15 = c(0.8961, 0.3586)
    
    #x = y = seq(0.1:1.0, by = 0.1)
    #plot(x, y, type = "l")
  }
  
  drawSimpleGraph = function(){
    # RI = 0%
    #tlboI = c(8445.73, 5241.84 , 2788.41, 203.17, 26.16, 3.96, 3.18E-8,1737.95, 2012.53, 3657.09, 15538.5, 15540.48, 15541.15, 15541.15)
    #deI   = c(6552.13, 4325.32, 2457.8, 10.43, 0.0007, 8.56E-10, 1.04E-9, 1.57E-9, 5.62E-9, 4.16E-9, 14035.51, 17132.2, 17132.39, 17132.39 )
    
    #RI = 15%
    #tlboI = c(8478.34, 5358.84 , 2742.46, 224.38, 39.87, 0.011, 4.3E-6,1263.95, 1253.21, 2479.75, 17494.4, 17494.41, 17494.41, 17494.41)
    #deI   = c(6535.48, 4396.1, 2538.18, 8.28, 0.0011, 7.5E-10, 7.02E-10, 1.2E-9, 5.76E-9, 5.53E-7, 15970.7, 18433.54, 18807.51, 18807.51 )
    
    #tlboI0 = c(8445.73, 5241.84 , 2788.41, 203.17, 26.16, 3.96, 3.18E-8,1737.95, 2012.53, 3657.09, 15538.5, 15540.48, 15541.15, 15541.15)
    #tlboI15 = c(8478.34, 5358.84 , 2742.46, 224.38, 39.87, 0.011, 4.3E-6,1263.95, 1253.21, 2479.75, 17494.4, 17494.41, 17494.41, 17494.41)
    
    deI0   = c(6552.13, 4325.32, 2457.8, 10.43, 0.0007, 8.56E-10, 1.04E-9, 1.57E-9, 5.62E-9, 4.16E-9, 14035.51, 17132.2, 17132.39, 17132.39 )
    deI15  = c(6535.48, 4396.1, 2538.18, 8.28, 0.0011, 7.5E-10, 7.02E-10, 1.2E-9, 5.76E-9, 5.53E-7, 15970.7, 18433.54, 18807.51, 18807.51 )
    
    iRange = range(0, deI0, deI15)
    
    plot(deI0, type = "o",col = "blue", ylim = iRange, axes = FALSE, ann = FALSE)
    axis(1, at = c(1:14), lab = c(0.2,0.4, 1, 10, 20, 40, 60,0.2,0.4, 1, 10, 20, 40, 60))
    box()

    lines(deI15, type = "o", pch = 22, lty = 2, col = "red")
    title(main = "#Evaluation vs Inertia in Epoch-1 and Epoch-2 (DE)", col.main = "blue", font.main = 4)
    title(xlab = "#Evaluations (in one thousand unit)", col = rgb(0, 1.0, 0.5))
    title(ylab = "Moment of Inertia", col = rgb(0, 1.0, 0.5))
    legend(3, iRange[2], c("rate = 0%", "rate = 15%"), col = c("blue", "red"), pch = 21:22, lty=1:2)
  }
