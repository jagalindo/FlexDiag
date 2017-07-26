#model|product|m|features|dependencies|variables|relations|start|end|result
library(plyr)
library(doBy)
library(ggplot2)
library(reshape2)
library(stringr)

#script.dir <- dirname(sys.frame(1)$ofile)
#setwd(script.dir);

minSizeCalculation <- function(x){
  subset <- data[ which(data$model==x$model & data$product == x$product), ]
  result <-  min(subset$resultSize)
  return(result)
}

minCalculation <- function(x){
  subset <- subset(data, model==x$model & product == x$product)
  subset <- subset(subset, minSize == as.numeric(x$minSize))
  result<-subset[1,]$result
  return(result)
}


#------------- process ----------

data = read.csv("./flex-real.csv", header = TRUE,sep='|')
data$result=str_sub(data$result, 2, str_length(data$result)-1)

data$time=data$end-data$start
data$resultSize <- str_count(data$result, ",")+1

#Esto nos permite acceder a los valores del aaplr por nombre. 
data$minSize <- data$min<-adply(data,1, minSizeCalculation,.expand=T)$V1
data$min<-adply(data,1,minCalculation,.expand = T)$V1

data$minimality <- data$minSize / data$resultSize
data$union <- paste(as.character(data$result),as.character(data$min), sep = ", ")

unique(strsplit(as.character(data$union), ", ", fixed=TRUE))


data$accuracy<-(length(union(strsplit(as.character(data$result), ", ", fixed=TRUE),
                     strsplit(as.character(data$min), ", ", fixed=TRUE))[[1]]))/data$minSize

plotData <- summaryBy(time + minimality + accuracy~ model+variables+dependencies+ m, data = data, 
                      FUN = function(x) { c(mean = mean(x)) } )
#Print latextable
print(xtable(plotData), include.rownames=FALSE)

#generate plot m vs time
featuresMGraphTime<-ggplot(plotData, aes(x = as.numeric(m), y = time.mean))  +
  theme_bw() +
  geom_line(aes(linetype=factor(features)),stat = "identity") +
  scale_y_log10(
    breaks = scales::trans_breaks("log10", function(x) 10^x),
    labels = scales::trans_format("log10", scales::math_format(10^.x))
  ) +
  geom_point(mapping=aes(x=as.numeric(m), y=time.mean, shape=factor(features)), size=3)+
  scale_linetype_discrete(guide=FALSE)+
  scale_shape_discrete()+
  xlab("Value of m") + 
  ylab("Time in milliseconds (log scale)") +
  labs(shape = "Number of features")+
  theme(legend.position="bottom",legend.text=element_text(size=15)) +
  scale_x_continuous(breaks=c(1,2,4,6,10)) + theme(aspect.ratio=0.45)+
  annotation_logticks(sides = "l") +
  theme(text = element_text(size=13),axis.text.x = element_text(angle=60, hjust=1))+guides(shape=guide_legend(nrow=1,byrow=TRUE))

ggsave("featuresMGraphTime.pdf",  width = 12)

#generate plot m vs minimality
featuresMGraphMinimality<-ggplot(plotData, aes(x = as.numeric(m), y = minimality.mean))  +
  theme_bw() +
  geom_line(aes(linetype=factor(features)),stat = "identity") +
  geom_point(mapping=aes(x=as.numeric(m), y=minimality.mean, shape=factor(features)), size=3)+
  scale_linetype_discrete(guide=FALSE)+
  scale_shape_discrete()+
  xlab("Value of m") + 
  ylab("Minimality (log scale)") +
  labs(shape = "Number of features")+
  theme(legend.position="bottom",legend.text=element_text(size=15)) +
  scale_x_continuous(breaks=c(1,2,4,6,10)) + theme(aspect.ratio=0.45)+
  theme(text = element_text(size=13),axis.text.x = element_text(angle=60, hjust=1))+guides(shape=guide_legend(nrow=1,byrow=TRUE))

ggsave("featuresMGraphMinimality.pdf",  width = 12)


#generate plot m vs accuracy
featuresMGraphMinimality<-ggplot(plotData, aes(x = as.numeric(m), y = accuracy.mean))  +
  theme_bw() +
  geom_line(aes(linetype=factor(features)),stat = "identity") +
  geom_point(mapping=aes(x=as.numeric(m), y=accuracy.mean, shape=factor(features)), size=3)+
  scale_linetype_discrete(guide=FALSE)+
  scale_shape_discrete()+
  xlab("Value of m") + 
  ylab("Accuracy (log scale)") +
  labs(shape = "Number of features")+
  theme(legend.position="bottom",legend.text=element_text(size=15)) +
  scale_x_continuous(breaks=c(1,2,4,6,10)) + theme(aspect.ratio=0.45)+
  theme(text = element_text(size=13),axis.text.x = element_text(angle=60, hjust=1))+guides(shape=guide_legend(nrow=1,byrow=TRUE))

ggsave("featuresMGraphAccuracy.pdf",  width = 12)
