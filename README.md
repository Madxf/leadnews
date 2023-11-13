# leadnews
基于SpringBoot + SpringCloud + Redis + Mysql的头条项目

微服务项目：
1.admin端
（1）admin用户登陆(基于JWT令牌)
（2）对用户的审核
（3）对文章的人工审核
（4）对敏感词的管理
（5）对文章频道的管理
2.app端（user, article)
（1）app用户登陆
（2）app端文章的查询
（3）根据输入的关键字进行查询(基于ES)
（4）保存搜索记录
（5）根据输入关键字进行提示(基于MongoDB)
（6）提供新增文章的远程调用(基于Feign)
3.Wemedia端
（1）wemedia用户登陆
（2）素材上传(基于MinIO)
（3）文章自动审核(基于KafKa进行消息文章审核的传递，百度云提供的文本、图片自动审核API)
（4）文章发布(远程调用article端的发布功能)
4.schedule
创建延时任务，以便文章的审核和提交(异步)