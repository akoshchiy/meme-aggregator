package com.roguepnz.memeagg.source.config

enum class SourceType(val code: Int) {
    NGAG_TAG(1),
    NGAG_GROUP(2),
    REDDIT(3),
    DEBESTE(4),
    ORSCHLURCH(5);

    companion object {
        fun fromCode(code: Int): SourceType {
            return when (code) {
                1 -> NGAG_TAG
                2 -> NGAG_GROUP
                3 -> REDDIT
                4 -> DEBESTE
                5 -> ORSCHLURCH
                else -> throw IllegalArgumentException("undefined code: $code")
            }
        }
    }
}