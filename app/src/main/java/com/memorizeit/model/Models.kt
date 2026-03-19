package com.memorizeit.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val userName: String = "",
    val likes: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val photoUris: List<String> = emptyList()
)

sealed class PuzzleContent {
    data class Text(val value: String) : PuzzleContent()
    data class Image(val uri: String) : PuzzleContent()
}

data class PuzzleSeed(
    val id: Int,
    val content: PuzzleContent
)

data class MemoryCard(
    val cardId: Int,
    val pairId: Int,
    val content: PuzzleContent,
    val isRevealed: Boolean = false,
    val isMatched: Boolean = false
)
