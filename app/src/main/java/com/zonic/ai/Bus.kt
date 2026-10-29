package com.zonic.ai

object Bus {
    var onMsg: ((String) -> Unit)? = null
}
