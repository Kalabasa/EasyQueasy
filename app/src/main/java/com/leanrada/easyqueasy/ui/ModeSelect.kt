package com.leanrada.easyqueasy.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.leanrada.easyqueasy.AppDataClient
import com.leanrada.easyqueasy.R

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ModeSelect(
    modifier: Modifier = Modifier,
    appData: AppDataClient,
    onSelectDrawOverOtherApps: () -> Unit = {},
    onSelectAccessibilityService: () -> Unit = {},
) {
    val loaded by appData.rememberLoaded()
    val onboarded by appData.rememberOnboarded()

    if (!loaded) return

    BoxWithConstraints {
        val constraints = this

        if (!onboarded) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = "",
                modifier = Modifier
                    .size(constraints.maxWidth)
                    .align(Alignment.Center)
                    .offset(
                        x = constraints.maxWidth * -0.3f,
                        y = constraints.maxWidth * -0.6f,
                    )
                    .alpha(0.15f),
            )
        }

        Column(
            verticalArrangement = Arrangement.Center,
            modifier = modifier.verticalScroll(rememberScrollState())
        ) {
            Column(Modifier.padding(top = 24.dp, start = 40.dp, end = 40.dp)) {
                if (!onboarded) {
                    Text(
                        "Ease that quease!",
                        style = MaterialTheme.typography.displaySmall,
                    )
                    Spacer(Modifier.size(16.dp))
                }
                Text(
                    "Choose your preferred mode",
                    style =
                    if (onboarded)
                        MaterialTheme.typography.titleLarge
                    else
                        MaterialTheme.typography.titleMedium,
                )
                if (!onboarded) {
                    Spacer(Modifier.size(16.dp))
                    Text(
                        "Get helpful onscreen vestibular signals using any of the following methods. You can change your selection anytime.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Spacer(Modifier.size(24.dp))

            val pagerState = rememberPagerState { 2 }

            HorizontalPager(
                state = pagerState,
                pageSpacing = 16.dp,
                pageSize = PageSize.Fixed(constraints.maxWidth - 80.dp),
                contentPadding = PaddingValues(horizontal = 40.dp),
                verticalAlignment = Alignment.Top,
            ) { page ->
                when (page) {
                    0 -> ModeCard(color = ModeCardColor.PRIMARY) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Draw over other apps",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .fillMaxWidth(),
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
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Select mode",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    }

                    1 -> ModeCard(color = ModeCardColor.TERTIARY) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Accessibility service",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .fillMaxWidth(),
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
                                onClick = onSelectAccessibilityService,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary,
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "Select mode",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color =
                        if (pagerState.currentPage == iteration)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.secondaryContainer
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}

enum class ModeCardColor {
    PRIMARY,
    TERTIARY
}

@Composable
private fun ModeCard(
    color: ModeCardColor,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = when (color) {
            ModeCardColor.PRIMARY -> CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )

            ModeCardColor.TERTIARY -> CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        },
    ) {
        content()
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
            style = MaterialTheme.typography.bodyMedium
        )
    }
}