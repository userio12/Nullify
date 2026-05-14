package com.nullify.cleaner.di

import com.nullify.cleaner.data.repository.AppRepositoryImpl
import com.nullify.cleaner.data.repository.CleanupRepositoryImpl
import com.nullify.cleaner.data.repository.DuplicateRepositoryImpl
import com.nullify.cleaner.data.repository.ScheduleRepositoryImpl
import com.nullify.cleaner.data.repository.StorageRepositoryImpl
import com.nullify.cleaner.domain.repository.AppRepository
import com.nullify.cleaner.domain.repository.CleanupRepository
import com.nullify.cleaner.domain.repository.DuplicateRepository
import com.nullify.cleaner.domain.repository.ScheduleRepository
import com.nullify.cleaner.domain.repository.StorageRepository
import org.koin.dsl.module

val dataModule = module {
    single<StorageRepository> { StorageRepositoryImpl(get(), get()) }
    single<AppRepository> { AppRepositoryImpl(get(), get()) }
    single<CleanupRepository> { CleanupRepositoryImpl() }
    single<DuplicateRepository> { DuplicateRepositoryImpl() }
    single<ScheduleRepository> { ScheduleRepositoryImpl(get(), get(), get()) }
}
