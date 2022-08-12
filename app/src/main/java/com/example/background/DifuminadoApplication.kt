

package com.example.background

import android.app.Application
import androidx.work.Configuration
import timber.log.Timber
import timber.log.Timber.DebugTree
import android.util.Log.DEBUG
import android.util.Log.ERROR

class DifuminadoApplication() : Application(), Configuration.Provider {

    override fun getWorkManagerConfiguration(): Configuration {
        return if (BuildConfig.DEBUG) {
            Configuration.Builder()
                    .setMinimumLoggingLevel(DEBUG)
                    .build()
        } else {
            Configuration.Builder()
                    .setMinimumLoggingLevel(ERROR)
                    .build()
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        }
    }
}
