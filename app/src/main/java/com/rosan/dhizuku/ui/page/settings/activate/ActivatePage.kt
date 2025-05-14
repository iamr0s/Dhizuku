package com.rosan.dhizuku.ui.page.settings.activate

import android.content.ComponentName

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import com.rosan.dhizuku.R
import com.rosan.dhizuku.ui.page.settings.SettingsRoute
import com.rosan.dhizuku.ui.theme.AppIconCache
import com.rosan.dhizuku.ui.theme.exclude
import com.rosan.dhizuku.ui.theme.plus

import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivatePage(
    windowInsets: WindowInsets,
    navController: NavController,
    mode: SettingsRoute.Activate.Mode,
    viewModel: ActivateViewModel = koinViewModel()
) {
    LaunchedEffect(true) {
        viewModel.collect()
    }

    val compState = remember {
        mutableStateOf<ComponentName?>(null)
    }

    LaunchedEffect(viewModel.state.data) {
        compState.value = viewModel.state.data.find { it.enabled }?.admin?.component
    }

    val title = stringResource(
        when (mode) {
            SettingsRoute.Activate.Mode.Dhizuku -> R.string.home_dhizuku_title
            SettingsRoute.Activate.Mode.Shizuku -> R.string.home_shizuku_title
        }
    )

    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(windowInsets.exclude(WindowInsetsSides.Bottom))
            .fillMaxSize(),
        contentWindowInsets = windowInsets.only(WindowInsetsSides.Bottom),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.TwoTone.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                title = {
                    Text(title)
                }
            )
        },
        floatingActionButton = {
            val comp by compState
            AnimatedVisibility(
                visible = comp != null,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                ExtendedFloatingActionButton(
                    icon = {
                        Icon(
                            imageVector = Icons.TwoTone.Check,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(stringResource(R.string.confirm))
                    },
                    onClick = {
                        comp?.let {
                            viewModel.activate(mode = mode, comp = it)
                        }
                    }
                )
            }
        }) {
        val pullToRefreshState = rememberPullToRefreshState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pullToRefresh(state = pullToRefreshState,
                    isRefreshing = viewModel.state.loading,
                    onRefresh = {
                        viewModel.collectData()
                    }
                )
        ) {
            ItemsWidget(viewModel = viewModel, contentPadding = it, state = compState)
            PullToRefreshDefaults.Indicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(it),
                state = pullToRefreshState,
                isRefreshing = viewModel.state.loading
            )
        }
    }

    if (viewModel.state.status == ActivateViewState.Status.Waiting) return
    AlertDialog(onDismissRequest = {
        viewModel.cancel()
    }, confirmButton = {
        val comp by compState
        val isRunning = viewModel.state.status !is ActivateViewState.Status.End
        val isSuccess =
            !isRunning && (viewModel.state.status as ActivateViewState.Status.End).error == null

        if (isRunning || !isSuccess) TextButton(onClick = { viewModel.cancel() }) {
            Text(stringResource(R.string.cancel))
        }
        if (isRunning) return@AlertDialog
        if (!isSuccess) TextButton(onClick = {
            viewModel.activate(mode = mode, comp = comp!!)
        }) {
            Text(stringResource(R.string.retry))
        } else TextButton(onClick = { navController.navigateUp() }) {
            Text(stringResource(R.string.finish))
        }
    }, title = {
        Text(title)
    }, text = {
        @Suppress("AnimatedContentLabel")
        AnimatedContent(
            viewModel.state.status,
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            if (it == ActivateViewState.Status.Running) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            else if (it is ActivateViewState.Status.End) {
                Text(
                    if (it.error == null) stringResource(R.string.home_status_owner_granted)
                    else it.error.toString(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    })
}

@Composable
private fun ItemsWidget(
    viewModel: ActivateViewModel,
    contentPadding: PaddingValues,
    state: MutableState<ComponentName?>
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp) + contentPadding,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(viewModel.state.data, key = { it.admin.component.flattenToShortString() }) {
            ItemWidget(
                viewModel = viewModel,
                data = it,
                state = state
            )
        }
    }
}

@Composable
private fun LazyItemScope.ItemWidget(
    viewModel: ActivateViewModel,
    data: ActivateViewData,
    state: MutableState<ComponentName?>
) {
    val interactionSource = remember { MutableInteractionSource() }

    fun setCheckedChange(bool: Boolean = state.value?.compareTo(data.admin.component) != 0) {
        state.value = if (bool) data.admin.component
        else null
    }

    ElevatedCard(
        onClick = ::setCheckedChange,
        interactionSource = interactionSource,
        modifier = Modifier
            .animateItem()
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = state.value?.compareTo(data.admin.component) == 0,
                onCheckedChange = ::setCheckedChange,
                interactionSource = interactionSource
            )

            val packageManager = LocalContext.current.packageManager
            val imageBitmap by AppIconCache.rememberImageBitmapState(data.admin)
            val label = data.admin.loadLabel(packageManager).toString()
            val text = data.admin.component.flattenToShortString()
            Image(
                bitmap = imageBitmap,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterVertically),
                contentDescription = null
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
