package com.leanrada.easyqueasy.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

fun AnnotatedString.Builder.appendBold(text: String) {
    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
        append(text)
    }
}