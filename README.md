# jd-ck

CK更新工具


修改配置：
  修改application.proeraties
  client.host=http://192.168.1.99:5700/open    //青龙地址
  client.id=                                   //青龙应用ID
  client.secret=                               //青龙应用key

  github.token=             //私有gitToken
  github.subscribes=smiek2121/scripts@master@青龙任务ID;smiek2121/scripts@master@青龙任务ID

  push.plus.token=推送ID，默认1对1   //不填写不推送
  
1. 本地安装 JAVA环境和MAVEN环境，安装教程请百度
2. 进入项目根目录，执行命令  mvn clean package  打包，
3. 在target目录下拷贝 adversereactions.jar 到你服务器目录
4. 服务器安装java运行环境
5. 切换到adversereactions.jar 所在目录，执行命令  nohup java -jar adversereactions.jar 2>&1 & 
6. 访问http://服务器ip:80/index  既可 (80端口自行修改application.proeraties)
