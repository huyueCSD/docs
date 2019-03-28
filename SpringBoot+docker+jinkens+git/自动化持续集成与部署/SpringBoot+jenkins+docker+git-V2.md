#持续集成与交付的步骤：

		1.编写完成之后编程人员将代码push到git远端仓库
		
		2.Jenkins利用第三方插件拉取源代码
		
		3.Jenkins利用maven进行编译、打包
		
		4.打包完成的代码生成一个新版本的镜像(部署至docker本地仓库)
		
		5.发布运行新的容器
		
		6.访问服务器进行测试

##Jenkins介绍

jenkins是什么？
	
	jenkins是一个开源的自动化服务器，可用于自动执行与构建、测试、交付或部署软件相关的各种任务。

#1部署之前的准备
	
	创建一个SpringBoot项目 可运行的
	
	创建虚拟机
	
	安装CentOS7系统 桥接模式 （因为后面要进行联网下载文件）
	
	使用Xshell工具连接远程服务器

	每个服务器上都安装docker

#环境描述

	  服务器		     		主机名				IP					运行服务
    jenkins服务器		     jenkins			192.168.0.32	安装docker、 运行jenkins容器、git客户端、jdk、maven
    docker服务器	          	 docker			192.168.0.33	安装docker、创建镜像运行java项目：tale

#2.SShH免密登录
###2.1.登录jenkins服务器，  
	
		cd .ssh  进入rsa公钥私钥文件存放的目录，  
		删除目录下的id_rsa，id_rsa.pub文件

###2.2. 在目录下输入命令：  

		ssh-keygen -t rsa 
		（连续三次回车）该目录下会产生id_rsa，id_rsa.pub文件
###2.3. 把公钥拷贝到docker服务器 
		ssh-copy-id  git@192.168.0.33  输入密码
#3配置docker服务器
###注意：这个步骤在 docker服务器上操作

###3.1、安装Docker

		[root@docker ~]# yum install -y yum-utils device-mapper-persistent-data lvm2
		
		[root@docker ~]# yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
		
		[root@docker ~]# yum install docker-ce


###3.2、启动dokcer服务

	[root@docker ~]# systemctl restart docker


###3.3、部署JDK

由于后面运行java容器需要jdk环境，jdk如果放在容器中运行容器又相当重，所以就在宿主机上部署jdk，后面创建java容器的时候把宿主机的jdk路径挂载到容器就去。部署jdk很简单，解压就行：

	[root@docker ~]# tar -zxvf jdk-8u51-linux-x64.tar.gz -C /usr/local/  
我们把它解压在docker这台宿主机的/usr/local目录中。


###3.4、构建基础镜像
其实，这个镜像随便在哪台服务器上构建都行，在本文中就直接在这台docker服务器构建了：

	[root@docker /]# mkdir /data
	
	[root@docker /]# cd /data
	
	[root@docker data]# vim Dockerfile

dockerfile内容：
		
		FROM centos:7
		ADD jdk-8u51-linux-x64.tar.gz /usr/local/src
		ENV JAVA_HOME=/usr/local/src/jdk1.8.0_51
		ENV PATH=$JAVA_HOME/bin:$PATH
		ENV CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
		ADD test_hello_war.jar /hello/
		WORKDIR /hello
		EXPOSE 8080
		CMD ["java","-jar","test_hello_war.jar"]

此为以上注释每个序号代表一行：  
	
		1.基于CentOS7镜像为基础  
		2.添加jdk压缩包 ADD命令自动解压
		3-5.设置环境变量
		6.添加build后打包后的SpringBoot.jar文件
		7.设置工作目录
		8.向外暴露8080端口
		9.执行命令

此dockerfile安装了jdk 部署了jdk环境变量 向外暴露了8080端口  
###注意：在/data目录中上传jdk-8u51-linux-x64.tar.gz  
	
	[root@docker data]#docker build  -t hello-test-master .
	
	[root@docker data]# docker images


##4部署jenkins

###注意：这个步骤在 jenkins 服务器上操作
###4.1环境配置安装步骤

####4.1.1、部署JDK  

	[root@jenkins /]# tar -zxvf jdk-8u51-linux-x64.tar.gz -C /usr/local/

####4.1.2、部署maven

	[root@jenkins local]# tar -zxvf apache-maven-3.6.0-bin.tar.gz  -C /usr/local/

####4.1.3、jenkins安装步骤
进入jenkins要安装的目录  

	获取jenkins安装源文件  
	wget -O /etc/yum.repos.d/jenkins.repo http://pkg.jenkins-ci.org/redhat/jenkins.repo  
	导入公钥  
	rpm --import https://jenkins-ci.org/redhat/jenkins-ci.org.key
	安装jenkins  
	yum install -y jenkins
可修改jenkins  端口为8080（默认8080我没改）

	vim /etc/sysconfig/jenkins
	JENKINS_PORT='8080'
####4.1.4.修改jenkins的jdk的安装路径

	vim  /etc/init.d/jenkins

	candidates=""中的/usr/bin/java  修改为你的jdk安装路径    /usr/local/src/java/jdk1.8.0_45/bin/java  这是我的  

####4.1.5.配置防火墙放心jenkins端口（8080）

	重启jenkins

####4.1.6.使用浏览器 http://192.168.0.32:8080
	
	进入之后让你输入密码  密码在/var/lib/...提示的目录下
	vim /var/lib/...

