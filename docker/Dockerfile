#添加contos6.5文件
FROM a2a69ca5184a

#添加JDK1.8  centos+jdk
ADD jdk-8u51-linux-x64.tar.gz /usr/local/src
ENV JAVA_HOME=/usr/local/src/jdk1.8.0_51
ENV PATH=$JAVA_HOME/bin:$PATH
ENV CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar

#centOS6.5+JDK1.8+tomcat7
ADD apache-tomcat-7.0.55.tar.gz /usr/local/src
ENV CATALINA_HOME /usr/local/src/apache-tomcat-7.0.55
ENV PATH=$PATH:$CATALINA_HOME/bin

#添加ROOT.war包文件
COPY ROOT.war $CATALINA_HOME/webapps/ROOT/ROOT.war
WORKDIR $CATALINA_HOME/webapps/ROOT
RUN jar xvf ROOT.war
RUN rm ROOT.war

#对外暴露的端口号
EXPOSE 8080
CMD ["/usr/local/src/apache-tomcat-7.0.55/bin/catalina.sh","run"]
