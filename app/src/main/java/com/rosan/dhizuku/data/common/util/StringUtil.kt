package com.rosan.dhizuku.data.common.util

import android.R
import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.TypedValue
import android.view.View
import androidx.core.net.toUri
import java.security.MessageDigest

fun ByteArray.digest(algorithm: String): ByteArray =
    MessageDigest.getInstance(algorithm).digest(this)

fun linkText(
    context: Context,
    text: CharSequence,
    label: String,
    url: String
): CharSequence {
    if (label.isEmpty()) return text

    val start = text.toString().indexOf(label)
    if (start < 0) return text

    return SpannableStringBuilder(text).apply {
        setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, url.toUri())
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }

                override fun updateDrawState(ds: TextPaint) {
                    val color = TypedValue()
                    context.theme.resolveAttribute(R.attr.colorAccent, color, true)
                    ds.color = color.data
                    ds.isUnderlineText = true
                }
            },
            start,
            start + label.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}
