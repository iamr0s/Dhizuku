package com.rosan.dhizuku.ui.widget.dialog

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    leftSubtitle: @Composable (() -> Unit)? = null,
    centerSubtitle: @Composable (() -> Unit)? = null,
    rightSubtitle: @Composable (() -> Unit)? = null,
    leftText: @Composable (() -> Unit)? = null,
    centerText: @Composable (() -> Unit)? = null,
    rightText: @Composable (() -> Unit)? = null,
    leftContent: @Composable (() -> Unit)? = null,
    centerContent: @Composable (() -> Unit)? = null,
    rightContent: @Composable (() -> Unit)? = null,
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
                        // set the button always in bottom
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
                                        Box(
                                            modifier = Modifier
                                                .padding(ButtonPadding)
                                        ) {
                                            button?.invoke()
                                        }
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
                                    ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
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
                                leftSubtitle,
                                centerSubtitle,
                                rightSubtitle
                            ) { subtitle ->
                                CompositionLocalProvider(LocalContentColor provides titleContentColor) {
                                    val textStyle = MaterialTheme.typography.bodyMedium
                                    ProvideTextStyle(textStyle) {
                                        Box(
                                            modifier = Modifier
                                                .padding(SubtitlePadding)
                                                .align(Alignment.CenterHorizontally)
                                        ) {
                                            subtitle?.invoke()
                                        }
                                    }
                                }
                            }
                            val contentMode =
                                leftContent != null || centerContent != null || rightContent != null
                            PositionChildWidget(
                                if (contentMode) leftContent else leftText,
                                if (contentMode) centerContent else centerText,
                                if (contentMode) rightContent else rightText
                            ) { text ->
                                CompositionLocalProvider(LocalContentColor provides textContentColor) {
                                    val textStyle = MaterialTheme.typography.bodyMedium
                                    ProvideTextStyle(textStyle) {
                                        Box(
                                            modifier = Modifier
                                                .weight(weight = 1f, fill = false)
                                                .padding(if (contentMode) ContentPadding else TextPadding)
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

private val DialogSinglePadding = 24.dp

private val DialogPadding = PaddingValues(top = DialogSinglePadding, bottom = DialogSinglePadding)
private val IconPadding =
    PaddingValues.Absolute(left = DialogSinglePadding, right = DialogSinglePadding, bottom = 12.dp)
private val TitlePadding =
    PaddingValues.Absolute(left = DialogSinglePadding, right = DialogSinglePadding, bottom = 12.dp)
private val SubtitlePadding =
    PaddingValues.Absolute(left = DialogSinglePadding, right = DialogSinglePadding, bottom = 12.dp)
private val TextPadding =
    PaddingValues.Absolute(left = DialogSinglePadding, right = DialogSinglePadding, bottom = 12.dp)
private val ContentPadding =
    PaddingValues.Absolute(bottom = 12.dp)
private val ButtonPadding = PaddingValues(horizontal = DialogSinglePadding)

private val MinWidth = 280.dp
private val MaxWidth = 560.dp