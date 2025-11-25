package com.esrac.snakegame.Rl

import android.os.Build
import androidx.annotation.RequiresApi
import com.esrac.snakegame.Game.Direction
import com.esrac.snakegame.Game.SnakeGame
import kotlin.random.Random

class QLearningAgent(
    private val actions: List<Direction>,
    private val alpha: Double = 0.1,
    private val gamma: Double = 0.9,
    private val epsilon: Double = 0.1
) {
    private val qTable = mutableMapOf<String, MutableMap<Direction, Double>>()

    private fun chooseAction(state: String): Direction {
        val stateActions = qTable.getOrPut(state) { mutableMapOf() }
        return if (Random.nextDouble() < epsilon) actions.random()
        else stateActions.maxByOrNull { it.value }?.key ?: actions.random()
    }

    fun updateQ(state: String, action: Direction, reward: Double, nextState: String) {
        val nextActions = qTable.getOrPut(nextState) { mutableMapOf() }
        val maxNextQ = nextActions.values.maxOrNull() ?: 0.0
        val qValues = qTable.getOrPut(state) { mutableMapOf() }
        val oldQ = qValues.getOrDefault(action, 0.0)
        val newQ = oldQ + alpha * (reward + gamma * maxNextQ - oldQ)
        qValues[action] = newQ
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun train(episodes: Int = 1000, maxSteps: Int = 200): List<Int> {
        val game = SnakeGame()
        val scores = mutableListOf<Int>()

        repeat(episodes) {
            game.reset()
            var totalReward = 0.0
            var steps = 0

            while (!game.isGameOver && steps < maxSteps) {
                val state = game.getState().toString()
                val action = chooseAction(state)
                val reward = game.performStep(action)
                val nextState = game.getState().toString()
                updateQ(state, action, reward, nextState)
                totalReward += reward
                steps++
            }

            scores.add(game.score)
            println("Episode ${it + 1}: Score = ${game.score}, TotalReward = ${"%.2f".format(totalReward)}")
        }
        return scores
    }

    fun getBestAction(state: String): Direction {
        val stateActions = qTable.getOrPut(state) { mutableMapOf() }
        return stateActions.maxByOrNull { it.value }?.key ?: actions.random()
    }
}