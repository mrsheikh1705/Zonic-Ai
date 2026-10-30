package com.zonic.ai

object Bus {
    var onMsg: ((String, String) -> Unit)? = null
}
