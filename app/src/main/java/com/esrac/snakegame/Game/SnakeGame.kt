package com.esrac.snakegame.Game

import android.os.Build
import androidx.annotation.RequiresApi

data class Point(var x: Int, var y: Int)

enum class Direction { UP, DOWN, LEFT, RIGHT }

class SnakeGame(private val width: Int = 10, private val height: Int = 10) {

    private var _snake = mutableListOf<Point>()
    val snake: List<Point> get() = _snake

    private var _food = Point(0, 0)
    val food: Point get() = _food

    var lastDirection: Direction = Direction.RIGHT
        private set

    var score: Int = 0
        private set
    var isGameOver: Boolean = false
        private set

    init { reset() }

    fun reset() {
        _snake = mutableListOf(Point(width / 2, height / 2))
        _food = Point((0 until width).random(), (0 until height).random())
        score = 0
        isGameOver = false
    }

    fun getState(): com.esrac.snakegame.Rl.State {
        val head = _snake.first()
        val foodDirX = when {
            _food.x > head.x -> 1
            _food.x < head.x -> -1
            else -> 0
        }
        val foodDirY = when {
            _food.y > head.y -> 1
            _food.y < head.y -> -1
            else -> 0
        }
        return com.esrac.snakegame.Rl.State(head.x, head.y, foodDirX, foodDirY)
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun performStep(dir: Direction): Double {
        if (isGameOver) return 0.0
        lastDirection = dir
        val head = _snake.first()
        val newHead = when (dir) {
            Direction.UP -> Point(head.x, head.y - 1)
            Direction.DOWN -> Point(head.x, head.y + 1)
            Direction.LEFT -> Point(head.x - 1, head.y)
            Direction.RIGHT -> Point(head.x + 1, head.y)
        }

        // Duvara çarpma
        if (newHead.x !in 0 until width || newHead.y !in 0 until height || _snake.contains(newHead)) {
            isGameOver = true
            return -10.0
        }

        _snake.add(0, newHead)

        // Yem yeme
        return if (newHead.x == _food.x && newHead.y == _food.y) {
            score++
            _food = Point((0 until width).random(), (0 until height).random())
            10.0
        } else {
            _snake.removeLast()
            -0.1 // adım başına küçük ceza
        }
    }
}