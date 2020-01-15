package com.roguepnz.memeagg.source.html

import com.typesafe.config.Config


class HtmlConfig(config: Config) {
    val startUrl = config.getString("startUrl")
    val pageUrlTpl = config.getString("pageUrlTpl")

}