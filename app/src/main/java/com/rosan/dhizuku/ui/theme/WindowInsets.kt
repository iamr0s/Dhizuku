package com.rosan.dhizuku.ui.theme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.only

fun WindowInsets.exclude(sides: WindowInsetsSides): WindowInsets =
    this.exclude(this.only(sides))
