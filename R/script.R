#model|product|m|features|dependencies|variables|relations|start|end|result
library(plyr)
library(doBy)
library(ggplot2)
library(reshape2)
library(stringr)

script.dir <- dirname(sys.frame(1)$ofile)
setwd(script.dir);

















#------------- dataFlexGen----------

dataFlexGen = read.csv("./flex-gen.csv", header = TRUE,sep='|')
dataFlexGen$time=dataFlexGen$end-dataFlexGen$start
dataFlexGen$precision <- str_count(dataFlexGen$result, ",")+1

min = min(dataFlexGen$precision)

plotData <- summaryBy(time + precision~ features + m, data = dataFlexGen, 
                  FUN = function(x) { c(mean = mean(x)) } )

featuresMGraphTime<-ggplot(plotData, aes(x = as.numeric(m), y = time.mean))  +
  theme_bw() +
  geom_line(aes(linetype=factor(features)),stat = "identity") +
  scale_y_log10("Time in milliseconds (log scale)") +
  geom_point(mapping=aes(x=as.numeric(m), y=time.mean, shape=factor(features)), size=3)+
  scale_linetype_discrete(guide=FALSE)+
  scale_shape_discrete()+
  xlab("m") + 
  labs(shape = "Number of features")+
  theme(legend.position="bottom",legend.text=element_text(size=15)) +
  scale_x_continuous(breaks=c(1,2,4,6,10)) + theme(aspect.ratio=0.45)+
  theme(text = element_text(size=13),axis.text.x = element_text(angle=60, hjust=1))+guides(shape=guide_legend(nrow=1,byrow=TRUE))

ggsave("featuresMGraphTime.pdf",  width = 12)

featuresMGraphTime<-ggplot(plotData, aes(x = as.numeric(m), y = precision.mean))  +
  theme_bw() +
  geom_line(aes(linetype=factor(features)),stat = "identity") +
  scale_y_log10("Number of explanations") +
  geom_point(mapping=aes(x=as.numeric(m), y=precision.mean, shape=factor(features)), size=3)+
  scale_linetype_discrete(guide=FALSE)+
  scale_shape_discrete()+
  xlab("m") + 
  labs(shape = "Number of features")+
  theme(legend.position="bottom",legend.text=element_text(size=15)) +
  scale_x_continuous(breaks=c(1,2,4,6,10)) + theme(aspect.ratio=0.45)+
  theme(text = element_text(size=13),axis.text.x = element_text(angle=60, hjust=1))+guides(shape=guide_legend(nrow=1,byrow=TRUE))

ggsave("featuresMGraphPrecision.pdf",  width = 12)

#------------- dataFlexGen----------

dataFlexReal = read.csv("./flex-real.csv", header = TRUE,sep='|')
dataFlexReal$time=dataFlexReal$end-dataFlexReal$start
dataFlexReal$precision <- str_count(dataFlexReal$result, ",")+1
minReal = min(dataFlexReal$precision)
dataFlexReal$minimality=minReal/dataFlexReal$precision

filter(dataFlexReal,dataFlexReal$precision == minReal)

plotDataReal <- summaryBy(time + precision~ m, data = dataFlexReal, 
                      FUN = function(x) { c(mean = mean(x)) } )

minReal = min(dataFlexReal$precision)


