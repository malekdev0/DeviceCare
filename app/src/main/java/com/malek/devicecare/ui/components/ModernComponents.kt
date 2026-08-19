package com.malek.devicecare.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import com.malek.devicecare.domain.model.UsagePoint

@Composable
fun UsageGraph(
    points: List<UsagePoint>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas

        val width = size.width
        val height = size.height
        val maxPoints = 20
        val stepX = width / (maxPoints - 1)

        val path = Path()
        points.forEachIndexed { index, point ->
            val x = index * stepX
            val y = height - (point.value / 100f * height)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface
    val isDark = surface.red + surface.green + surface.blue < 1.5f
    val backgroundColor = if (isDark) Color(0x33FFFFFF) else Color(0x66FFFFFF)
    val borderColor = if (isDark) Color(0x1AFFFFFF) else Color(0x33000000)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp)
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed && enabled) 0.95f else 1f, label = "buttonScale")

    val surface = MaterialTheme.colorScheme.surface
    val isDark = surface.red + surface.green + surface.blue < 1.5f

    val gradient = if (enabled) {
        if (isDark) {
            Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF15192D),
                    Color(0xFF0A0E21)
                )
            )
        } else {
            Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary
                )
            )
        }
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color.Gray.copy(alpha = 0.5f),
                Color.Gray.copy(alpha = 0.3f)
            )
        )
    }

    val borderModifier = if (isDark && enabled) {
        Modifier.border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(28.dp))
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(gradient)
            .then(borderModifier)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                onClick = if (enabled) onClick else ({ }),
                enabled = enabled
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}
