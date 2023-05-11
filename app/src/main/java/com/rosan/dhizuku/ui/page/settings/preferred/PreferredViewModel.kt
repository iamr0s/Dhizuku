package com.rosan.dhizuku.ui.page.settings.preferred

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class PreferredViewModel(private val navController: NavController) : ViewModel(), KoinComponent {
    private val context by inject<Context>()

    private val sharedPreferences: SharedPreferences = get {
        parametersOf("preferred")
    }

    var state by mutableStateOf(PreferredViewState())
        private set

    fun dispatch(action: PreferredViewAction) {
        when (action) {
            PreferredViewAction.Init -> init()
            is PreferredViewAction.ChangeToastWhenUsingDhizuku -> changeToastWhenUsingDhizuku(action.value)
        }
    }

    private var isInited = false

    private fun init() {
        synchronized(this) {
            if (isInited) return
            isInited = true
            collect()
        }
    }

    private var collectJob: Job? = null

    private fun collect() {
        collectJob?.cancel()
        collectJob = viewModelScope.launch(Dispatchers.IO) {
            callbackFlow {
                val listener =
                    SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                        trySendBlocking(key)
                    }
                sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
                for (key in sharedPreferences.all.keys) {
                    send(key)
                }
                awaitClose {
                    sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }.collect {
                state = when (it) {
                    "toast_when_using_dhizuku" -> state.copy(
                        toastWhenUsingDhizuku = sharedPreferences.getBoolean(
                            it,
                            state.toastWhenUsingDhizuku
                        )
                    )

                    else -> state
                }
            }
        }
    }

    private fun changeToastWhenUsingDhizuku(value: Boolean) {
        sharedPreferences.edit {
            putBoolean("toast_when_using_dhizuku", value)
        }
    }
}