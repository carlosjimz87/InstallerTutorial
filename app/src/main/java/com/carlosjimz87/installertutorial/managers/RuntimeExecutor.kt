package com.carlosjimz87.installertutorial.managers

import androidx.lifecycle.MutableLiveData
import arrow.fx.IO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import timber.log.Timber

object RuntimeExecutor {
    sealed class RuntimeResult {
        data class Success(val value: Int, val output: String) : RuntimeResult()
        data class Failure(val error: String?) : RuntimeResult()
    }

    private val result = MutableLiveData<RuntimeResult>()

    fun execute(commands: Array<String>): IO<RuntimeResult> {
        return IO {

            withContext(Dispatchers.IO) {

                Timber.d("EXEC [[ ${commands.joinToString(" ")} ]]")
                try {

                    val p = Runtime.getRuntime().exec(commands)

                    val (stdOut, stdErr) = p.extractStreams()

                    if (stdErr.isNotEmpty() && stdErr.contentEquals("Error")) {
                        Timber.e("EXEC ERR: $stdErr")
                        result.value = RuntimeResult.Failure(stdErr)
                    }
                    result.value = RuntimeResult.Success(p.waitFor(), stdOut)
                } catch (e: Exception) {
                    return@withContext result.value ?: RuntimeResult.Failure(e.message)
                }

            }
            return@IO result.value ?: RuntimeResult.Failure(null)
        }
    }

    private fun Process.extractStreams(): Pair<String, String> {
        val stdOut = IOUtils.toString(this.inputStream, Charsets.UTF_8)
        val stdErr = IOUtils.toString(this.errorStream, Charsets.UTF_8)
        return Pair(stdOut, stdErr)
    }

    fun execute(commands: String): IO<RuntimeResult> {
        return this.execute(commands.split(" ").toTypedArray())
    }
}
