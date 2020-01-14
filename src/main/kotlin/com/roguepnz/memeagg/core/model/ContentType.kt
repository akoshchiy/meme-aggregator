package com.roguepnz.memeagg.core.model

enum class ContentType(val code: Int) {
    IMAGE(1),
    VIDEO(2);

    companion object {
        fun fromCode(code: Int): ContentType {
            return when (code) {
                1 -> IMAGE
                2 -> VIDEO
                else -> throw IllegalArgumentException("undefined type : $code")
            }
        }
    }
}