####4.1.7.进入安装插件   可能是离线状态

	此时不要在当前窗口操作  
	再打开一个tab窗口输入http://192.168.0.32:8080/pluginManager/advanced  
	在最下方的URL中修改为http://updates.jenkins.io/update-center.json     
	提交  

####4.1.8.重启jenkins

	service jenkins restart  
	在之前的离线窗口刷新 再次执行输入密码操作

####4.1.9.页面会跳转等待后进入CUstomize Jenkins 页面 选择安装（选第一个默认安装）

	推荐安装（它就会自动安装）
	进入之后创建用户信息
	重启jenkins      service jenkins restart
	需要额外安装的插件

####4.1.10.安装额外的插件

	进入系统管理--插件管理--可选插件--搜索以下插件  直接安装
	Maven Integration plugin
	Deploy to container plugin
	publish over ssh

###4.2 系统设置

	进入“系统管理” -- “系统设置”，主要是把docker这台服务器通过ssh的形式添加进来，后期部署dokcer容器

参数说明：

	Name：192.168.0.33-docker
	Hostname：192.168.0.33
	Username：root
	Remote Directory:/data 这个意思是把代码发布192.168.0.33 /data目录中，需要手动创建个目录
	Use password authentication, or use a different key：勾选它
	点击高级设置	
	Passphrase / Password：输入192.168.0.33这台docker服务器的密码
	Key：
			进入jenkins服务器的.ssh目录中
			cat id_rsa 
			将秘钥拷贝至Key中
			点击测试连接即可

###4.3 配置Maven、jdk、git环境

####4.3.1、jdk配置

	进“系统管理” -- "Global Tool Configuration"，添加jdk安装

参数说明：

	别名： jdk （自定义就行）
	JAVA_HOME： /usr/local/jdk1.8.0_51   这个是你jenkins容器里的JDK路径，不是宿主机的JDK路径；

####4.3.2、maven配置

	进“系统管理” -- "Global Tool Configuration"，添加maven安装

参数说明：

	和上面的形式jdk一样，MAVEN_HOME 的路径也是指向jenkins容器里的maven路径

####4.3.3、git配置

	这里我没有动git的配置，让它为默认配置

###4.4 配置java项目

####4.4.1.构建maven项目
	
	新建任务 随便起个项目名  构建一个maven项目    确定 

####4.4.2、源码管理

	jenkins提供多种代码管理的接口 git、svn等
	在“源码管理”项中选择Git 输入项目的git地址
	点击添加钥匙jenkins凭据提供者
	用户名中输入你git的用户名和密码  点击添加

	Branches to build 中选中你将要拉取源代码的分支
	
####4.4.3、构建触发器

    选这个 Build whenever a SNAPSHOT dependency is built
	还可以定时触发  * * * * *   （代表每分钟构建一次）
构建触发器的介绍

	build whenever a snapshot dependency is built
	
	当job依赖的快照版本被build时，执行本job。
	
	build after other projects are built
	
	当本job依赖的job被build时，执行本job
	
	build periodically：隔一段时间build一次，不管版本库代码是否发生变化。
	
	poll scm：隔一段时间比较一次源代码如果发生变更，那么就build。否则，不进行build。

	定时构建语法
		第一颗*表示分钟，取值0~59
		第二颗*表示小时，取值0~23
		第三颗*表示一个月的第几天，取值1~31
		第四颗*表示第几月，取值1~12
		第五颗*表示一周中的第几天，取值0~7，其中0和7代表的都是周日
		1.每30分钟构建一次：
			H/30 * * * *
		2.每2个小时构建一次
			H H/2 * * *
		3.每天早上8点构建一次
			0 8 * * *
		4.每天的8点，12点，22点，一天构建3次
			0 8,12,22 * * *
	
####4.4.4、构建环境
	（将时间戳添加到控制台输出）
	Add timestamps to the Console Output
####4.4.5、build
	
	Root POM输入： pom.xml
	Goals and options输入：clean package

####4.4.6、Post Steps

	在“Post Steps”选项中，配置如下操作：
		点击Add post-build step选择Send files orexecute commands over SSH
	Nmae:选择需要部署的docker服务器，前面我们SSH增加了一台，所以这里可以直接选择；
	source files：需要部署到目标服务的打包成果路径
	Remove prefilx: “Source files”配置的路径中要移除的前缀
	Remote directory：成果要发送到的远程目标服务目录路径，这个路径与第一步配置中的Remote Directory对应。
	Exec command：成果发送完成后，需要执行的命令，具体如下
			docker rm -f hytest
			docker rmi -f hello-test-master 
			cd /data
			docker build -t hello-test-master .
			docker run  --name hytest  -p 8080:8080 -d hello-test-master
####4.4.7、构建后操作
	
	这里主要可以对结果进行一些可视化的处理，并提供反馈和通知，也可以触发其他任务。	
	点解增加构建后步骤
	点击Editable Email Notification（进入邮件内容详细配置界面）
	Project Recipient List：这个项目的需要发送邮件给哪些人，可以在这里输入多个邮箱，中间以英文逗号隔开。

邮件详细情况

	https://www.cnblogs.com/yajing-zh/p/5111060.html

####4.4.8、构建项目

	回到项目界面 
		1.点击立即构建  
		2.点击下方的正在构建的项目进度条时间  
		3.点击（控制台输出  进行查看）

	看控制台的输出有没有异常	（第一次的构建因为需要下载maven的jar包速度会比较慢）	
		
		[INFO] BUILD SUCCESS
		Finished: SUCCESS
		表示成功搭建完成
	
#5、测试

		浏览器访问服务器地址
		https://192.168.0.33:8080

