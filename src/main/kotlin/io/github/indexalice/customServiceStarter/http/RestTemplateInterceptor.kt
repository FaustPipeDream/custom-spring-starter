package io.github.indexalice.customServiceStarter.http

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets


/**
 * @Author indexalice
 * @Description 记录日志的拦截器
 * @Date 10/8/24
 * @Version 1.0
 */
class RestTemplateInterceptor : ClientHttpRequestInterceptor {

    private val log = LoggerFactory.getLogger(RestTemplateInterceptor::class.java)

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        traceRequest(request, body)
        val response = execution.execute(request, body)
        traceResponse(response)
        return response
    }

    private fun traceRequest(request: HttpRequest, body: ByteArray) {
        log.info("==================== request begin ====================")
        log.info("URI          : ${request.uri}")
        log.info("Method       : ${request.method}")
        log.info("Headers      : ${request.headers}")
        log.info("Request boyd : {}", String(body, StandardCharsets.UTF_8))
        log.info("==================== request end ====================")
    }

    private fun traceResponse(response: ClientHttpResponse) {
        var inputStringBuilder = StringBuilder()
        val bufferedReader = BufferedReader(InputStreamReader(response.body, StandardCharsets.UTF_8))
        var line = bufferedReader.readLine()
        while (line != null) {
            inputStringBuilder.append(line)
            inputStringBuilder.append("\n")
            line = bufferedReader.readLine()
        }
        log.info("==================== response begin ====================")
        log.info("Status code   : ${response.statusCode}")
        log.info("Status text   : ${response.statusText}")
        log.info("Headers       : ${response.headers}")
        log.info("Response boyd : $inputStringBuilder")
        log.info("==================== response end ====================")
    }
}