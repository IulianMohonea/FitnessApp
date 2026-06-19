package com.fitnessapp.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun FitnessBottomNavigationBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar {
        MainTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    BottomTabIcon(tab = tab)
                },
                label = null,
                alwaysShowLabel = false
            )
        }
    }
}

@Composable
private fun BottomTabIcon(tab: MainTab) {
    val iconColor = LocalContentColor.current

    Canvas(
        modifier = Modifier
            .size(24.dp)
            .semantics { contentDescription = tab.label }
    ) {
        when (tab) {
            MainTab.Statistics -> {
                val barWidth = size.width * 0.16f
                val cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())

                drawRoundRect(
                    color = iconColor,
                    topLeft = Offset(size.width * 0.2f, size.height * 0.52f),
                    size = Size(barWidth, size.height * 0.34f),
                    cornerRadius = cornerRadius
                )
                drawRoundRect(
                    color = iconColor,
                    topLeft = Offset(size.width * 0.42f, size.height * 0.24f),
                    size = Size(barWidth, size.height * 0.62f),
                    cornerRadius = cornerRadius
                )
                drawRoundRect(
                    color = iconColor,
                    topLeft = Offset(size.width * 0.64f, size.height * 0.4f),
                    size = Size(barWidth, size.height * 0.46f),
                    cornerRadius = cornerRadius
                )
            }

            MainTab.Home -> {
                val path = Path().apply {
                    moveTo(size.width * 0.12f, size.height * 0.45f)
                    lineTo(size.width * 0.5f, size.height * 0.14f)
                    lineTo(size.width * 0.88f, size.height * 0.45f)
                    lineTo(size.width * 0.79f, size.height * 0.54f)
                    lineTo(size.width * 0.79f, size.height * 0.88f)
                    lineTo(size.width * 0.58f, size.height * 0.88f)
                    lineTo(size.width * 0.58f, size.height * 0.62f)
                    lineTo(size.width * 0.42f, size.height * 0.62f)
                    lineTo(size.width * 0.42f, size.height * 0.88f)
                    lineTo(size.width * 0.21f, size.height * 0.88f)
                    lineTo(size.width * 0.21f, size.height * 0.54f)
                    close()
                }

                drawPath(path = path, color = iconColor)
            }

            MainTab.History -> {
                val strokeWidth = size.minDimension * 0.09f
                val center = Offset(size.width * 0.5f, size.height * 0.5f)

                drawCircle(
                    color = iconColor,
                    radius = size.minDimension * 0.36f,
                    center = center,
                    style = Stroke(width = strokeWidth)
                )
                drawLine(
                    color = iconColor,
                    start = center,
                    end = Offset(size.width * 0.5f, size.height * 0.31f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = iconColor,
                    start = center,
                    end = Offset(size.width * 0.66f, size.height * 0.58f),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
