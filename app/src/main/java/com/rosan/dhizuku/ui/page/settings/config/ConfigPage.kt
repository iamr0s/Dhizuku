package com.rosan.dhizuku.ui.page.settings.config

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.rosan.dhizuku.R
import com.rosan.dhizuku.ui.theme.exclude
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigPage(
    windowInsets: WindowInsets,
    navController: NavController,
    viewModel: ConfigViewModel = getViewModel()
) {
    LaunchedEffect(true) {
        viewModel.dispatch(ConfigViewAction.Init)
    }

    Scaffold(
        modifier = Modifier
            .windowInsetsPadding(windowInsets.exclude(WindowInsetsSides.Bottom))
            .fillMaxSize(),
        contentWindowInsets = windowInsets.only(WindowInsetsSides.Bottom),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.config))
                }
            )
        },
    ) {
        Box(modifier = Modifier.padding(it)) {
            if (viewModel.state.initialized && viewModel.state.data.isEmpty())
                LottieWidget(
                    spec = LottieCompositionSpec.RawRes(R.raw.empty_state),
                    text = stringResource(R.string.empty_config_dsp)
                )
            else
                DataWidget(viewModel = viewModel)
        }
    }
}

@Composable
fun LottieWidget(
    spec: LottieCompositionSpec,
    text: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val composition by rememberLottieComposition(spec)
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever,
            )
            LottieAnimation(
                modifier = Modifier
                    .size(200.dp),
                composition = composition,
                progress = { progress }
            )
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun DataWidget(viewModel: ConfigViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(viewModel.state.data, { it.appEntity.uid }) {
            DataItemWidget(viewModel, it)
        }
    }
}

@Composable
fun DataItemWidget(viewModel: ConfigViewModel, data: ConfigViewState.Data) {
    fun changeIt(allowApi: Boolean) {
        viewModel.dispatch(ConfigViewAction.UpdateConfigByUID(data.appEntity.uid, allowApi))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                changeIt(!data.appEntity.allowApi)
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
            checked = data.appEntity.allowApi,
            onCheckedChange = {
                changeIt(it)
            }
        )
    }
}