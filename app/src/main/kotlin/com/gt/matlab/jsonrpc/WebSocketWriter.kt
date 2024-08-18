package com.gt.matlab.jsonrpc

import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import java.io.Writer

class WebSocketWriter(
    private val ws: Websocket,
    private val method: String,
) : Writer() {
    override fun close() {
    }

    override fun flush() {
    }

    override fun write(cbuf: CharArray, off: Int, len: Int) {
        val out = String(cbuf.sliceArray(off until (off + len)))
        ws.send(WsMessage(""" {"jsonrpc": "2.0", "method": "$method", "params": {"value": "$out"}} """))
    }
}