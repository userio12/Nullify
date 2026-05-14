package com.nullify.cleaner.di

import com.nullify.cleaner.ui.analyzer.AnalyzerViewModel
import com.nullify.cleaner.ui.appmanager.AppManagerViewModel
import com.nullify.cleaner.ui.corpse.CorpseViewModel
import com.nullify.cleaner.ui.dashboard.DashboardViewModel
import com.nullify.cleaner.ui.duplicate.DuplicateViewModel
import com.nullify.cleaner.ui.exclusion.ExclusionRuleViewModel
import com.nullify.cleaner.ui.junk.JunkViewModel
import com.nullify.cleaner.ui.onboarding.OnboardingViewModel
import com.nullify.cleaner.ui.recyclebin.RecycleBinViewModel
import com.nullify.cleaner.ui.scheduler.SchedulerViewModel
import org.koin.dsl.module

val uiModule = module {
    factory { DashboardViewModel(get(), get()) }
    factory { AnalyzerViewModel(get()) }
    factory { CorpseViewModel(get(), get()) }
    factory { JunkViewModel(get(), get()) }
    factory { DuplicateViewModel(get()) }
    factory { AppManagerViewModel(get(), get()) }
    factory { SchedulerViewModel(get()) }
    factory { ExclusionRuleViewModel(get()) }
    factory { OnboardingViewModel(get()) }
    factory { RecycleBinViewModel(get()) }
}
