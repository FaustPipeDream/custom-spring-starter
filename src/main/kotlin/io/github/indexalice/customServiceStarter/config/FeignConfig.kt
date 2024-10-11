package io.github.indexalice.customServiceStarter.config

import feign.RequestInterceptor
import feign.RequestTemplate
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes


/**
 * @Author indexalice
 * @Description 使用feign进行服务间调用时的自定义配置
 * @Date 10/8/24
 * @Version 1.0
 */
@Configuration
@EnableConfigurationProperties(SpringConfig::class, CustomConfig::class)
class FeignConfig (
    val springConfig: SpringConfig,
    val customConfig: CustomConfig
) : RequestInterceptor{
    override fun apply(requestTemplate: RequestTemplate) {
        //设置灰度字段,如果本服务是灰度的，则设置
        //这是为了标志一些由后端服务自行发起的请求是否为灰度
        if(springConfig.instanceId.endsWith(CustomLoadBalancer.CANARY_SUFFIX)){
            requestTemplate.header("x-canary","true")
        }
        //对于无x-location的请求，则在调用链的第一个本地服务中新增这个header
        val attributes  = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        val headers = attributes.request.headerNames.toList()
       if("x-location" !in headers){
           requestTemplate.header("x-location",customConfig.location)
       }
    }

}