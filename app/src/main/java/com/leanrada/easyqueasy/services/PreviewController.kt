package com.leanrada.easyqueasy.services

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf

/**
 * Coordinates and controls preview overlays when the user is changing settings.
 *
 * A preview can be 'live', if the overlay service is running; or 'fake', a part of MainActivity. This class doesn't care about fake previews.
 */
object PreviewController {
    val overlayServiceActive: State<Boolean> = derivedStateOf { false }
    val requestingLivePreview: State<Boolean> = derivedStateOf { false }
}