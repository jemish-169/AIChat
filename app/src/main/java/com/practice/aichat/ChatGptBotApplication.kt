package com.practice.AIChat

import android.app.Application
import com.practice.AIChat.di.chatModule
import com.practice.AIChat.di.networkModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ChatGptBotApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin()
    }

    private fun startKoin() {
        startKoin {
            // Log Koin into Android logger
            androidLogger()
            // Reference Android context
            androidContext(this@ChatGptBotApplication)
            // Load modules
            modules(
                listOf(
                    networkModule,
                    chatModule
                )
            )
        }
    }
}