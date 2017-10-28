library(plyr)
library(doBy)
library(ggplot2)
library(reshape2)
library(stringr)
library(xtable)
library(tableHTML)

getValuableColumns <- function(data){
  data<- subset(data, select = c(model,product, m,features,dependencies,accuracy, minimality) )
}

removeExtension <-function(data){
  data$product=gsub(".prod","",data$product)
  data$model=gsub(".xml","",data$model)
  return(data)
}  

removeModelName <-function(data){
  data$iteration <-sapply(strsplit(as.character(data$product), "-"), tail, 1)
  data$percentage <-sapply(strsplit(as.character(data$product), "-"), segundo)
  data$product<-paste(data$percentage,data$iteration,sep = "-")
  return(data)
}
segundo <- function(x){
  all<-tail(x,2)
  tailBut<-head(all,1)
  return(tailBut)
}
######## Read data  
dataFlexGen7 = read.csv("./old/7-17/processedData/flex-gen.csv", header = TRUE,sep='|')
dataFlexReal7 = read.csv("./old/7-17/processedData/flex-real.csv", header = TRUE,sep='|')

dataFlexGen10 = read.csv("./old/10-17/processedData/flex-gen.csv", header = TRUE,sep='|')
dataFlexReal10 = read.csv("./old/10-17/processedData/flex-real.csv", header = TRUE,sep='|')

#Remove extension
dataFlexGen7<-removeExtension(dataFlexGen7)
dataFlexReal7<-removeExtension(dataFlexReal7)
dataFlexGen10<-removeExtension(dataFlexGen10)
dataFlexReal10<-removeExtension(dataFlexReal10)

dataFlexGen10<-removeModelName(dataFlexGen10)
dataFlexReal10<-removeModelName(dataFlexReal10)

###Remove non useful columns
dataFlexGen7<-getValuableColumns(dataFlexGen7)
dataFlexReal7<-getValuableColumns(dataFlexReal7)
dataFlexGen10<-getValuableColumns(dataFlexGen10)
dataFlexReal10<-getValuableColumns(dataFlexReal10)

######## 
######## Flexdiag Random.
######## 

dataFlexGenSummary10 <- summaryBy( minimality + accuracy~ features + m, data = dataFlexGen10, FUN = function(x) { c(october = mean(x)) } )
dataFlexGenSummary7 <- summaryBy( minimality + accuracy~ features + m, data = dataFlexGen7, FUN = function(x) { c(july = mean(x)) } )
totalGen <- merge(dataFlexGenSummary7,dataFlexGenSummary10,by=c("features","m"))

##remove 5000 feats due failure in july
totalGen<-totalGen[which(!totalGen$features==5000),]
totalGen<-totalGen[which(!totalGen$features==500),]
totalGen<-totalGen[which(!totalGen$features==100),]

#CalculateDiff
totalGen$accuracy.diff=totalGen$accuracy.july-totalGen$accuracy.october
totalGen$minimality.diff=totalGen$minimality.july-totalGen$minimality.october

#Reorder
totalGen<-arrange(totalGen, features, m)

## Latex table.
print(xtable(totalGen), include.rownames=FALSE, file="./output/gen-table.tex")
#htmlTable(dataFlexGenSummary)
write_tableHTML(tableHTML(totalGen, rownames = FALSE, caption = 'FlexDiag with Random Models'), "./output/gen-table.html", complete_html = FALSE)

######## 
######## Flexdiag Real.
######## 

dataFlexRealSummary10 <- summaryBy( minimality + accuracy~ model + m, data = dataFlexReal10, FUN = function(x) { c(october = mean(x)) } )
dataFlexRealSummary7 <- summaryBy( minimality + accuracy~ model + m, data = dataFlexReal7, FUN = function(x) { c(july = mean(x)) } )
totalReal <- merge(dataFlexRealSummary7,dataFlexRealSummary10,by=c("model","m"))

#CalculateDiff
totalReal$accuracy.diff=totalReal$accuracy.july-totalReal$accuracy.october
totalReal$minimality.diff=totalReal$minimality.july-totalReal$minimality.october

#Reorder
totalReal<-arrange(totalReal, model, m)
totalReal<-totalReal[which(startsWith(totalReal$model,"REAL")),]
totalReal<-totalReal[which(!totalReal$model=="REAL-FM-20"),]

## Latex table.
print(xtable(totalReal), include.rownames=FALSE, file="./output/real-table.tex")
#htmlTable(dataFlexGenSummary)
write_tableHTML(tableHTML(totalReal, rownames = FALSE, caption = 'FlexDiag with Real Models'), "./output/real-table.html", complete_html = FALSE)

