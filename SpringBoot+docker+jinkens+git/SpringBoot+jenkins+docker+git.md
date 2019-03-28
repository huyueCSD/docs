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

#2环境描述

	  服务器		     		主机名				IP					运行服务
    jenkins服务器		     jenkins			192.168.0.32	安装docker、 运行jenkins容器、git客户端、jdk、maven
    docker服务器	           docker			192.168.0.33	安装docker、创建镜像运行java项目：tale
    Git和私有仓库服务器	    git_registry	 	192.168.0.34	安装docker、git服务、运行registry私有仓库容器


#3部署环境

##3.1 部署git服务

###注意：这个步骤在 GIT_REGISTRY 服务器上操作

1、安装git

[root@git_registry ~]# yum -y install git

2、创建git用户

[root@git_registry ~]# useradd  git

[root@git_registry ~]# passwd git

切换创建的git用户操作：

[root@git_registry ~]#su - git

3、创建仓库

创建app.git仓库，仓库名自定义，这一步需要切换刚才创建的git用户操作

[git@git_registry root]$ cd /home/git/

[git@git_registry ~]$ cd app.git/

[git@git_registry app.git]$ git --bare init

##3.2 验证git服务

###注意：这个步骤在 jenkins 服务器上操作


1、下载git客户端

[root@jenkins ~]# yum -y install git

2、下载tale项目包

先创建个目录来存放tale包，随便放在哪个空目录下都行，后面我们还需要把它移走  
[root@jenkins ~]# mkdir /tale

[root@jenkins ~]# git  clone  https://github.com/otale/tale.git

在查看下载好的tale项目  
[root@jenkins tale]# ls

3、生成公钥，拷贝到git服务器

[root@jenkins tale]# ssh-keygen  -t rsa


把公钥拷贝到git服务器，注意用刚才的git用户  
[root@jenkins tale]# ssh-copy-id  git@192.168.0.34

4、用git clone验证

先创建个目录，目录名随便定义，用于拉取git服务器上创建的app.git仓库  
[root@jenkins tale]# mkdir /git

[root@jenkins tale]# cd /git/

配置下git客户端的用户信息  
[root@jenkins git]# git config --global  user.email "test@qq.com"

[root@jenkins git]# git config --global user.name "test"

在来git clone，由于app.git仓库是空的，所以会很快  
[root@jenkins git]# git clone  git@192.168.0.34:/home/git/app.git

5、把之前下载的tale项目push到app.git仓库中

先移动至刚才/git目录下  
[root@jenkins git]# mv /tale/* /git/app/

[root@jenkins git]# cd /git/app/

[root@jenkins app]# ls

bin  LICENSE  package.xml  pom.xml  README.md  README_ZH.md  src

然后提交到git仓库  
[root@jenkins app]# git add .

[root@jenkins app]# git commit  -m "add project tale"

[root@jenkins app]# git push  origin master


##3.3 部署docker私有仓库

###注意：这个步骤在 git_registry 服务器上操作
我们还是用官方的镜像来创建docker私有仓库：  
[root@git_registry /]# docker run -d -v /opt/registry:/var/lib/registry -p 5000:5000 --restart=always --name rregistry registry

查看一下私有仓库已经起来了：

[root@git_registry /]# docker ps -l

    CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                    NAMES
    4ac357e4b6dd        registry            "/entrypoint.sh /etc…"   7 seconds ago       Up 7 seconds        0.0.0.0:5000->5000/tcp   rregistry


##3.4 配置docker服务器
注意：这个步骤在 docker服务器上操作

这台服务器就是在docker中运行刚才的tale 这个java项目的服务器，所以我们要在这台服各器上安装下docker。

1、安装Docker

[root@docker ~]# yum install -y yum-utils device-mapper-persistent-data lvm2

[root@docker ~]# yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo

[root@docker ~]# yum install docker-ce

2、配置镜像源为国内官方源

[root@docker ~]# vim /etc/docker/daemon.json

{  
  "registry-mirrors": [ "https://registry.docker-cn.com"],  
  "insecure-registries": [ "192.168.0.34:5000"]  
}  
注意书写格式为json格式，有严格的书写要求；  
第1行是国内镜像源，第2行是docker私有仓库地址；  
192.168.0.34就是docker私有仓库的地址，添加后连接docker私有仓库就是用http协议了。  

3、启动dokcer服务

[root@docker ~]# systemctl restart docker

4、部署JDK

由于后面运行java容器需要jdk环境，jdk如果放在容器中运行容器又相当重，所以就在宿主机上部署jdk，后面创建java容器的时候把宿主机的jdk路径挂载到容器就去。部署jdk很简单，解压就行：

[root@docker ~]# tar -zxvf jdk-8u45-linux-x64.tar.gz -C /usr/local/  
我们把它解压在docker这台宿主机的/usr/local目录中。

5、构建tale开源博客的基础镜像
其实，这个镜像随便在哪台服务器上构建都行，在本文中就直接在这台docker服务器构建了：

[root@docker /]# mkdir /data

[root@docker /]# cd /data

[root@docker data]# vim Dockerfile
		
		FROM centos:7
		ADD jdk-8u51-linux-x64.tar.gz /usr/local/src
		ENV JAVA_HOME=/usr/local/src/jdk1.8.0_51
		ENV PATH=$JAVA_HOME/bin:$PATH
		ENV CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
		ADD test_hello_war.jar /hello/
		WORKDIR /hello
		CMD ["java","-jar","test_hello_war.jar","--server.port=8080"]

