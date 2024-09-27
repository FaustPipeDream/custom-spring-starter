package io.github.indexalice.customServiceStarter.config

import org.springframework.boot.context.properties.ConfigurationProperties


/**
 * @Author indexalice
 * @Description 自定义配置项
 * @Date 9/27/24
 * @Version 1.0
 */
@ConfigurationProperties("custom")
class CustomConfig(
    //配置当前服务的位置，对于手动配置一般是地点，对使用K8S部署可以为NODE的ID
    var location : String,
)