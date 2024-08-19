package com.rosan.dhizuku.ui.page.settings.app_management

data class AppManagementViewState(
    val data: List<AppManagementViewData> = emptyList(),
    val loading: Boolean = true,
)