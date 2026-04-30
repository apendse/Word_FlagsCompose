package com.aap.worldflags

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

class HiltTestRunner: AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        // We replace the application class with Hilt's test application
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}