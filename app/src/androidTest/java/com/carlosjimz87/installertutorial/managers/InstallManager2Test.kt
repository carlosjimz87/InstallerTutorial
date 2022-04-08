package com.carlosjimz87.installertutorial.managers

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.carlosjimz87.installertutorial.ui.MainActivity
import com.carlosjimz87.installertutorial.utils.Manufacturers
import com.carlosjimz87.installertutorial.utils.Utils
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@LargeTest
@RunWith(AndroidJUnit4::class)
class InstallManager2InstrumentedTest {

    private lateinit var instrumentationContext: Context

    lateinit var manager: InstallManager2

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        instrumentationContext = InstrumentationRegistry.getInstrumentation().context
        manager = InstallManager2(instrumentationContext, false, Manufacturers.PHILLIPS)

        mockkObject(Utils)
        every { Utils.getManufacturer() } returns Manufacturers.PHILLIPS
    }

    @Test
    fun assert_utils(){
        assertThat(Utils.getManufacturer()).isEqualTo(Manufacturers.PHILLIPS)
    }

}