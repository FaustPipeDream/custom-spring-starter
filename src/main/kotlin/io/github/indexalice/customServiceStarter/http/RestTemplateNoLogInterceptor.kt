package io.github.indexalice.customServiceStarter.http

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse


/**
 * @Author indexalice
 * @Description 不记录日志的拦截器
 * @Date 10/8/24
 * @Version 1.0
 */
class RestTemplateNoLogInterceptor  : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        val response = execution.execute(request, body)
        return response
    }
}