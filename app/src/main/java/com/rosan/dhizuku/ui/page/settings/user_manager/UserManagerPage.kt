package com.rosan.dhizuku.ui.page.settings.user_manager

import android.os.Process
import androidx.compose.foundation.layout.size
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.twotone.Warning
import androidx.compose.material.icons.twotone.Person
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rosan.dhizuku.R
import com.rosan.dhizuku.data.common.util.help
import com.rosan.dhizuku.data.account.entity.UserEntity
import com.rosan.dhizuku.ui.widget.EmptyState
import com.rosan.dhizuku.ui.theme.exclude
import org.koin.androidx.compose.koinViewModel

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun UserManagerPage(
    windowInsets: WindowInsets,
    onNavigateToAccount: (userId: Int) -> Unit,
    onBack: () -> Unit,
    viewModel: UserManagerViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.dispatch(UserManagerViewAction.Load)
    }

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
                title = { Text(stringResource(R.string.user_manager)) }
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
                        viewModel.dispatch(UserManagerViewAction.Load)
                    }
                )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val cause = viewModel.state.cause
                if (cause != null) {
                    item("error_state") {
                        val errorMsg = cause.help() ?: cause.localizedMessage ?: cause.toString()
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyState(message = errorMsg)
                        }
                    }
                } else {
                    items(viewModel.state.users, key = { it.id }) { user ->
                        var alpha by remember { mutableStateOf(0f) }
                        UserCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .graphicsLayer(
                                    alpha = animateFloatAsState(
                                        targetValue = alpha,
                                        animationSpec = spring(stiffness = 100f)
                                    ).value
                                ),
                            viewModel = viewModel,
                            onNavigateToAccount = { onNavigateToAccount(user.id) },
                            user = user
                        )
                        SideEffect { alpha = 1f }
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

@Composable
private fun UserCard(
    modifier: Modifier = Modifier,
    viewModel: UserManagerViewModel,
    onNavigateToAccount: (userId: Int) -> Unit,
    user: UserEntity
) {
    var showing by remember { mutableStateOf(false) }

    val curUserId = try {
        val handle = Process.myUserHandle()
        val method = handle.javaClass.getMethod("getIdentifier")
        method.invoke(handle) as Int
    } catch (_: Exception) { 0 }

    ElevatedCard(
        onClick = { onNavigateToAccount(user.id) },
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.TwoTone.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.name ?: stringResource(R.string.user_name_default),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "ID: ${user.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            if (curUserId == user.id) {
                Text(
                    text = stringResource(R.string.account_manager),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                IconButton(onClick = { showing = true }) {
                    Icon(
                        imageVector = Icons.TwoTone.Delete,
                        contentDescription = stringResource(R.string.remove),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (curUserId != user.id) {
        DeleteUserDialog(
            viewModel = viewModel,
            showing = showing,
            onDismissRequest = { showing = false },
            user = user
        )
    }
}

@Composable
private fun DeleteUserDialog(
    viewModel: UserManagerViewModel,
    showing: Boolean,
    onDismissRequest: () -> Unit,
    user: UserEntity
) {
    if (!showing) return
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(imageVector = Icons.TwoTone.Warning, contentDescription = null) },
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(user.id.toString(), color = MaterialTheme.colorScheme.primary)
                Text(user.name ?: stringResource(R.string.user_name_default))
            }
        },
        text = { Text(stringResource(R.string.delete_user_warning)) },
        confirmButton = {
            TextButton(onClick = {
                viewModel.dispatch(UserManagerViewAction.Remove(user))
                onDismissRequest()
            }) {
                Text(stringResource(R.string.delete_user_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.delete_user_cancel))
            }
        }
    )
}
