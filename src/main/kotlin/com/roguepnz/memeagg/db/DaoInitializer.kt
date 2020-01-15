package com.roguepnz.memeagg.db

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

object DaoInitializer {

    suspend fun init(daos: List<Dao>) {
        val jobs = daos.map {
            GlobalScope.async {
                it.init()
            }
        }
        jobs.forEach { it.join() }
    }
}