package com.esrac.snakegame.gui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.esrac.snakegame.Game.SnakeGame

@Composable
fun SnakeBoard(game: SnakeGame, cellSizeDp: Int = 30) {
    val cellSize = cellSizeDp.dp
    Canvas(modifier = Modifier) {
        game.snake.forEachIndexed { index, point ->
            drawRect(
                color = if (index == 0) Color.Yellow else Color.Green,
                topLeft = Offset(point.x * cellSize.toPx(), point.y * cellSize.toPx()),
                size = Size(cellSize.toPx(), cellSize.toPx())
            )
        }
        val food = game.food
        drawRect(
            color = Color.Red,
            topLeft = Offset(food.x * cellSize.toPx(), food.y * cellSize.toPx()),
            size = Size(cellSize.toPx(), cellSize.toPx())
        )
    }
}
