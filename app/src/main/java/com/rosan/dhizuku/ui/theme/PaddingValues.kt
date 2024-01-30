package com.rosan.dhizuku.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection

@Composable
operator fun PaddingValues.plus(paddingValues: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection)
                + paddingValues.calculateStartPadding(layoutDirection),
        top = calculateTopPadding() + paddingValues.calculateTopPadding(),
        end = calculateEndPadding(layoutDirection)
                + paddingValues.calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + paddingValues.calculateBottomPadding()
    )
}