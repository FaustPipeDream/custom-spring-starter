package io.github.indexalice.customServiceStarter.http

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import com.fasterxml.jackson.core.JsonProcessingException

/**
 * @Author indexalice
 * @Description
 * @Date 9/26/24
 * @Version 1.0
 */
object RestResponse {
    var log: Logger = LoggerFactory.getLogger(RestResponse::class.java)

    @JvmOverloads
    fun success(data: Any = ""): ResponseEntity<*> {
        return buildResult(data, "", "", HttpStatus.OK)
    }

    @JvmOverloads
    fun successNoLog(data: Any = ""): ResponseEntity<*> {
        return buildResult(data, "", "", HttpStatus.OK, false)
    }

    @JvmOverloads
    fun badRequest(data: Any = "", message: String): ResponseEntity<*> {
        return buildResult(data, message, "", HttpStatus.BAD_REQUEST)
    }

    @JvmOverloads
    fun forbidden(data: Any = "", message: String): ResponseEntity<*> {
        return buildResult(data, message, "", HttpStatus.FORBIDDEN)
    }

    @JvmOverloads
    fun timeout(data: Any = "", message: String): ResponseEntity<*> {
        return buildResult(data, message, "", HttpStatus.REQUEST_TIMEOUT)
    }

    @JvmOverloads
    fun tooManyRequest(data: Any = "", message: String): ResponseEntity<*> {
        return buildResult(data, message, "", HttpStatus.TOO_MANY_REQUESTS)
    }

    @JvmOverloads
    fun internalError(data: Any = "", message: String, stack: String): ResponseEntity<*> {
        return buildResult(data, message, stack, HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @JvmOverloads
    fun internalError(data: Any = "", message: String, stack: Exception): ResponseEntity<*> {
        return buildResult(data, message, pasteTrace(stack), HttpStatus.INTERNAL_SERVER_ERROR)
    }


    /**
     * @Description 构建通用返回值
     * @Param data 数据
     * @Param message 信息
     * @Param stack 堆栈
     * @Param code 错误码
     * @Param withLog 是否记录日志
     * @Return 构建好的ResponseEntity类
     * @Author indexalice
     * @Date 9/26/24
     */
    @JvmOverloads
    fun buildResult(
        data: Any,
        message: String,
        stack: String,
        code: HttpStatus,
        withLog: Boolean = true
    ): ResponseEntity<*> {
        val om = ObjectMapper()
        val ret: String
        val headers = HttpHeaders()
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        try {
            ret = if (code == HttpStatus.OK) {
                om.writeValueAsString(data)
            } else {
                if ("" == data) {
                    om.writeValueAsString(ErrorBean("", message, stack))
                } else {
                    om.writeValueAsString(ErrorBean(data, message, stack))
                }
            }
        } catch (e: JsonProcessingException) {
            log.error("json解析失败", e)
            return ResponseEntity("", headers, HttpStatus.INTERNAL_SERVER_ERROR)
        }
        if (withLog) {
            log.info(
                """
                Rest Response ${code.name}(${code.value()}):
                $ret
            """.trimIndent()
            )
        }
        return ResponseEntity(ret, headers, code)
    }

    private fun pasteTrace(e: Exception): String {
        val trace = e.stackTrace
        val sb = StringBuilder()
        for (t in trace) {
            sb.append("  ").append(t.toString()).append(System.lineSeparator())
        }
        return sb.toString()
    }
}