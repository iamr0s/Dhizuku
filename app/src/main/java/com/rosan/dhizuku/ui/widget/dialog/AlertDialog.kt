//package com.rosan.installer.ui.widget.dialog
//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.sizeIn
//import androidx.compose.material3.AlertDialogDefaults
//import androidx.compose.material3.LocalContentColor
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.ProvideTextStyle
//import androidx.compose.material3.Surface
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.CompositionLocalProvider
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Shape
//import androidx.compose.ui.unit.Dp
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.Dialog
//import androidx.compose.ui.window.DialogProperties
//import com.google.accompanist.flowlayout.FlowRow
//
//@Composable
//fun AlertDialog(
//    onDismissRequest: () -> Unit,
//    confirmButton: @Composable () -> Unit,
//    modifier: Modifier = Modifier,
//    dismissButton: @Composable (() -> Unit)? = null,
//    icon: @Composable (() -> Unit)? = null,
//    title: @Composable (() -> Unit)? = null,
//    text: @Composable (() -> Unit)? = null,
//    view: @Composable (() -> Unit)? = null,
//    shape: Shape = AlertDialogDefaults.shape,
//    containerColor: Color = AlertDialogDefaults.containerColor,
//    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
//    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
//    textContentColor: Color = AlertDialogDefaults.textContentColor,
//    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
//    properties: DialogProperties = DialogProperties()
//) {
//    val realView: @Composable (() -> Unit)? = when {
//        view != null -> view
//        text != null -> {
//            @Composable {
//                Box(modifier = Modifier.padding(DialogWidgetPadding)) {
//                    text()
//                }
//            }
//        }
//        else -> null
//    }
//    Dialog(onDismissRequest = onDismissRequest, properties = properties) {
//        AlertDialogContent(
//            buttons = {
//                FlowRow(
//                    mainAxisSpacing = ButtonsMainAxisSpacing,
//                    crossAxisSpacing = ButtonsCrossAxisSpacing
//                ) {
//                    dismissButton?.invoke()
//                    confirmButton()
//                }
//            },
//            modifier = modifier,
//            icon = icon,
//            title = title,
//            view = realView,
//            shape = shape,
//            containerColor = containerColor,
//            tonalElevation = tonalElevation,
//            buttonContentColor = MaterialTheme.colorScheme.primary,
//            iconContentColor = iconContentColor,
//            titleContentColor = titleContentColor,
//            textContentColor = textContentColor
//        )
//    }
//}
//
//@Composable
//private fun AlertDialogContent(
//    buttons: @Composable () -> Unit,
//    modifier: Modifier = Modifier,
//    icon: (@Composable () -> Unit)?,
//    title: (@Composable () -> Unit)?,
//    view: @Composable (() -> Unit)?,
//    shape: Shape,
//    containerColor: Color,
//    tonalElevation: Dp,
//    buttonContentColor: Color,
//    iconContentColor: Color,
//    titleContentColor: Color,
//    textContentColor: Color,
//) {
//    Surface(
//        modifier = modifier,
//        shape = shape,
//        color = containerColor,
//        tonalElevation = tonalElevation
//    ) {
//        Column(
//            modifier = Modifier
//                .sizeIn(minWidth = MinWidth, maxHeight = MaxWidth)
//                .padding(vertical = DialogPadding)
//        ) {
//            icon?.let { icon ->
//                CompositionLocalProvider(LocalContentColor provides iconContentColor) {
//                    Box(
//                        modifier = Modifier
//                            .padding(DialogWidgetPadding)
//                            .padding(IconPadding)
//                            .align(Alignment.CenterHorizontally)
//                    ) {
//                        icon()
//                    }
//                }
//            }
//            title?.let { title ->
//                CompositionLocalProvider(LocalContentColor provides titleContentColor) {
//                    val textStyle = MaterialTheme.typography.headlineSmall
//                    ProvideTextStyle(textStyle) {
//                        Box(
//                            modifier = Modifier
//                                .padding(DialogWidgetPadding)
//                                .padding(TitlePadding)
//                                .align(
//                                    if (icon == null) Alignment.Start
//                                    else Alignment.CenterHorizontally
//                                )
//                        ) {
//                            title()
//                        }
//                    }
//                }
//            }
//            view?.let { view ->
//                CompositionLocalProvider(LocalContentColor provides textContentColor) {
//                    val textStyle = MaterialTheme.typography.bodyMedium
//                    ProvideTextStyle(textStyle) {
//                        Box(
//                            modifier = Modifier
//                                .weight(weight = 1f, fill = false)
//                                .padding(TextPadding)
//                                .align(Alignment.Start)
//                        ) {
//                            view()
//                        }
//                    }
//                }
//            }
//            Box(
//                modifier = Modifier
//                    .padding(DialogWidgetPadding)
//                    .align(Alignment.End)
//            ) {
//                CompositionLocalProvider(LocalContentColor provides buttonContentColor) {
//                    val textStyle = MaterialTheme.typography.labelLarge
//                    ProvideTextStyle(value = textStyle, content = buttons)
//                }
//            }
//        }
//    }
//}
//
//
//private val ButtonsMainAxisSpacing = 8.dp
//private val ButtonsCrossAxisSpacing = 12.dp
//
//// Paddings for each of the dialog's parts.
//private val DialogPadding = 24.dp
//private val DialogWidgetPadding = PaddingValues(horizontal = DialogPadding)
//private val IconPadding = PaddingValues(bottom = 16.dp)
//private val TitlePadding = PaddingValues(bottom = 16.dp)
//private val TextPadding = PaddingValues(bottom = 24.dp)
//
//private val MinWidth = 280.dp
//private val MaxWidth = 560.dp
