package com.rosan.dhizuku.di

import com.rosan.dhizuku.ui.page.settings.config.ConfigViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        ConfigViewModel(get())
    }
}