package com.esrac.snakegame.gui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.esrac.snakegame.Game.Direction
import com.esrac.snakegame.Game.SnakeGame
import com.esrac.snakegame.Rl.QLearningAgent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.ui.unit.times
import kotlin.math.pow
import kotlin.math.sqrt

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
@Composable
fun GameScreen() {
    val cellSize = 30.dp
    val gridSize = 10

    val game = remember { SnakeGame(gridSize, gridSize) }
    val agent = remember { QLearningAgent(
        actions = listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)
    ) }

    var isTraining by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Animasyonlu yılan segmentleri (mutable)
    val snakePositions = remember {
        mutableStateListOf<Animatable<Offset, AnimationVector2D>>().apply {
            addAll(game.snake.map { Animatable(Offset.Zero, Offset.VectorConverter) })
        }
    }
    val foodOffsetY = remember { Animatable(0f) }

    // Yılan animasyonu güncelleme
    fun updateSnakePositions() {
        coroutineScope.launch {
            val cellSizePx = with(density) { cellSize.toPx() }

            // Yeni segment ekle
            if (snakePositions.size < game.snake.size) {
                val newSegments = game.snake.drop(snakePositions.size).map {
                    Animatable(Offset(it.x * cellSizePx, it.y * cellSizePx), Offset.VectorConverter)
                }
                snakePositions.addAll(newSegments)
            }

            // Tüm segmentleri animasyonla güncelle (boyut eşitleme ile güvenli)
            val snakeSize = minOf(snakePositions.size, game.snake.size)
            for (i in 0 until snakeSize) {
                val target = Offset(game.snake[i].x * cellSizePx, game.snake[i].y * cellSizePx)
                snakePositions[i].animateTo(target, tween(150))
            }
        }
    }

    fun resetSnakePositions() {
        coroutineScope.launch {
            val cellPx = with(density) { cellSize.toPx() }
            snakePositions.clear()
            snakePositions.addAll(
                game.snake.map { Animatable(Offset(it.x * cellPx, it.y * cellPx), Offset.VectorConverter) }
            )
        }
    }




    // Yem zıplama animasyonu
    LaunchedEffect(game.food) {
        foodOffsetY.animateTo(
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        Text(
            text = "Snake RL 🐍",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(gridSize * cellSize)
                .background(Color.Black, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSizePx = with(density) { cellSize.toPx() }

                // Yılan çizimi
                snakePositions.forEachIndexed { index, animPos ->
                    val center = animPos.value + Offset(cellSizePx / 2, cellSizePx / 2)

                    val gradient = if (index == 0) {
                        val distance = sqrt(
                            ((game.food.x - game.snake[index].x).toFloat().pow(2)) +
                                    ((game.food.y - game.snake[index].y).toFloat().pow(2))
                        )
                        val maxDistance = sqrt((gridSize.toFloat().pow(2)) * 2)
                        val brightness = (1f - (distance / maxDistance)).coerceIn(0f, 1f)
                        val alphaValue = 0.5f + 0.5f * brightness

                        Brush.radialGradient(
                            colors = listOf(
                                Color.Yellow.copy(alpha = 1f),
                                Color(0xFFB8860B).copy(alpha = alphaValue)
                            ),
                            center = center,
                            radius = cellSizePx / 2
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(Color.Green, Color(0xFF006400)),
                            center = center,
                            radius = cellSizePx / 2
                        )
                    }

                    drawCircle(
                        brush = gradient,
                        radius = cellSizePx / 2,
                        center = center
                    )

                    // Kafa gözleri
                    if (index == 0) {
                        val eyeRadius = cellSizePx / 10
                        val offset = cellSizePx / 4
                        val (eye1Offset, eye2Offset) = when (game.lastDirection) {
                            Direction.UP -> Pair(Offset(-offset/2, -offset/2), Offset(offset/2, -offset/2))
                            Direction.DOWN -> Pair(Offset(-offset/2, offset/2), Offset(offset/2, offset/2))
                            Direction.LEFT -> Pair(Offset(-offset/2, -offset/4), Offset(-offset/2, offset/4))
                            Direction.RIGHT -> Pair(Offset(offset/2, -offset/4), Offset(offset/2, offset/4))
                        }
                        drawCircle(color = Color.Black, radius = eyeRadius, center = center + eye1Offset)
                        drawCircle(color = Color.Black, radius = eyeRadius, center = center + eye2Offset)
                    }
                }

                // Yem çizimi
                val foodCenter = Offset(
                    with(density) { game.food.x * cellSize.toPx() } + cellSizePx / 2,
                    with(density) { game.food.y * cellSize.toPx() } + cellSizePx / 2 - foodOffsetY.value
                )
                val foodGradient = Brush.radialGradient(
                    colors = listOf(Color.Red, Color(0xFF800000)),
                    center = foodCenter,
                    radius = cellSizePx / 2
                )
                drawCircle(
                    brush = foodGradient,
                    radius = cellSizePx / 2,
                    center = foodCenter
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Score: $score", color = Color.White)
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            var isTraining by remember { mutableStateOf(false) }
            Button(
                onClick = {
                    if (!isTraining) {
                        // Eğitim başlat
                        isTraining = true
                        coroutineScope.launch {
                            repeat(100) { // 100 episode
                                if (!isTraining) return@launch // Kullanıcı durdurduysa çık

                                game.reset()
                                score = 0
                                resetSnakePositions()

                                while (!game.isGameOver) {
                                    if (!isTraining) break // Durdurma kontrolü

                                    val state = game.getState().toString()
                                    val action = agent.getBestAction(state)
                                    val reward = game.performStep(action)
                                    agent.updateQ(state, action, reward, state)
                                    updateSnakePositions()
                                    score = game.score
                                    delay(150)
                                }

                                resetSnakePositions() // Yılan öldüğünde temizle
                            }
                            isTraining = false
                        }
                    } else {
                        // Eğitim durdur
                        isTraining = false
                    }
                },
                enabled = !isPlaying
            ) {
                Text(if (isTraining) "Stop Training" else "Train")
            }


            // Normal oynatma
            Button(
                onClick = {
                    if (!isPlaying) {
                        isPlaying = true
                        coroutineScope.launch {
                            game.reset()
                            score = 0
                            resetSnakePositions()
                            while (!game.isGameOver && isPlaying) {
                                val state = game.getState().toString()
                                val action = agent.getBestAction(state)
                                game.performStep(action)
                                updateSnakePositions()
                                score = game.score
                                delay(200)
                            }
                            resetSnakePositions()
                            isPlaying = false
                        }
                    } else {
                        isPlaying = false
                    }
                },
                enabled = !isTraining
            ) {
                Text(if (isPlaying) "Stop" else "Play")
            }


            // Reset
            Button(
                onClick = {
                    game.reset()
                    score = 0
                    updateSnakePositions()
                },
                enabled = !isPlaying && !isTraining
            ) { Text("Reset") }
        }
    }
}
