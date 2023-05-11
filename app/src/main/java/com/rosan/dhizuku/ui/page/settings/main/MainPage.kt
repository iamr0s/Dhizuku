package com.rosan.dhizuku.ui.page.settings.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Home
import androidx.compose.material.icons.twotone.RoomPreferences
import androidx.compose.material.icons.twotone.SettingsSuggest
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.rosan.dhizuku.R
import com.rosan.dhizuku.ui.page.settings.config.ConfigPage
import com.rosan.dhizuku.ui.page.settings.home.HomePage
import com.rosan.dhizuku.ui.page.settings.preferred.PreferredPage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class, ExperimentalFoundationApi::class)
@Composable
fun MainPage(navController: NavController) {
    val pagerState = rememberPagerState()
    val currentPage = pagerState.currentPage
    fun onPageChanged(page: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            pagerState.scrollToPage(page = page)
        }
    }

    val data = arrayOf(
        NavigationData(
            icon = Icons.TwoTone.Home,
            label = stringResource(id = R.string.home)
        ) {
            HomePage(navController = navController)
        },
        NavigationData(
            icon = Icons.TwoTone.RoomPreferences,
            label = stringResource(id = R.string.config)
        ) {
            ConfigPage(navController = navController)
        },
        NavigationData(
            icon = Icons.TwoTone.SettingsSuggest,
            label = stringResource(id = R.string.preferred)
        ) {
            PreferredPage(navController = navController)
        }
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val isLandscapeScreen = maxHeight / maxWidth > 1.4
        Row(modifier = Modifier.fillMaxSize()) {
            if (!isLandscapeScreen) {
                ColumnNavigation(
                    data = data,
                    currentPage = currentPage,
                    onPageChanged = { onPageChanged(it) }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                HorizontalPager(
                    pageCount = data.size,
                    state = pagerState,
                    userScrollEnabled = false,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    data[it].content.invoke()
                }
                if (isLandscapeScreen) {
                    RowNavigation(
                        data = data,
                        currentPage = currentPage,
                        onPageChanged = { onPageChanged(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun RowNavigation(
    data: Array<NavigationData>,
    currentPage: Int,
    onPageChanged: (Int) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize()
    ) {
        data.forEachIndexed { index, navigationData ->
            NavigationBarItem(
                selected = currentPage == index,
                onClick = { onPageChanged(index) },
                icon = {
                    Icon(
                        imageVector = navigationData.icon,
                        contentDescription = navigationData.label
                    )
                },
                label = {
                    Text(text = navigationData.label)
                },
                alwaysShowLabel = false
            )
        }
    }
}

@Composable
fun ColumnNavigation(
    data: Array<NavigationData>,
    currentPage: Int,
    onPageChanged: (Int) -> Unit
) {
    NavigationRail(
        modifier = Modifier
            .fillMaxHeight()
            .wrapContentSize()
    ) {
        Spacer(
            modifier = Modifier
                .weight(1f)
        )
        data.forEachIndexed { index, navigationData ->
            NavigationRailItem(
                selected = currentPage == index,
                onClick = { onPageChanged(index) },
                icon = {
                    Icon(
                        imageVector = navigationData.icon,
                        contentDescription = navigationData.label
                    )
                },
                label = {
                    Text(text = navigationData.label)
                },
                alwaysShowLabel = false
            )
        }
    }
}
