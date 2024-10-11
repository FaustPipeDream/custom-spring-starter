package io.github.indexalice.customServiceStarter.config

import com.ecwid.consul.v1.health.model.Check
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.loadbalancer.DefaultRequestContext
import org.springframework.cloud.client.loadbalancer.DefaultResponse
import org.springframework.cloud.client.loadbalancer.EmptyResponse
import org.springframework.cloud.client.loadbalancer.Request
import org.springframework.cloud.client.loadbalancer.RequestData
import org.springframework.cloud.client.loadbalancer.Response
import org.springframework.cloud.consul.discovery.ConsulServiceInstance
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier
import org.springframework.http.HttpHeaders
import org.springframework.util.CollectionUtils
import org.springframework.util.StringUtils
import reactor.core.publisher.Mono


/**
 * @Author indexalice
 * @Description 自定义loadBalancer的主体，实现了通过location的就近服务间调用转发及 x-gray的header的灰度转发
 * @Date 9/27/24
 * @Version 1.0
 */
class CustomLoadBalancer(
    private var serviceInstanceListSupplierProvider: ObjectProvider<ServiceInstanceListSupplier>,
    private var serviceId: String,
    private var springConfig: SpringConfig,
    private var customConfig: CustomConfig,
) :
    RoundRobinLoadBalancer(serviceInstanceListSupplierProvider, serviceId) {

    private val log = LoggerFactory.getLogger(CustomLoadBalancer::class.java)

    override fun choose(request: Request<*>): Mono<Response<ServiceInstance>> {
        val supplier = serviceInstanceListSupplierProvider.getIfAvailable { NoopServiceInstanceListSupplier() }
        return supplier[request].next().map { serviceInstances: List<ServiceInstance> ->
            getInstanceResponse(serviceInstances, request)
        }
    }

    companion object {
        const val CANARY_SUFFIX: String = "canary"
        const val CUSTOM_HEADER_CANARY: String = "x-canary"
        const val CUSTOM_HEADER_LOCATION: String = "x-location"
    }

    fun getInstanceResponse(
        instances: List<ServiceInstance>,
        request: Request<*>,
    ): Response<ServiceInstance> {
        val healthInstances = instances.filter { serviceInstance ->
            val checks = (serviceInstance as ConsulServiceInstance).healthService.checks
            checks.all { check -> check.status == Check.CheckStatus.PASSING }
        }

        //不存在健康实例直接返回
        if (CollectionUtils.isEmpty(healthInstances)) {
            log.warn("未找到可用实例{}", serviceId)
            return EmptyResponse()
        }

        val clientRequest = (request.context as DefaultRequestContext).clientRequest as RequestData
        val httpHeaders = clientRequest.headers

        //处理灰度转发
        if (isGray(httpHeaders)) {
            return DefaultResponse(getGrayInstance(healthInstances, httpHeaders))
        }
        //就近转发
        return DefaultResponse(getNearestInstance(healthInstances, httpHeaders))
    }

    /**
     * 本实例是否需要转发至灰度实例
     * 分为两种情况，本实例为灰度实例，或本实例收到了x-canary为true的请求
     */
    private fun isGray(httpHeaders: HttpHeaders): Boolean {
        val canary = httpHeaders.getFirst(CUSTOM_HEADER_CANARY)
        if (StringUtils.hasText(canary) && canary.toBoolean()) {
            return true
        }
        //用endsWith是防止一些服务名中包含了canary字段，而我们约定的instanceId结尾如果不包含canary则必定为host，不会出现误判
        return springConfig.instanceId.endsWith(CANARY_SUFFIX)
    }

    /**
     * 查找最近的实例，如果未找到则返回随机一个实例
     */
    private fun getNearestInstance(instances: List<ServiceInstance>, httpHeaders: HttpHeaders): ServiceInstance {


        val location = httpHeaders.getFirst(CUSTOM_HEADER_LOCATION)
        return instances.firstOrNull { serviceInstance ->
            //我们的FeignConfig确保了所有请求都包含CUSTOM_HEADER_LOCATION
            serviceInstance.instanceId.contains(location.toString())
        } ?: run {
            val instance = instances.random()
            log.warn("未找到就近实例，返回随机实例 location: ${location},service: ${instance.serviceId}")
            instance
        }
    }

    /**
     * 查找灰度实例，如果未找到则返回就近实例
     */
    private fun getGrayInstance(instances: List<ServiceInstance>, httpHeaders: HttpHeaders): ServiceInstance {
        return instances.firstOrNull { serviceInstance ->
            serviceInstance.instanceId.endsWith(CANARY_SUFFIX)
        } ?: run {
            var instance = getNearestInstance(instances, httpHeaders)
            log.warn("未找到灰度实例，返回就近实例 service: ${instance.serviceId}")
            instance
        }
    }
}
