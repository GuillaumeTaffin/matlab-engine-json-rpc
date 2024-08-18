package com.gt.matlab.jsonrpc

import com.mathworks.engine.EngineException
import com.mathworks.engine.MatlabExecutionException
import com.mathworks.engine.MatlabSyntaxException
import com.mathworks.engine.UnsupportedTypeException
import org.http4k.format.Json
import org.http4k.jsonrpc.ErrorHandler
import org.http4k.jsonrpc.ErrorMessage

object MatlabErrorHandler : ErrorHandler {
    override fun invoke(error: Throwable): ErrorMessage? = when (error) {
        is EngineException -> MatlabExceptionMessage(
            1,
            "Failure by MATLAB® to start, connect, terminate, or disconnect",
            error
        )

        is UnsupportedTypeException -> MatlabExceptionMessage(
            2,
            "Unsupported data type in input or output of MATLAB function",
            error
        )

        is MatlabExecutionException -> MatlabExceptionMessage(3, "Runtime error in MATLAB code", error)
        is MatlabSyntaxException -> MatlabExceptionMessage(4, "Syntax error in MATLAB expression", error)

        else -> null
    }

    private class MatlabExceptionMessage(
        code: Int,
        message: String,
        private val throwable: Throwable
    ) : ErrorMessage(code, message) {
        override fun <NODE> data(json: Json<NODE>): NODE =
            json.string(throwable.message ?: "Something went wrong in MATLAB.")

    }

}