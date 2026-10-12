package com.rosan.dhizuku.di

import com.rosan.dhizuku.ui.page.settings.activate.ActivateViewModel
import com.rosan.dhizuku.ui.page.settings.app_management.AppManagementViewModel
import com.rosan.dhizuku.ui.page.settings.account_manager.AccountManagerViewModel
import com.rosan.dhizuku.ui.page.settings.settings.SettingsViewModel
import com.rosan.dhizuku.ui.page.settings.user_manager.UserManagerViewModel

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
    viewModel {
        UserManagerViewModel()
    }
    viewModel { parameters ->
        AccountManagerViewModel(parameters.get())
    }
}
