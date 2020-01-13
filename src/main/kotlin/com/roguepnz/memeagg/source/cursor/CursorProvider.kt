package com.roguepnz.memeagg.source.cursor

import com.roguepnz.memeagg.source.model.RawContent


typealias CursorProvider =  suspend (Cursor?) -> CursorContent

data class Cursor(val cursor: String, val hasNext: Boolean)
data class CursorContent(val cursor: Cursor, val data: List<RawContent>)