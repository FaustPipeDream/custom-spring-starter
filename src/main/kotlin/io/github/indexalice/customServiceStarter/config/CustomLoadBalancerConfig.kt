package io.github.indexalice.customServiceStarter.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment


/**
 * @Author indexalice
 * @Description 自定义负载均衡配置
 * @Date 9/29/24
 * @Version 1.0
 */
@Configuration
@EnableConfigurationProperties(SpringConfig::class, CustomConfig::class)
class CustomLoadBalancerConfig(
    val springConfig: SpringConfig,
    val customConfig: CustomConfig,
) {
    @Bean
    fun randomLoadBalancer(
        environment: Environment,
        //todo 为什么我在源码可以找到这个bean，却无法注入，实际运行时测试一下吧
       @Qualifier("loadBalancerClientFactory") loadBalancerClientFactory:LoadBalancerClientFactory
    ):ReactorLoadBalancer<ServiceInstance>{
        val name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME)
            ?: throw IllegalArgumentException("name cannot be null")
        return CustomLoadBalancer(loadBalancerClientFactory.getLazyProvider(name,ServiceInstanceListSupplier::class.java),
            name,springConfig,customConfig)
    }
}