package com.roguepnz.memeagg.source.reddit

import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.model.RawMeta
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel

class RedditContentSource : ContentSource {

    override fun listen(scope: CoroutineScope): ReceiveChannel<RawContent> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}