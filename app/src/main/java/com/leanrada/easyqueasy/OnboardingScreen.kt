package com.leanrada.easyqueasy

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OnboardingScreen(onSelectDrawOverOtherApps: () -> Unit = {}) {
    BoxWithConstraints() {
        val constraints = this
        Column {
            Column(Modifier.padding(top = 24.dp, start = 40.dp, end = 40.dp)) {
                Text(
                    "Welcome to Easy Queasy!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "Choose your preferred mode.",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.size(16.dp))
                Text(
                    "Get helpful onscreen vestibular signals using any of the following methods. You can change your selection anytime.",
                    fontSize = 16.sp,
                )
            }
            Spacer(Modifier.size(24.dp))
            Row(
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                ModeCard(constraints) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Draw over other apps",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.size(8.dp))
                        ListItem(
                            Icons.Filled.Build,
                            buildAnnotatedString {
                                append("Requires ")
                                appendBold("Draw over other apps")
                                append(" permission.")
                            }
                        )
                        ListItem(
                            Icons.Filled.PlayArrow,
                            buildAnnotatedString {
                                append("Activate using the ")
                                appendBold("in-app button")
                                append(" or ")
                                appendBold("Quick Settings tile")
                                append(".")
                            }
                        )
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = onSelectDrawOverOtherApps,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Select mode", fontSize = 18.sp)
                        }
                    }
                }
                Spacer(Modifier.size(24.dp))
                ModeCard(constraints) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Accessibility service",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.size(8.dp))
                        ListItem(
                            Icons.Filled.Build,
                            buildAnnotatedString {
                                append("Requires ")
                                appendBold("Accessibility")
                                append(" permission.")
                            }
                        )
                        ListItem(
                            Icons.Filled.Lock,
                            AnnotatedString("Permission will only be used to draw over other apps.")
                        )
                        ListItem(
                            Icons.Filled.PlayArrow,
                            buildAnnotatedString {
                                append("Activate using Android ")
                                appendBold("accessibility shortcuts")
                                append(", like a floating button or a swipe gesture.")
                            }
                        )
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = onSelectDrawOverOtherApps,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Select mode", fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeCard(constraints: BoxWithConstraintsScope, content: @Composable ColumnScope.() -> Unit) {
    Card(
        Modifier
            .width(constraints.maxWidth - 72.dp)
            .fillMaxHeight()
    ) {
        content()
    }
}

private fun AnnotatedString.Builder.appendBold(text: String) {
    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
        append(text)
    }
}

@Composable
private fun ListItem(icon: ImageVector, text: AnnotatedString) {
    Row(Modifier.padding(vertical = 8.dp)) {
        Icon(
            imageVector = icon, "", modifier = Modifier
                .width(18.dp)
                .height(26.dp)
                .padding(vertical = 4.dp)
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text,
            fontSize = 18.sp,
        )
    }
}
