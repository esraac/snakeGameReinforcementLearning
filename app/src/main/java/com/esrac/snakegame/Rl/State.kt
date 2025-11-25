package com.esrac.snakegame.Rl

data class State(
    val headX: Int,
    val headY: Int,
    val foodDirX: Int,
    val foodDirY: Int
) {
    override fun toString(): String {
        return "${headX}_${headY}_${foodDirX}_${foodDirY}"
    }
}