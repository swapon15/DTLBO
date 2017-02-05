# To run, place in the ./stats-data directory for Exp 3, run R, then:
#  source('rpw-exp3-analysis.r')
#  summarizeObjData(useMeanMean=T)
#  summarizeDispData()

# You have to have the following libraries installed:
library(plyr)
library(dplyr)
library(ggplot2)

# Also, don't forget to fix the typo in the filename of diff.<etc.> in P-PHC-I...


# Convert objective data into long-form, which is better for 
# visualizationand data analysis in R.  Each row is a separate observation 
# and each column is a variable:  
#    Trial      (numeric)
#    Generation (numeric)
#    BSF        (numeric)
convertObjData <- function(problems, alg) {
  numGens   = dim(problems)[1]
  numTrials = dim(problems)[2]
  
  trialVar = NULL
  genVar = NULL
  bsfVar = NULL
  
  for (trial in 1:numTrials) {
    for (gen in 1:numGens) {
      # Build a row related to the problem population 
      trialVar = c(trialVar, trial)
      genVar = c(genVar, gen)
      bsfVar = c(bsfVar, problems[gen, trial])
    }
  }
  
  # All of these are from the same algorithm, so the Algorithm column is just 
  # the same thing all the way down
  algVar = rep(alg, length(trialVar))
  
  # Send back as an R data frame
  return( data.frame(Algorithm=algVar,
                     Trial=trialVar,
                     Generation=genVar,
                     BSF=bsfVar) )
}


# Wrapper function that gets all BSF data related to a particular
# noise level and algorithm and returns a standard R data frame
# for that data.
getObjData = function() {
  rawData.CHC  = read.table('./temp_result1',   header=F)
  rawData.PHCI = read.table('./temp_result2', header=F)
  rawData.PHCP = read.table('./temp_result3', header=F)  
  
  return ( rbind(convertObjData(rawData.CHC, "SGA"),
                 convertObjData(rawData.PHCI, "DE"),
                 convertObjData(rawData.PHCP, "TLBO") ) )
}


# Produce a plot of the candidate and test BSF curves, means and SD error bars,
# along with the thresholds, etc.
plotObjectiveAggregates <- function(bsfData) {
  aggData = ddply(bsfData, .(Generation, Algorithm), summarize, avgBSF=mean(BSF), sigmaBSF=sd(BSF))
  
  g = ggplot(aggData, aes(x=Generation, y=avgBSF)) + 
    geom_errorbar(aes(ymin=avgBSF-sigmaBSF/2, ymax=avgBSF+sigmaBSF/2), color="darkgray") +
    geom_line(size=1.25) + 
    ylab("Best So Far Fitness") +
    facet_grid(Algorithm ~ .) +
    #ggtitle(paste("Algorithm=",alg)) +
    theme(text=element_text(size=18, family="Times"))
  
  print(g)
}


summarizeObjData <- function(useMeanMean=F) {
  bsfData = getObjData()
  
  if (useMeanMean)
    bestInEachTrial = ddply(bsfData, .(Algorithm, Trial), summarize, best=mean(BSF))
  else
    bestInEachTrial = ddply(bsfData, .(Algorithm, Trial), summarize, best=max(BSF))
  
  overalAverages = ddply(bestInEachTrial, .(Algorithm), summarize, meanBest = mean(best), sdBest = sd(best))

  cat("\n")
  print(overalAverages)
  
  cat("\n")
  print("Test for difference in minimum generation to threshold for problems population:", quote=F)
  print(kruskal.test(best ~ Algorithm, 
                     data=bestInEachTrial))
  
  cat("\n")
  plotObjectiveAggregates(bsfData)
}





# Convert dispersion data into long-form, which is better for 
# visualizationand data analysis in R.  Each row is a separate observation 
# and each column is a variable:  
#    Trial      (numeric)
#    Individual (numeric)
#    BSF        (numeric)
convertDispData <- function(problems, alg) {
  numTrials = dim(problems)[1]
  numIndividuals = dim(problems)[2]
  
  trialVar = NULL
  indivVar = NULL
  dispVar = NULL
  
  for (trial in 1:numTrials) {
    for (ind in 1:numIndividuals) {
      # Build a row related to the problem population 
      trialVar = c(trialVar, trial)
      indivVar = c(indivVar, ind)
      dispVar = c(dispVar, problems[trial, ind])
    }
  }
  
  # All of these are from the same algorithm, so the Algorithm column is just 
  # the same thing all the way down
  algVar = rep(alg, length(trialVar))
  
  # Send back as an R data frame
  return( data.frame(Algorithm=algVar,
                     Trial=trialVar,
                     Individual=indivVar,
                     Dispersion=dispVar) )
}


# Wrapper function that gets all BSF data related to a particular
# noise level and algorithm and returns a standard R data frame
# for that data.
getDispData = function() {
  rawData.CHC  = read.table('./PCHC/test_xy_difference_merged',   header=F)
  rawData.PHCI = read.table('./PPHCI/test_xy_difference_merged', header=F)
  rawData.PHCP = read.table('./PPHC/test_xy_difference_merged', header=F)
  
  return ( rbind(convertDispData(rawData.CHC, "P-CHC"),
                 convertDispData(rawData.PHCI, "P-PHC-I"),
                 convertDispData(rawData.PHCP, "P-PHC-P") ) )
}



plotDispersionAggregates <- function(dispData) {
  aggData = ddply(dispData, .(Algorithm), summarize, avgDisp=mean(Dispersion), sigmaDisp=sd(Dispersion))
  
  g = ggplot(aggData, aes(x=Algorithm, y=avgDisp)) + 
    geom_errorbar(aes(ymin=avgDisp-sigmaDisp/2, ymax=avgDisp+sigmaDisp/2)) +
    geom_bar(stat="identity") + 
    ylab("Dispersion") +
    theme(text=element_text(size=18, family="Times"))
  
  print(g)
}



summarizeDispData <- function() {
  dispData = getDispData()
  aggData = ddply(dispData, .(Algorithm), summarize, avgDisp=mean(Dispersion), sigmaDisp=sd(Dispersion))

  cat("\n")
  print(aggData)
  
  cat("\n")
  print("Test for difference in minimum generation to threshold for problems population:", quote=F)
  print(kruskal.test(Dispersion ~ Algorithm, 
                     data=dispData))
  
  cat("\n")
  plotDispersionAggregates(dispData)
}


