package com.rosan.dhizuku.di

import com.rosan.dhizuku.ui.page.settings.activate.ActivateViewModel
import com.rosan.dhizuku.ui.page.settings.app_management.AppManagementViewModel
import com.rosan.dhizuku.ui.page.settings.settings.SettingsViewModel

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        AppManagementViewModel()
    }
    viewModel {
        ActivateViewModel()
    }
    viewModel {
        SettingsViewModel()
    }
}