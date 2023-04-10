package com.rosan.dhizuku.ui.widget.dialog

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun PositionDialog(
    properties: DialogProperties = DialogProperties(),
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = AlertDialogDefaults.containerColor,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    leftIcon: @Composable (() -> Unit)? = null,
    centerIcon: @Composable (() -> Unit)? = null,
    rightIcon: @Composable (() -> Unit)? = null,
    leftTitle: @Composable (() -> Unit)? = null,
    centerTitle: @Composable (() -> Unit)? = null,
    rightTitle: @Composable (() -> Unit)? = null,
    leftText: @Composable (() -> Unit)? = null,
    centerText: @Composable (() -> Unit)? = null,
    rightText: @Composable (() -> Unit)? = null,
    leftButton: @Composable (() -> Unit)? = null,
    centerButton: @Composable (() -> Unit)? = null,
    rightButton: @Composable (() -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    onClick = onDismissRequest,
                    indication = null,
                    interactionSource = remember {
                        MutableInteractionSource()
                    })
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clickable(
                        onClick = {},
                        indication = null,
                        interactionSource = remember {
                            MutableInteractionSource()
                        })
            ) {
                Surface(
                    modifier = modifier,
                    shape = shape,
                    color = containerColor,
                    tonalElevation = tonalElevation
                ) {
                    Box(
                        modifier = Modifier
                            .sizeIn(minWidth = MinWidth, maxHeight = MaxWidth)
                            .padding(DialogPadding)
                    ) {
                        var buttonHeightPx by remember {
                            mutableStateOf(0)
                        }
                        val buttonHeight = (buttonHeightPx / LocalDensity.current.density).dp
                        Box(modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .onSizeChanged {
                                buttonHeightPx = it.height
                            }) {
                            PositionChildWidget(
                                leftButton,
                                centerButton,
                                rightButton
                            ) { button ->
                                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
                                    val textStyle = MaterialTheme.typography.labelLarge
                                    ProvideTextStyle(value = textStyle) {
                                        button?.invoke()
                                    }
                                }
                            }
                        }
                        Column(
                            modifier = Modifier.padding(bottom = animateDpAsState(targetValue = buttonHeight).value)
                        ) {
                            PositionChildWidget(
                                leftIcon,
                                centerIcon,
                                rightIcon
                            ) { icon ->
                                CompositionLocalProvider(LocalContentColor provides iconContentColor) {
                                    Box(
                                        modifier = Modifier
                                            .padding(IconPadding)
                                            .align(Alignment.CenterHorizontally)
                                    ) {
                                        icon?.invoke()
                                    }
                                }
                            }
                            PositionChildWidget(
                                leftTitle,
                                centerTitle,
                                rightTitle
                            ) { title ->
                                CompositionLocalProvider(LocalContentColor provides titleContentColor) {
                                    val textStyle = MaterialTheme.typography.headlineSmall
                                    ProvideTextStyle(textStyle) {
                                        Box(
                                            modifier = Modifier
                                                .padding(TitlePadding)
                                                .align(Alignment.CenterHorizontally)
                                        ) {
                                            title?.invoke()
                                        }
                                    }
                                }
                            }
                            PositionChildWidget(
                                leftText,
                                centerText,
                                rightText
                            ) { text ->
                                CompositionLocalProvider(LocalContentColor provides textContentColor) {
                                    val textStyle = MaterialTheme.typography.bodyMedium
                                    ProvideTextStyle(textStyle) {
                                        Box(
                                            modifier = Modifier
                                                .weight(weight = 1f, fill = false)
                                                .padding(TextPadding)
                                        ) {
                                            text?.invoke()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PositionChildWidget(
    left: @Composable (() -> Unit)? = null,
    center: @Composable (() -> Unit)? = null,
    right: @Composable (() -> Unit)? = null,
    parent: @Composable ((child: @Composable (() -> Unit)?) -> Unit)
) {
    if (left == null && center == null && right == null) return
    Box(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.align(Alignment.TopStart)) {
            parent.invoke(left)
        }
        Box(modifier = Modifier.align(Alignment.TopCenter)) {
            parent.invoke(center)
        }
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            parent.invoke(right)
        }
    }
}

private val ButtonsMainAxisSpacing = 8.dp
private val ButtonsCrossAxisSpacing = 12.dp

private val DialogPadding = 24.dp
private val IconPadding = PaddingValues(bottom = 16.dp)
private val TitlePadding = PaddingValues(bottom = 16.dp)
private val TextPadding = PaddingValues(bottom = 24.dp)

private val MinWidth = 280.dp
private val MaxWidth = 560.dp