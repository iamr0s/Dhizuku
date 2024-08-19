package com.rosan.dhizuku.ui.page.settings.activate

data class ActivateViewState(
    val data: List<ActivateViewData> = emptyList(),
    val loading: Boolean = true,
    val status: Status = Status.Waiting
) {
    sealed class Status {
        data object Waiting : Status()
        data object Running : Status()
        data class End(val error: Throwable? = null) : Status()
    }
}
