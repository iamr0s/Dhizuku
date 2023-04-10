package com.rosan.dhizuku.ui.page.settings.preferred

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.rosan.dhizuku.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferredPage(navController: NavController) {
    val snackBarHostState = remember {
        SnackbarHostState()
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.preferred))
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
        }
    }
}
