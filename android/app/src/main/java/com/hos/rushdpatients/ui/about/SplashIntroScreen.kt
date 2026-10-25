package com.hos.rushdpatients.ui.about

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hos.rushdpatients.BuildConfig
import com.hos.rushdpatients.R
import com.hos.rushdpatients.ui.components.uiNumber
import com.hos.rushdpatients.ui.theme.UiSpacing
import com.hos.rushdpatients.ui.theme.LocalUiStyle
import com.hos.rushdpatients.ui.theme.uiContentGap
import com.hos.rushdpatients.ui.theme.uiContentPadding

/** MainActivity owns the fixed five-second deadline; holding only defers dismissal. */
@Composable
internal fun SplashIntroScreen(
    onSkip: () -> Unit,
    remainingSeconds: Int,
    held: Boolean,
    onHeldChange: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val currentOnSkip by rememberUpdatedState(onSkip)
    val currentOnHeldChange by rememberUpdatedState(onHeldChange)
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { entered = true }
    BoxWithConstraints(Modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(lerp(colors.background, colors.primary, 0.08f), colors.background, colors.background)))
        .pointerInput(Unit) {
            awaitEachGesture {
                // Observe before scrolling consumes input; do not consume reading/scroll gestures.
                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                var moved = false
                var releasedAt = down.uptimeMillis
                var pressed: Boolean
                currentOnHeldChange(true)
                try {
                    do {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        event.changes.firstOrNull { it.id == down.id }?.let {
                            releasedAt = it.uptimeMillis
                            if ((it.position - down.position).getDistance() > viewConfiguration.touchSlop) moved = true
                        }
                        pressed = event.changes.any { it.pressed }
                    } while (pressed)
                    // A hold defers dismissal until release; only a short stationary tap skips.
                    if (!moved && releasedAt - down.uptimeMillis < viewConfiguration.longPressTimeoutMillis) currentOnSkip()
                } finally {
                    currentOnHeldChange(false)
                }
            }
        }
        .semantics {
            onClick(label = "تخطي المقدمة") { onSkip(); true }
            customActions = listOf(CustomAccessibilityAction(
                if (held) "إنهاء القراءة" else "إبقاء المقدمة مفتوحة للقراءة"
            ) { onHeldChange(!held); true })
        }
        .safeDrawingPadding().padding(UiSpacing.section), contentAlignment = Alignment.Center) {
        // Keep the credits reachable on short windows and with enlarged text.
        val emblemSize = minOf(com.hos.rushdpatients.ui.theme.UiSize.splashEmblem, maxHeight * 0.25f)
            .coerceAtLeast(96.dp)
        Column(Modifier.widthIn(max = com.hos.rushdpatients.ui.theme.UiLayout.splashMaxWidth)
            .fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UiSpacing.largeSection)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(UiSpacing.screen)) {
                Surface(color = Color.White, shape = MaterialTheme.shapes.extraLarge,
                    border = BorderStroke(LocalUiStyle.current.normalBorder, colors.outlineVariant)) {
                    Image(painterResource(R.drawable.app_emblem_transparent), contentDescription = "شعار شعبة الطب النفسي",
                        contentScale = ContentScale.Fit, modifier = Modifier.size(emblemSize).padding(UiSpacing.screen))
                }
                Text("تقرير المناوبة", style = MaterialTheme.typography.headlineMedium, color = colors.primary,
                    textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
                Text("للطب النفسي", style = MaterialTheme.typography.titleMedium,
                    color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
            }
            Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large,
                color = colors.surface, border = BorderStroke(LocalUiStyle.current.normalBorder, colors.outlineVariant)) {
                Column {
                    SplashCreditSection("إشراف وتطوير", listOf(introSupervisorName), 0, entered)
                    Divider(Modifier.padding(horizontal = UiSpacing.largeSection), color = colors.outlineVariant)
                    SplashCreditSection("مساهمة تقنية · الأطباء", technicalContributors, 1, entered)
                    Divider(Modifier.padding(horizontal = UiSpacing.largeSection), color = colors.outlineVariant)
                    SplashCreditSection("فكرة Excel الأولية وبنية التقرير الأساسي", listOf(introOriginalAuthorName), 2, entered)
                }
            }
            Text("الإصدار ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            Surface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
                color = colors.surfaceVariant, contentColor = colors.onSurfaceVariant) {
                Text(if (held && remainingSeconds <= 0) "انتهى الوقت · ارفع إصبعك للمتابعة"
                    else if (held) "ارفع إصبعك عند انتهاء القراءة · المتبقي: ${uiNumber(remainingSeconds)} ث"
                    else "اضغط مطولاً للقراءة · لمسة سريعة للتخطي · المتبقي: ${uiNumber(remainingSeconds.coerceAtLeast(0))} ث",
                    modifier = Modifier.padding(UiSpacing.section),
                    textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SplashCreditSection(role: String, names: List<String>, order: Int, entered: Boolean) {
    val colors = MaterialTheme.colorScheme
    val style = LocalUiStyle.current
    val fraction by animateFloatAsState(targetValue = if (entered) 1f else 0f,
        animationSpec = tween(durationMillis = style.expandMillis,
            delayMillis = order * (style.expandMillis / 3)), label = "splashCreditEntry")
    val travel = with(LocalDensity.current) { UiSpacing.medium.toPx() }
    Column(modifier = Modifier.fillMaxWidth().graphicsLayer {
        alpha = fraction
        translationY = travel * (1f - fraction)
    }.semantics(mergeDescendants = true) {}
        .background(if (order == 0) colors.primary.copy(alpha = 0.06f) else Color.Transparent)
        .padding(uiContentPadding(PaddingValues(horizontal = UiSpacing.largeSection, vertical = UiSpacing.section))),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(uiContentGap())) {
        Text(role, style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        names.forEach { name ->
            Text(name, style = if (order == 0) MaterialTheme.typography.titleMedium
                else MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}
