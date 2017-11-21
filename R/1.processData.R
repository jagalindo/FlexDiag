library(plyr)
library(doBy)
library(ggplot2)
library(reshape2)
library(stringr)
library(xtable)

minSizeCalculation <- function(x){
  subset <- data[ which(data$model==x$model & data$product == x$product), ] 
 
    ##force to use fmdiag m=1 for minimal diagnossis avoiding non-complete results from evolutionary
  #si tenemos ejecutado con m=1 lo uso, si no el minimo y listo
  if(nrow(subset[ which(subset$m==1), ])>0){
    subset <- subset[ which(subset$m==1), ]
  }
  result <-  min(subset$resultSize)
  return(result)
}


minCalculation <- function(x){
  subset <- data[ which(data$model==x$model & data$product == x$product), ] 
  subset <- subset[ which(subset$resultSize==x$minSize), ]
  result<-subset[1,]$result
  return(result)
}

minSizeGroupedCalculation <- function(x){
  subset <- data[ which(data$model==x$model & data$percentage == x$percentage), ] 
  
  ##force to use fmdiag m=1 for minimal diagnossis avoiding non-complete results from evolutionary
  #si tenemos ejecutado con m=1 lo uso, si no el minimo y listo
  if(nrow(subset[ which(subset$m==1), ])>0){
    subset <- subset[ which(subset$m==1), ]
  }
  result <-  min(subset$resultSize)
  return(result)
}


minGroupedCalculation <- function(x){
  subset <- data[ which(data$model==x$model & data$percentage == x$percentage), ] 
  subset <- subset[ which(subset$resultSize==x$minGroupedSize), ]
  result<-subset[1,]$result
  return(result)
}


uniqueCount <- function(x){
  string<-gsub(" ", "", as.character(x$union), fixed = TRUE)
  split<-strsplit(string, ",", fixed=TRUE)
  unlist<-unlist(split)
  result<-paste(unlist[duplicated(unlist)], sep = ", ",collapse=", ")
  return(result)
}

uniqueGroupedCount <- function(x){
  split<-strsplit(as.character(x$groupedUnion), ", ", fixed=TRUE)
  unlist<-unlist(split)
  result<-paste(unlist[duplicated(unlist)], sep = ", ",collapse=", ")
  return(result)
}

segundo <- function(x){
  all<-tail(x,2)
  tailBut<-head(all,1)
return(tailBut)
}

#------------- process ----------
args <- commandArgs(trailingOnly = TRUE)
#args<-c("evol")
#args<-c("flex-real")

inputName<-paste("./sourceData/",args[1],".csv", sep = "")
outputName<-paste("./processedData/",args[1],".csv", sep = "")

###reading the results
data = read.csv(inputName, header = TRUE,sep='|', stringsAsFactors = FALSE)
#data$result=str_sub(data$result, 2, str_length(data$result)-1)
#data$result<-gsub('^.|.$', '', data$result)
###generating duration and number of results
data$end<-as.numeric(as.character(data$end))
data$start<-as.numeric(as.character(data$start))
data$time=data$end-data$start
data$resultSize <- str_count(data$result, ",")+1
data$iteration <-sapply(strsplit(as.character(gsub(".prod","",data$product)), "-"), tail, 1)
data$percentage <-sapply(strsplit(as.character(gsub(".prod","",data$product)), "-"), segundo)

#mirar si filtrar aqui, yo diría que si para evitar comparar con otras no minimas

#dataFlexGen<-dataFlexGen[which(!dataFlexGen$features==5000),]
#dataEvol<-dataEvol[which(!dataEvol$features==5000),]

if("m" %in% colnames(data))
{
  cat("We are processing a flexdiag-based approach!\n");

 # data <- data[which(data$percentage==100),]
  #data <- subset(data, select = -c(productDesc) )

  
  ###calculating the minimal diagnosys size and the minimal diagnosis
  data$minSize <- data$min<-adply(data,1, minSizeCalculation,.expand=T)$V1
  data$min<-adply(data,1,minCalculation,.expand = T)$V1
  #with groups
  ###calculating the disjuntion of the minimal explaination and the actual explaination
  data$union <- paste(as.character(data$result),as.character(data$min), sep = ", ")
  data$union <-adply(data,1,uniqueCount,.expand = T)$V1
 
  data$unionSize<-str_count(data$union, ",")+1
  ###calculating accuracy and minimality
  data$accuracy<-data$unionSize/data$minSize
  data$minimality <- data$minSize / data$resultSize
 
  #Calculate another set of metrics between different orders to check if m=1 always returns the same result.
  #data$minGroupedSize <- data$minGrouped<-adply(data,1, minSizeGroupedCalculation,.expand=T)$V1
  #data$minGrouped<-adply(data,1,minGroupedCalculation,.expand = T)$V1
  
  #data$groupedUnion <- paste(as.character(data$result),as.character(data$minGrouped), sep = ", ")
  #data$groupedUnion <-adply(data,1,uniqueGroupedCount,.expand = T)$V1
  
  #data$accuracyGrouped <-(str_count(data$groupedUnion, ",")+1)/data$minGroupedSize
  #data$minimalityGrouped <- data$minGroupedSize / data$resultSize
  
  
}else{
  dataFlex = read.csv("./processedData/flex-gen.csv", header = TRUE,sep='|', stringsAsFactors = FALSE)
  dataFlex <- subset(dataFlex, select = -c(productDesc) )
  dataFlex<-dataFlex[which(dataFlex$m ==1),] 
  dataFlex$iteration <-sapply(strsplit(as.character(gsub(".prod","",dataFlex$product)), "-"), tail, 1)
  dataFlex$percentage <-sapply(strsplit(as.character(gsub(".prod","",dataFlex$product)), "-"), segundo)
  dataFlex$product<-paste(paste(dataFlex$percentage,"-",sep=""),dataFlex$iteration,sep="")
  data$m<-"evolutionary"
  data$minSize<-0
  data$min<-""
  data$union <-""
  data$unionSize<-0
  data$accuracy<-0
  data$minimality <-0
  data <- rbind(data,dataFlex)
  ###calculating the minimal diagnosys size and the minimal diagnosis
  data$minSize <- data$min<-adply(data,1, minSizeCalculation,.expand=T)$V1
  data$min<-adply(data,1,minCalculation,.expand = T)$V1
  ###calculating the disjuntion of the minimal explaination and the actual explaination
  data$union <- paste(as.character(data$result),as.character(data$min), sep = ", ")
  data$union <-adply(data,1,uniqueCount,.expand = T)$V1
  ###calculating accuracy and minimality
  data$accuracy<-(str_count(data$union, ",")+1)/data$minSize
  data$minimality <- data$minSize / data$resultSize
  data$unionSize<-str_count(data$union, ",")+1
  data<-data[which(data$m =="evolutionary"),] 
  #data<-data[ , !(names(data) %in% c("m"))]
  
}

###save data as CSV
write.table(data, file = outputName,row.names=FALSE,sep="|")

