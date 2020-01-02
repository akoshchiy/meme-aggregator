package com.roguepnz.memeagg.util

import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.KClass

object JSON {
    private val mapper: ObjectMapper = ObjectMapper()

    fun stringify(v: Any): String = mapper.writeValueAsString(v)

    fun <T : Any> parse(json: String, type: KClass<T>): T = mapper.readValue(json, type.java)
}