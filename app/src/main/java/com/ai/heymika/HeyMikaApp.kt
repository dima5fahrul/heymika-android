package com.ai.heymika

import android.app.Application
import android.content.Context
import com.ai.heymika.core.di.databaseModule
import com.ai.heymika.core.di.networkModule
import com.ai.heymika.core.di.repositoryModule
import com.ai.heymika.core.di.useCaseModule
import com.ai.heymika.di.appModule
import com.google.android.play.core.splitcompat.SplitCompat
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application entry point: starts Koin with the modules published by `:core` plus the app's own
 * [appModule], and installs SplitCompat so code from an on-demand feature module becomes
 * loadable in the *current* process (see [com.ai.heymika.navigation.DynamicFeatureInstaller]).
 */
class HeyMikaApp : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Without this, a module installed while the app is running is only visible to the
        // class loader after a cold restart, and Class.forName() on its entry point fails.
        SplitCompat.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.NONE)
            androidContext(this@HeyMikaApp)
            modules(
                listOf(
                    databaseModule,
                    networkModule,
                    repositoryModule,
                    useCaseModule,
                    appModule,
                ),
            )
        }
    }
}
