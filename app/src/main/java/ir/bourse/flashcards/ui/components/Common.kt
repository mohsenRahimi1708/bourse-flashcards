package ir.bourse.flashcards.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A card that flips around its vertical axis. The front is shown while the
 * rotation is under 90°, the back is counter-rotated so its text is not mirrored.
 */
@Composable
fun FlipCard(
    flipped: Boolean,
    modifier: Modifier = Modifier,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "flashcardFlip",
    )

    Card(
        modifier = modifier.graphicsLayer {
            rotationY = rotation
            cameraDistance = 16f * density
        },
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
    ) {
        if (rotation <= 90f) {
            Box(modifier = Modifier.fillMaxSize()) { front() }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f },
            ) { back() }
        }
    }
}

/** Label on the leading side, value on the trailing side. */
@Composable
fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    muted: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.alpha(if (muted) 0.7f else 1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(1f)
                .alpha(if (muted) 0.7f else 1f),
        )
    }
}

/** Small rounded label used for scores and counts. */
@Composable
fun TagChip(
    text: String,
    contentColor: Color,
    modifier: Modifier = Modifier,
    containerColor: Color = contentColor.copy(alpha = 0.12f),
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
        )
    }
}

/** Centered message used for empty lists and empty decks. */
@Composable
fun EmptyHint(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
