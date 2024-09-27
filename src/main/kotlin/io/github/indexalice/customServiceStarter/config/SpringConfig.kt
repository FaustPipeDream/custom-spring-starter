package io.github.indexalice.customServiceStarter.config

import org.springframework.boot.context.properties.ConfigurationProperties


/**
 * @Author indexalice
 * @Description spring配置项
 * @Date 9/27/24
 * @Version 1.0
 */
@ConfigurationProperties("spring.cloud.consul.discovery")
class SpringConfig(
    var serviceName : String
)