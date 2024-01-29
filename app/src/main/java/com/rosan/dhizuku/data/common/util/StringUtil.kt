package com.rosan.dhizuku.data.common.util

import java.security.MessageDigest

fun ByteArray.digest(algorithm: String) =
    MessageDigest.getInstance(algorithm).digest(this)
