package com.roguepnz.meme

import io.ktor.routing.Routing


typealias Routes = Routing.() -> Unit

interface KtorController {
    fun routing(): Routes
}