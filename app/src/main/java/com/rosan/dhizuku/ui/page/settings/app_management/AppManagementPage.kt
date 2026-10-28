package com.rosan.dhizuku.ui.page.settings.app_management

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Block
import androidx.compose.material.icons.twotone.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import com.rosan.dhizuku.R
import com.rosan.dhizuku.ui.theme.AppIconCache
import com.rosan.dhizuku.ui.theme.exclude
import com.rosan.dhizuku.ui.theme.plus

import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagementPage(
    windowInsets: WindowInsets,
    navController: NavController,
    viewModel: AppManagementViewModel = koinViewModel()
) {
    LaunchedEffect(true) {
        viewModel.collect()
    }
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
                    Text(stringResource(R.string.home_app_management_title))
                }
            )
        }) {
        @Suppress("AnimatedContentLabel")
        AnimatedContent(targetState = viewModel.state.data.isEmpty()) { isEmpty ->
            val pullToRefreshState = rememberPullToRefreshState()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullToRefresh(state = pullToRefreshState,
                        isRefreshing = viewModel.state.loading,
                        onRefresh = {
                            viewModel.collectRepo()
                        }
                    )
            ) {
                if (isEmpty) Text(
                    stringResource(R.string.home_app_management_dsp),
                    modifier = Modifier
                        .padding(it + PaddingValues(16.dp))
                        .align(Alignment.Center)
                )
                else ItemsWidget(viewModel = viewModel, contentPadding = it)
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(it),
                    state = pullToRefreshState,
                    isRefreshing = viewModel.state.loading
                )
            }
        }
    }
}

@Composable
private fun ItemsWidget(
    viewModel: AppManagementViewModel,
    contentPadding: PaddingValues
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp) + contentPadding,
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(viewModel.state.data, key = { it.applicationInfo.uid }) {
            ItemWidget(
                viewModel = viewModel,
                data = it
            )
        }
    }
}

@Composable
private fun LazyItemScope.ItemWidget(
    viewModel: AppManagementViewModel,
    data: AppManagementViewData
) {
    val animatedCardColor by animateColorAsState(
        targetValue = if (data.blocked) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "card_color"
    )
    Card(
        modifier = Modifier
            .animateItem()
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = animatedCardColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        AnimatedContent(
            targetState = data.blocked,
            label = "blocked_item"
        ) { isBlocked ->
            if (isBlocked) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(data)
                        AppLabels(data, Modifier.weight(1f))
                    }

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.TwoTone.Block,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                stringResource(R.string.app_blocked_notice),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.setBlocked(data.applicationInfo.uid, false)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.TwoTone.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text(
                            stringResource(R.string.unblock_app),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(data)
                    AppLabels(data, Modifier.weight(1f))

                    Switch(
                        checked = data.enabled,
                        onCheckedChange = { enabled ->
                            viewModel.setEnabled(data.applicationInfo.uid, enabled)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                            uncheckedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    IconButton(
                        onClick = {
                            viewModel.setBlocked(data.applicationInfo.uid, true)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.TwoTone.Block,
                            contentDescription = stringResource(R.string.block_app),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppIcon(data: AppManagementViewData) {
    val imageBitmap = AppIconCache.rememberImageBitmapState(data.applicationInfo)
    Image(
        bitmap = imageBitmap.value,
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentDescription = null
    )
}

@Composable
private fun AppLabels(
    data: AppManagementViewData,
    modifier: Modifier = Modifier
) {
    val applicationInfo = data.applicationInfo
    val label = applicationInfo.loadLabel(LocalContext.current.packageManager).toString()
    Column(modifier = modifier) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = if (data.blocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            applicationInfo.packageName,
            style = MaterialTheme.typography.bodySmall,
            color = if (data.blocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
