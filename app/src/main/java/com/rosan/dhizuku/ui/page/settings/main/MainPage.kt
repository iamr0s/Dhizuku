package com.rosan.dhizuku.ui.page.settings.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Home
import androidx.compose.material.icons.twotone.RoomPreferences
import androidx.compose.material.icons.twotone.SettingsSuggest
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.rosan.dhizuku.R
import com.rosan.dhizuku.ui.page.settings.config.ConfigPage
import com.rosan.dhizuku.ui.page.settings.home.HomePage
import com.rosan.dhizuku.ui.page.settings.preferred.PreferredPage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, DelicateCoroutinesApi::class)
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
                    count = data.size,
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
