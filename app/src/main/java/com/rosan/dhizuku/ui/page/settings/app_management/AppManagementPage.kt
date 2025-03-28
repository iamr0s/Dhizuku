package com.rosan.dhizuku.ui.page.settings.app_management

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
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
    val interactionSource = remember { MutableInteractionSource() }

    fun setEnabledChange(bool: Boolean = !data.enabled) {
        viewModel.setEnabled(data.applicationInfo.uid, bool)
    }

    ElevatedCard(
        onClick = ::setEnabledChange,
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
            val applicationInfo = data.applicationInfo
            val imageBitmap by AppIconCache.rememberImageBitmapState(applicationInfo)
            val text = applicationInfo.packageName
            val label = applicationInfo.loadLabel(LocalContext.current.packageManager).toString()
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
            Switch(
                checked = data.enabled,
                onCheckedChange = ::setEnabledChange,
                interactionSource = interactionSource
            )
        }
    }
}
