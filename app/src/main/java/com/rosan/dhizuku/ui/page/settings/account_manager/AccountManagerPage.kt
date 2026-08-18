package com.rosan.dhizuku.ui.page.settings.account_manager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Lock
import androidx.compose.material.icons.twotone.LockOpen
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rosan.dhizuku.R
import com.rosan.dhizuku.data.common.util.copy
import com.rosan.dhizuku.data.common.util.toast
import com.rosan.dhizuku.ui.page.settings.account_manager.components.AppCard
import com.rosan.dhizuku.ui.widget.EmptyState
import com.rosan.dhizuku.ui.theme.exclude
import org.json.JSONArray
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun AccountManagerPage(
    windowInsets: WindowInsets,
    userId: Int,
    onBack: () -> Unit,
    viewModel: AccountManagerViewModel = koinViewModel(parameters = { parametersOf(userId) })
) {
    LaunchedEffect(userId) {
        viewModel.dispatch(AccountManagerViewAction.Load)
    }
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(windowInsets.exclude(WindowInsetsSides.Bottom))
            .fillMaxSize(),
        contentWindowInsets = windowInsets.only(WindowInsetsSides.Bottom),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.TwoTone.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                title = { Text(stringResource(R.string.account_manager)) },
                actions = {
                    IconButton(onClick = {
                        try {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_SYNC_SETTINGS)
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            context.toast(R.string.error_open_sync_settings)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.TwoTone.Settings,
                            contentDescription = stringResource(R.string.action_open_sync_settings)
                        )
                    }
                    val isTogglingAll = viewModel.togglingPackages.isNotEmpty()
                    if (isTogglingAll) {
                        Box(
                            modifier = Modifier.padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        val allFrozen = viewModel.state.authenticators.isNotEmpty() &&
                                viewModel.state.authenticators.all { it.isFrozen }
                        IconButton(onClick = {
                            viewModel.dispatch(
                                if (allFrozen) AccountManagerViewAction.UnfreezeAll
                                else AccountManagerViewAction.FreezeAll
                            )
                        }) {
                            Icon(
                                imageVector = if (allFrozen) Icons.TwoTone.Lock else Icons.TwoTone.LockOpen,
                                contentDescription = stringResource(
                                    if (allFrozen) R.string.action_unfreeze_all
                                    else R.string.action_freeze_all
                                )
                            )
                        }
                    }
                }
            )
        },
    ) { innerPadding ->
        val pullToRefreshState = rememberPullToRefreshState()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .pullToRefresh(
                    state = pullToRefreshState,
                    isRefreshing = viewModel.state.loading,
                    onRefresh = {
                        viewModel.dispatch(AccountManagerViewAction.Load)
                    }
                )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (viewModel.state.authenticators.isEmpty()) {
                    item("empty_state") {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyState(message = stringResource(R.string.account_empty))
                        }
                    }
                } else {
                    items(
                        items = viewModel.state.authenticators,
                        key = { "${it.auth.packageName}_${it.auth.userId}_${it.auth.type}" },
                        contentType = { if (it.isFrozen) "frozen" else "active" }
                    ) { authenticator ->
                        AppCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            authenticator = authenticator,
                            isToggling = viewModel.togglingPackages.contains(authenticator.auth.packageName),
                            onFreezeToggle = { packageName, isFrozen ->
                                viewModel.dispatch(
                                    AccountManagerViewAction.FreezeToggle(packageName, isFrozen)
                                )
                            }
                        )
                    }
                }
            }

            PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullToRefreshState,
                isRefreshing = viewModel.state.loading
            )
        }
    }
}