此dockerfile安装了jdk 部署了jdk环境变量 向外暴露了8080端口  
注意：在/data目录中上传jdk-8u51-linux-x64.tar.gz  

[root@docker data]#docker build  -t hello-test-master .

[root@docker data]# docker images

对hello-test-master镜像打tag，并上传到registry私有仓库：

[root@docker /]#docker tag hello-test-master  192.168.0.34:5000/hello-test-master

[root@docker /]#docker push 192.168.0.34:5000/hello-test-master

##4部署jenkins

###注意：这个步骤在 jenkins 服务器上操作
4.1环境配置安装步骤

1、部署JDK  

[root@jenkins /]# tar -zxvf jdk-8u45-linux-x64.tar.gz -C /usr/local/

2、部署maven

[root@jenkins local]# tar -zxvf apache-maven-3.6.0-bin.tar.gz  -C /usr/local/

3、进入jenkins的安装目录

获取jenkins安装源文件  
wget -O /etc/yum.repos.d/jenkins.repo http://pkg.jenkins-ci.org/redhat/jenkins.repo  
导入公钥  
rpm --import https://jenkins-ci.org/redhat/jenkins-ci.org.key
安装jenkins  
yum install -y jenkins

可修改jenkins  端口为8080（默认8080我没改）
vim /etc/sysconfig/jenkins
JENKINS_PORT='8080'

4.修改jenkins的jdk的安装路径

vim  /etc/init.d/jenkins

candidates=""中的/usr/bin/java  修改为你的jdk安装路径    /usr/local/src/java/jdk1.8.0_45/bin/java  这是我的  

5.配置防火墙放心jenkins端口（8080）

重启jenkins

6.使用浏览器 http://192.168.0.202:8787

进入之后让你输入密码  密码在/var/lib/...提示的目录下
vim /var/lib/...

7.进入安装插件   可能是离线状态

	此时不要在当前窗口操作  
	再打开一个tab窗口输入http://192.168.0.32:8080/pluginManager/advanced  
	在最下方的URL中修改为http://updates.jenkins.io/update-center.json     
	提交  

8.重启jenkins

service jenkins restart  
在之前的离线窗口刷新 再次执行输入密码操作

9.页面会跳转等待后进入CUstomize Jenkins 页面 选择安装（选第一个默认安装）

	推荐安装（它就会自动安装）
	进入之后创建用户信息
	重启jenkins      service jenkins restart
	需要额外安装的插件

10.安装额外的插件

	进入系统管理--插件管理--可选插件--搜索以下插件  直接安装
	Maven Integration plugin
	Deploy to container plugin
	publish over ssh

4.2 系统设置

进入“系统管理” -- “系统设置”，主要是把docker这台服务器通过ssh的形式添加进来，后期部署dokcer容器

	参数说明：
	Name：192.168.0.33-docker
	Hostname：192.168.0.33
	Username：root
	Remote Directory:/data 这个意思是把代码发布192.168.0.33 /data目录中，需要手动创建个目录
	Use password authentication, or use a different key：勾选它
	Passphrase / Password：输入192.168.0.33这台docker服务器的密码

4.3 配置Maven、jdk、git环境

1、jdk配置
进“系统管理” -- "Global Tool Configuration"，添加jdk安装

	参数说明：
	别名： jdk （自定义就行）
	JAVA_HOME： /usr/local/jdk1.8.0_45   这个是你jenkins容器里的JDK路径，不是宿主机的JDK路径；

2、maven配置
进“系统管理” -- "Global Tool Configuration"，添加maven安装

	参数说明：和上面的形式jdk一样，MAVEN_HOME 的路径也是指向jenkins容器里的maven路径

3、git配置

	这里我没有动git的配置，让它为默认配置

4.4 配置java项目

1.构建maven项目
	
	新建任务 随便起个项目名  构建一个maven项目    确定 

2、源码管理
在“源码管理”项中选择Git 输入项目的git地址

3、构建触发器

    选这个 Build whenever a SNAPSHOT dependency is built
	还可以定时触发  * * * * *   （代表每分钟构建一次）

4、build配置
	
	Root POM输入： pom.xml
	Goals and options输入：clean package

5、构建后的配置

	在“Post Steps”选项中，配置如下操作：
		点击Add post-build step选择Send files orexecute commands over SSH
	Nmae:选择需要部署的docker服务器，前面我们SSH增加了一台，所以这里可以直接选择；
	source files：需要部署到目标服务的打包成果路径
	Remove prefilx: “Source files”配置的路径中要移除的前缀
	Remote directory：成果要发送到的远程目标服务目录路径，这个路径与第一步配置中的Remote Directory对应。
	Exec command：成果发送完成后，需要执行的命令，具体如下
			docker rm -f hytest
			docker rmi -f hello-test-master
			docker run 
			--name hytest 
			-p 8080:8080 
			-d hello-test-master

第1/2条命令：其实这两步你先可以不用加上，到了下一次构建的时候你测试一下不加这两条命令会有什么结果。如果构建失败，你在加上这两条命令，其实这两条命令是事先删除容器和镜像文件，然后通过第3条命令运行全新的容器。  
第3条命令：就是通过docker run来运行tale-test容器了，并把172.18.18.33-docker这台服务器的jdk和tale目录挂载到容器中。

6、测试

浏览器访问服务器地址
https://192.168.0.33:8080






docker+jenkins+git搭建优质文章
https://blog.51cto.com/ganbing/2085769

