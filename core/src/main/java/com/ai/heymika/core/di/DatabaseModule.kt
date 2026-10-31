package com.ai.heymika.core.di

import androidx.room.Room
import com.ai.heymika.core.data.source.local.LocalDataSource
import com.ai.heymika.core.data.source.local.datastore.SettingPreferences
import com.ai.heymika.core.data.source.local.room.AiLearnDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule =
    module {
        single {
            Room
                .databaseBuilder(
                    androidContext(),
                    AiLearnDatabase::class.java,
                    "AiLearn.db",
                ).fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
        factory { get<AiLearnDatabase>().materialDao() }
        factory { get<AiLearnDatabase>().generatedContentDao() }
        single { LocalDataSource(get(), get()) }
        single { SettingPreferences(androidContext()) }
    }
