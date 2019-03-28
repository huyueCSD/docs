#Docker简介
	
	Docker项目提供了构建在Linux内核功能之上，协同在一起的的高级工具。其目标是帮助开发和运维人员更容易地跨系统跨主机交付应用程序和他们的依赖。
	Docker通过Docker容器，一个安全的，基于轻量级容器的环境，来实现这个目标。这些容器由镜像创建，而镜像可以通过命令行手工创建或 者通过Dockerfile自动创建。

#Dockerfile

	Dockerfile是由一系列命令和参数构成的脚本，这些命令应用于基础镜像并最终创建一个新的镜像。
	它们简化了从头到尾的流程并极大的简化了部署工作。Dockerfile从FROM命令开始，紧接着跟随者各种方法，命令和参数。
	其产出为一个新的可以用于创建容器的镜像。

##Dockerfile语法
###Dockerfile 语法示例

Dockerfile语法由两部分构成，注释和命令+参数

	# Line blocks used for commenting
	command argument argument ..
一个简单的例子：

	# Print "Hello docker!"
	RUN echo "Hello docker!"
##Dockerfile命令
###常用命令
####ADD

	ADD命令有两个参数，源和目标。它的基本作用是从源系统的文件系统上复制文件到目标容器的文件系统。如果源是一个URL，那该URL的内容将被下载并复制到容器中。
	# Usage: ADD [source directory or URL] [destination directory]
	ADD /my_app_folder /my_app_folder 
####CMD

	和RUN命令相似，CMD可以用于执行特定的命令。和RUN不同的是，这些命令不是在镜像构建的过程中执行的，而是在用镜像构建容器后被调用。
	# Usage 1: CMD application "argument", "argument", ..
	CMD "echo" "Hello docker!"
	
	有三种用法：
	#第一种用法：运行一个可执行的文件并提供参数。
	CMD ["executable","param1","param2"] (exec form, this is the preferred form)
	#第二种用法：为ENTRYPOINT指定参数。
	CMD ["param1","param2"] (as default parameters to ENTRYPOINT)
	#第三种用法(shell form)：是以”/bin/sh -c”的方法执行的命令。
	CMD command param1 param2 (shell form)

	
	

####ENTRYPOINT

	配置容器启动后执行的命令，并且不可被 docker run 提供的参数覆盖。
	每个 Dockerfile 中只能有一个 ENTRYPOINT，当指定多个时，只有最后一个起效。
	ENTRYPOINT 帮助你配置一个容器使之可执行化，如果你结合CMD命令和ENTRYPOINT命令，你可以从CMD命令中移除“application”而仅仅保留参数，参数将传递给ENTRYPOINT命令。
	ENTRYPOINT echo

	CMD "Hello docker!"
	ENTRYPOINT echo
####ENV 

	ENV命令用于设置环境变量。这些变量以”key=value”的形式存在，并可以在容器内被脚本或者程序调用。这个机制给在容器中运行应用带来了极大的便利。
	# Usage: ENV key value
	ENV SERVER_WORKS 4
 
####EXPOSE

	EXPOSE用来指定端口，使容器内的应用可以通过端口和外界交互。
	# Usage: EXPOSE [port]
	EXPOSE 8080

####FROM

	FROM命令可能是最重要的Dockerfile命令。该命令定义了使用哪个基础镜像启动构建流程。
	基础镜像可以为任意镜像。如果基础镜像没有被发现，Docker将试图从Docker image index来查找该镜像。
	FROM命令必须是Dockerfile的首个命令。
	# Usage: FROM [image name]
	FROM ubuntu 

####MAINTAINER
	
	我建议这个命令放在Dockerfile的起始部分，虽然理论上它可以放置于Dockerfile的任意位置。这个命令用于声明作者，并应该放在FROM的后面。
	# Usage: MAINTAINER [name]
	MAINTAINER authors_name 

####RUN
	
	RUN命令是Dockerfile执行命令的核心部分。它接受命令作为参数并用于创建镜像。不像CMD命令，RUN命令用于创建镜像（在之前commit的层之上形成新的层）。
	# Usage: RUN [command]
	RUN aptitude install -y riak

####USER
	
	USER命令用于设置运行容器的UID。
	# Usage: USER [UID]
	USER 751

###VOLUME

	VOLUME命令用于让你的容器访问宿主机上的目录。
	# Usage: VOLUME ["/dir_1", "/dir_2" ..]
	VOLUME ["/my_files"]

####WORKDIR

	WORKDIR命令用于设置CMD指明的命令的运行目录。
	# Usage: WORKDIR /path
	WORKDIR ~/
####ONBUILD

	这是一个特殊的指令，它后面跟的是其它指令，比如 RUN , COPY等，而这些指令， 在当前镜像构建时并不会被执行。
	只有当以当前镜像为基础镜像，去构建下一级镜像的时候才会被执行。
####SHELL
	
	SHEELL指令允许默认的shell形式被命令形式覆盖。在Linux系统中默认shell形式为 ["/bin/sh", "-c"], 在 Windows上是["cmd", "/S", "/C"]。
	SHELL指令必须用Dockerfile中的JSON格式写入。SHELL指令在Windows上特别有用，其中有两个常用的和完全不同的本机shell：cmd和powershell，以及包括sh的备用shell。
	SHELL指令可以出现多次。每个SHELL指令都会覆盖所有以前的SHELL指令，并影响所有后续指令。

##如何使用Dockerfile生成镜像

	# Example: sudo docker build -t [name] .
	sudo docker build -t my_mongodb . 

##Dockerfile示例  创建一个Nginx的镜像

	# Set the base image to Ubuntu
	FROM ubuntu
	# File Author / Maintainer
	MAINTAINER Maintaner Name
	
	安装nginx
	# Install Nginx
	# Add application repository URL to the default sources
	RUN echo "deb http://archive.ubuntu.com/ubuntu/ raring main universe" >> /etc/apt/sources.list
	# Update the repository
	RUN apt-get update
	# Install necessary tools
	RUN apt-get install -y nano wget dialog net-tools
	# Download and Install Nginx
	RUN apt-get install -y nginx
	
	配置Nginx并且替换掉默认的配置文件
	# Remove the default Nginx configuration file
	RUN rm -v /etc/nginx/nginx.conf
	# Copy a configuration file from the current directory
	ADD nginx.conf /etc/nginx/
	# Append "daemon off;" to the beginning of the configuration
	RUN echo "daemon off;" >> /etc/nginx/nginx.conf
	# Expose ports
	EXPOSE 80
	# Set the default command to execute
	# when creating a new container
	CMD service nginx start
	
	使用Dockerfile自动构建Nginx容器
	
	因为我们命令Docker用当前目录的Nginx的配置文件替换默认的配置文件，我们要保证这个新的配置文件存在。在Dockerfile存在的目录下，创建nginx.conf：
	
	sudo nano nginx.conf
	然后用下述内容替换原有内容：
	worker_processes 1;
	events { worker_connections 1024; }
	http {
	     sendfile on;
	     server {
	         listen 80;
	         location / {
	              proxy_pass http://httpstat.us/;
	              proxy_set_header X-Real-IP $remote_addr;
	         }
	     }
	}
	让我们保存nginx.conf。之后我们就可以用Dockerfile和配置文件来构建镜像。

	