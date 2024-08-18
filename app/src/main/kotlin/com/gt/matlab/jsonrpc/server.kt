package com.gt.matlab.jsonrpc

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mathworks.engine.MatlabEngine
import org.http4k.core.HttpHandler
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.format.ConfigurableJackson
import org.http4k.format.asConfigurable
import org.http4k.format.withStandardMappings
import org.http4k.jsonrpc.JsonRpc
import org.http4k.lens.Query
import org.http4k.routing.websockets
import org.http4k.routing.ws.bind
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.websocket.Websocket
import org.http4k.websocket.WsMessage
import org.http4k.websocket.WsResponse
import java.util.concurrent.CompletableFuture.runAsync


object CustomJackson : ConfigurableJackson(
    KotlinModule.Builder().build()
        .asConfigurable()
        .withStandardMappings()
        .done()
        .deactivateDefaultTyping()
        .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, false)
        .configure(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS, false)
)

fun main() {

    val engineNamePath = Query.optional("engineName")

    val ws = websockets(
        "/connect" bind { req: Request ->
            WsResponse { ws: Websocket ->

                val engineName = engineNamePath(req)

                val engine = if (engineName == null) {
                    MatlabEngine.startMatlab()
                } else {
                    MatlabEngine.connectMatlab(engineName)
                }

                ws.onClose { engine.disconnect() }

                ws.send(WsMessage(""" {"jsonrpc": "2.0", "method": "connected", "params": {"message" : "Successfully connected to MATLAB."}} """))

                val matlab = Matlab(
                    engine,
                    WebSocketWriter(ws, "stdout"),
                    WebSocketWriter(ws, "stderr")
                )

                val rpcHandler: HttpHandler = createHandler(matlab)

                ws.onMessage { m ->
                    runAsync {
                        val response = rpcHandler(
                            Request(POST, "/rpc")
                                .header("Content-Type", "application/json")
                                .body(m.body)
                        )

                        ws.send(WsMessage(response.body))
                    }
                }
            }
        }
    )

    ws.asServer(Undertow(9000)).start()
}

private fun createHandler(matlab: Matlab): HttpHandler = JsonRpc.auto(CustomJackson, MatlabErrorHandler) {
    method("eval", handler(matlab::eval))
    method("feval", handler(matlab::feval))
    method("getVariable", handler(matlab::getVariable))
    method("putVariable", handler(matlab::putVariable))
}