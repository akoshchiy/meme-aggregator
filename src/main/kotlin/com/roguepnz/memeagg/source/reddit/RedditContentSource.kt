package com.roguepnz.memeagg.source.reddit

import com.roguepnz.memeagg.source.ContentSource
import com.roguepnz.memeagg.source.model.RawContent
import com.roguepnz.memeagg.source.model.RawMetadata
import kotlinx.coroutines.channels.ReceiveChannel

class RedditContentSource : ContentSource {

    override fun start() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun contentChannel(): ReceiveChannel<RawContent> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun metaUpdateChannel(): ReceiveChannel<RawMetadata> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}