package com.roguepnz.memeagg.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import kotlin.reflect.KClass

object JSON {
    private val mapper: ObjectMapper = ObjectMapper()
        .registerModule(KotlinModule())

    fun stringify(v: Any): String = mapper.writeValueAsString(v)

    fun <T : Any> parse(json: String, type: KClass<T>): T = mapper.readValue(json, type.java)
}