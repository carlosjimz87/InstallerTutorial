package com.carlosjimz87.installertutorial.managers

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class RuntimeExecutorTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @Test
    fun assert_simple_command_exec() = runBlocking {
        val UNAME_CMD = "uname -a"

        val result = RuntimeExecutor.execute(UNAME_CMD)
            .unsafeRunSync()
        println(result)
        assertThat(result).isInstanceOf(RuntimeExecutor.RuntimeResult.Success::class.java)
        when (result) {
            is RuntimeExecutor.RuntimeResult.Success -> {
                assertThat(result.output.split(" ").first()).isEqualTo("Linux")
            }
            else -> {}
        }
    }


    @Test
    fun assert_install_command_exec() = runBlocking {
        val path = "/sdcard/Downloads/copyfiles.apk"
        val UNAME_CMD = "pm install -r"

        val result = RuntimeExecutor.execute(UNAME_CMD)
            .unsafeRunSync()
        println(result)
        assertThat(result).isInstanceOf(RuntimeExecutor.RuntimeResult.Success::class.java)
        when (result) {
            is RuntimeExecutor.RuntimeResult.Success -> {
                assertThat(result.output.split(" ").first()).isEqualTo("Linux")
            }
            else -> {}
        }
    }
}