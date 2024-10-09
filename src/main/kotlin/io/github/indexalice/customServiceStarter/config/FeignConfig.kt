package io.github.indexalice.customServiceStarter.config

import feign.RequestInterceptor
import feign.RequestTemplate
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration


/**
 * @Author indexalice
 * @Description 使用feign进行服务间调用时的自定义配置
 * @Date 10/8/24
 * @Version 1.0
 */
@Configuration
@EnableConfigurationProperties(SpringConfig::class)
class FeignConfig (
    val springConfig: SpringConfig,
) : RequestInterceptor{
    override fun apply(requestTemplate: RequestTemplate) {
        //设置灰度字段,如果本服务是灰度的，则设置
        //这是为了标志一些由后端服务自行发起的请求是否为灰度
        if(springConfig.instanceId.endsWith(CustomLoadBalancer.CANARY_SUFFIX)){
            requestTemplate.header("x-canary","true")
        }
        //todo 需要测试是否需要逐次转发请求头
    }

}