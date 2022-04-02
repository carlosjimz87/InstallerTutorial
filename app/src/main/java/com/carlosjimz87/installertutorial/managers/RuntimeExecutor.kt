package com.carlosjimz87.installertutorial.managers

import arrow.fx.IO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import timber.log.Timber

object RuntimeExecutor {

    fun execute(commands: Array<String>): IO<Int> {

        return IO {
            withContext(Dispatchers.IO) {
                Timber.d("EXEC [[ ${commands.joinToString(" ")} ]]")

                val p = Runtime.getRuntime().exec(commands)

                val stdOut = IOUtils.toString(p.inputStream, Charsets.UTF_8)
                val stdErr = IOUtils.toString(p.errorStream, Charsets.UTF_8)
                return@withContext if (stdErr.isNotEmpty()) {
                    throw Exception(stdErr)
                } else {
                    Timber.w(stdOut)
                    p.waitFor()
                }
            }
        }
    }

    fun execute(commands: String): IO<Int> {
        return this.execute(commands.split(" ").toTypedArray())
    }
}