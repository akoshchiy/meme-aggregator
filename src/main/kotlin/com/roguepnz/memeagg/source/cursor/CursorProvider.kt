package com.roguepnz.memeagg.source.cursor

import com.roguepnz.memeagg.source.model.RawContent


//typealias CursorProvider =

interface CursorProvider {
    fun next(cursor: Cursor): CursorContent
    fun next(): CursorContent
}

data class Cursor(val cursor: String, val hasNext: Boolean)
data class CursorContent(val cursor: Cursor, val data: List<RawContent>)