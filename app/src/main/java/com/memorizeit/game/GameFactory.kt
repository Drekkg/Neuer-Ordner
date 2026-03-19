package com.memorizeit.game

import com.memorizeit.model.MemoryCard
import com.memorizeit.model.PuzzleContent
import com.memorizeit.model.PuzzleSeed
import com.memorizeit.model.UserProfile
import kotlin.random.Random

object GameFactory {
    fun createGame(profile: UserProfile, seedCount: Int = 6): List<MemoryCard> {
        val pool = buildSeedPool(profile)
        if (pool.isEmpty()) return emptyList()

        val selected = pool.shuffled().take(seedCount.coerceAtMost(pool.size))
        val cards = mutableListOf<MemoryCard>()
        var cardId = 0

        selected.forEach { seed ->
            cards += MemoryCard(cardId = cardId++, pairId = seed.id, content = seed.content)
            cards += MemoryCard(cardId = cardId++, pairId = seed.id, content = seed.content)
        }

        return cards.shuffled(Random(System.currentTimeMillis()))
    }

    private fun buildSeedPool(profile: UserProfile): List<PuzzleSeed> {
        var id = 0
        val seeds = mutableListOf<PuzzleSeed>()

        profile.photoUris.forEach { uri ->
            seeds += PuzzleSeed(id = id++, content = PuzzleContent.Image(uri))
        }
        profile.likes.forEach { like ->
            if (like.isNotBlank()) {
                seeds += PuzzleSeed(id = id++, content = PuzzleContent.Text("Likes: $like"))
            }
        }
        profile.interests.forEach { interest ->
            if (interest.isNotBlank()) {
                seeds += PuzzleSeed(id = id++, content = PuzzleContent.Text("Interest: $interest"))
            }
        }

        return seeds
    }
}
