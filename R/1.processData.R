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
  subset <- subset(data, model==x$model & product == x$product)
  subset <- subset[ which(subset$resultSize==x$minSize), ]
  result<-subset[1,]$result
  return(result)
}

uniqueCount <- function(x){
  split<-strsplit(as.character(x$union), ", ", fixed=TRUE)
  unlist<-unlist(split)
  result<-paste(unlist[duplicated(unlist)], sep = ", ",collapse=", ")
  return(result)
}


#------------- process ----------
args <- commandArgs(trailingOnly = TRUE)
#args<-c("flex-gen")
inputName<-paste("./sourceData/",args[1],".csv", sep = "")
outputName<-paste("./processedData/",args[1],".csv", sep = "")

###reading the results
data = read.csv(inputName, header = TRUE,sep='|', stringsAsFactors = FALSE)
data$result=str_sub(data$result, 2, str_length(data$result)-1)
###generating duration and number of results
data$end<-as.numeric(as.character(data$end))
data$start<-as.numeric(as.character(data$start))
data$time=data$end-data$start
data$resultSize <- str_count(data$result, ",")+1


if("m" %in% colnames(data))
{
  cat("We are processing a flexdiag-based approach!\n");
  ###calculating the minimal diagnosys size and the minimal diagnosis
  data$minSize <- data$min<-adply(data,1, minSizeCalculation,.expand=T)$V1
  data$min<-adply(data,1,minCalculation,.expand = T)$V1
  ###calculating the disjuntion of the minimal explaination and the actual explaination
  data$union <- paste(as.character(data$result),as.character(data$min), sep = ", ")
  data$union <-adply(data,1,uniqueCount,.expand = T)$V1
  ###calculating accuracy and minimality
  data$accuracy<-(str_count(data$union, ",")+1)/data$minSize
  data$minimality <- data$minSize / data$resultSize
}else{
  dataFlex = read.csv("./processedData/flex-gen.csv", header = TRUE,sep='|', stringsAsFactors = FALSE)
  dataFlex<-dataFlex[which(dataFlex$m ==1),] 
  data$m<-"evolutionary"
  data$minSize<-0
  data$min<-""
  data$union <-""
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
  data<-data[which(data$m =="evolutionary"),] 
  #data<-data[ , !(names(data) %in% c("m"))]
  
}

###save data as CSV
write.table(data, file = outputName,row.names=FALSE,sep="|")

