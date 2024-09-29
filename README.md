# 自定义的spring-cloud-starter包
# 包含功能
## 通用restful规范HTTP返回实体
使用范例
```
//common success
RestResponse.success()
//build your custom message
RestResponse.build("your data","your message","error stack",HttpStatus.YOURCODE,true)
```
## 基于consul+feign+loadBalance的灰度及就近服务转发
使用流程

1. 在服务配置文件中添加`custom.location=you location `标注本服务位置,` custom.host= you ip `标注本服务IP
2. 在服务的consul配置文件中对` instance-id `配置格式为 ` instance-id: ${spring.application.name}-${custom.location}-${custom.host} `(如果为灰度服务，需要变更为` instance-id: ${spring.application.name}-${custom.location}-${custom.host}-canary`)
3. 转发规则为:首先，服务会优先判断自身是否为灰度实例/请求头是否包含`x-canary=ture`字段，如果是则优先转发至灰度节点;其次，服务会优先转发至location相同的节点;最后，如果均不满足，服务会随机转发至一个节点