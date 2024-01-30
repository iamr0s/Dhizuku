package com.rosan.dhizuku.ui.page.settings.preferred

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.NotificationsActive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.rosan.dhizuku.R
import com.rosan.dhizuku.ui.theme.exclude
import com.rosan.dhizuku.ui.widget.setting.LabelWidget
import com.rosan.dhizuku.ui.widget.setting.SwitchWidget
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferredPage(
    windowInsets: WindowInsets,
    navController: NavController,
    viewModel: PreferredViewModel = getViewModel {
        parametersOf(navController)
    }
) {
    LaunchedEffect(true) {
        viewModel.dispatch(PreferredViewAction.Init)
    }

    val snackBarHostState = remember {
        SnackbarHostState()
    }
    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(windowInsets.exclude(WindowInsetsSides.Bottom))
            .fillMaxSize(),
        contentWindowInsets = windowInsets.only(WindowInsetsSides.Bottom),
        topBar = {
            TopAppBar(title = {
                Text(text = stringResource(id = R.string.preferred))
            })
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            item { LabelWidget(label = stringResource(id = R.string.basic)) }
            item { DataAllowTestOnlyWidget(viewModel) }
        }
    }
}

@Composable
fun DataAllowTestOnlyWidget(viewModel: PreferredViewModel) {
    SwitchWidget(
        icon = Icons.TwoTone.NotificationsActive,
        title = stringResource(id = R.string.toast_when_using_dhizuku),
        description = stringResource(id = R.string.toast_when_using_dhizuku_dsp),
        checked = viewModel.state.toastWhenUsingDhizuku,
        onCheckedChange = {
            viewModel.dispatch(PreferredViewAction.ChangeToastWhenUsingDhizuku(it))
        }
    )
}
