package com.rosan.dhizuku.ui.page.settings.config

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.rosan.dhizuku.R
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigPage(
    navController: NavController,
    viewModel: ConfigViewModel = getViewModel()
) {
    LaunchedEffect(true) {
        viewModel.dispatch(ConfigViewAction.Init)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.config))
                }
            )
        },
    ) {
        Box(modifier = Modifier.padding(it)) {
            DataWidget(viewModel = viewModel)
        }
    }
}

@Composable
fun DataWidget(viewModel: ConfigViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(viewModel.state.data) {
            DataItemWidget(viewModel, it)
        }
    }
}

@Composable
fun DataItemWidget(viewModel: ConfigViewModel, data: ConfigViewState.Data) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.dispatch(ConfigViewAction.UpdateConfigByUID(data.uid, !data.allowApi))
                }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Image(
                painter = rememberDrawablePainter(drawable = data.icon),
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.CenterVertically),
                contentDescription = data.label
            )
            Column(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            ) {
                Text(
                    text = data.label,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = data.packageName,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Switch(
                modifier = Modifier
                    .align(Alignment.CenterVertically),
                checked = data.allowApi,
                onCheckedChange = {
                    viewModel.dispatch(ConfigViewAction.UpdateConfigByUID(data.uid, it))
                }
            )
        }
    }
}