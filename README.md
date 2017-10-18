#简单amazon爬虫

亚马逊对爬虫抓取做了限制，爬行几百条就会出现５０３，再抓取时间会更短，为了突破数据限制，编写了代理接口及抓取程序。接口只需要添加代理ip,而爬虫只关注于逻辑。

历时３个月爬行了约３亿记录，非常稳定，内存占用约３G(主要是去重算法占用),Cpu稳定...


java  -Xms3128M -Xmx6024M -Djava.ext.dirs=../lib infifly.amazonapi.fetch.AmazonFetch


