package io.github.indexalice.customServiceStarter.config

import io.github.indexalice.customServiceStarter.http.RestTemplateInterceptor
import io.github.indexalice.customServiceStarter.http.RestTemplateNoLogInterceptor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate


/**
 * @Author indexalice
 * @Description 使用rest template服务间调用时的配置
 * @Date 10/8/24
 * @Version 1.0
 */
@Configuration
@EnableConfigurationProperties(SpringConfig::class, CustomConfig::class)
//值得注意的是，我们的服务间调用统一使用feign，因此这里可以不设置
//如果需要用rest template进行服务间调用，则需要配置，同时应该也需要对header进行配置
//@LoadBalancerClients(defaultConfiguration = [CustomLoadBalancerConfig::class])
class RestTemplateConfig {
    @Bean
    @Primary
    fun restTemplate() : RestTemplate{
        val factory = SimpleClientHttpRequestFactory()
        //这个配置可以是我们可以多次使用响应主体
        var restTemplate = RestTemplate(BufferingClientHttpRequestFactory(factory))
        restTemplate.interceptors.add(RestTemplateInterceptor())
        return restTemplate
    }
    @Bean("restTemplateNoLog")
    fun restTemplateNoLog() : RestTemplate{
        val factory = SimpleClientHttpRequestFactory()
        //这个配置可以是我们可以多次使用响应主体
        var restTemplate = RestTemplate(BufferingClientHttpRequestFactory(factory))
        restTemplate.interceptors.add(RestTemplateNoLogInterceptor())
        return restTemplate
    }
}