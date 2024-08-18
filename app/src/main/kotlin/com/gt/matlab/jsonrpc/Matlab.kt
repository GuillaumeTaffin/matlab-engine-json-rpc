package com.gt.matlab.jsonrpc

import com.mathworks.engine.MatlabEngine
import java.io.Writer

class Matlab(
    private val matlabEngine: MatlabEngine,
    private val stdout: Writer,
    private val stderr: Writer
) {

    fun eval(command: EvalCommand) {
        matlabEngine.eval(command.value, stdout, stderr)
    }

    fun feval(command: FEvalCommand): Any {
        return matlabEngine.feval<Any>(command.nlhs, command.func, stdout, stderr, *command.args) ?: Unit
    }

    fun getVariable(command: GetVariableCommand): Any {
        return matlabEngine.getVariable<Any>(command.name) ?: Unit
    }

    fun putVariable(command: PutVariableCommand) {
        return matlabEngine.putVariable(command.name, command.value)
    }

    data class GetVariableCommand(val name: String)

    data class PutVariableCommand(val name: String, val value: Any)

    data class EvalCommand(val value: String)

    data class FEvalCommand(val nlhs: Int, val func: String, val args: Array<Any>) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FEvalCommand

            if (nlhs != other.nlhs) return false
            if (func != other.func) return false
            if (!args.contentEquals(other.args)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = nlhs
            result = 31 * result + func.hashCode()
            result = 31 * result + args.contentHashCode()
            return result
        }
    }

}