package com.memorizeit

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.memorizeit.data.ProfileStore
import com.memorizeit.game.GameFactory
import com.memorizeit.model.MemoryCard
import com.memorizeit.model.PuzzleContent
import com.memorizeit.model.UserProfile
import kotlinx.coroutines.delay

private enum class AppScreen {
    Profile,
    Puzzle
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val profileStore = ProfileStore(this)
        setContent {
            MaterialTheme {
                MemorizeItApp(profileStore)
            }
        }
    }
}

@Composable
private fun MemorizeItApp(profileStore: ProfileStore) {
    var screen by remember { mutableStateOf(AppScreen.Profile) }
    var profile by remember { mutableStateOf(profileStore.load()) }

    var cards by remember(profile) { mutableStateOf(GameFactory.createGame(profile)) }
    var firstSelectedCardId by remember { mutableStateOf<Int?>(null) }
    var secondSelectedCardId by remember { mutableStateOf<Int?>(null) }
    var moves by remember { mutableIntStateOf(0) }

    fun resetRound() {
        cards = GameFactory.createGame(profile)
        firstSelectedCardId = null
        secondSelectedCardId = null
        moves = 0
    }

    fun onCardTapped(cardId: Int) {
        val tapped = cards.firstOrNull { it.cardId == cardId } ?: return
        if (tapped.isMatched || tapped.isRevealed || secondSelectedCardId != null) return

        cards = cards.map { card ->
            if (card.cardId == cardId) card.copy(isRevealed = true) else card
        }

        if (firstSelectedCardId == null) {
            firstSelectedCardId = cardId
        } else {
            secondSelectedCardId = cardId
            moves += 1
        }
    }

    LaunchedEffect(firstSelectedCardId, secondSelectedCardId, cards) {
        val firstId = firstSelectedCardId
        val secondId = secondSelectedCardId
        if (firstId == null || secondId == null) return@LaunchedEffect

        val first = cards.firstOrNull { it.cardId == firstId }
        val second = cards.firstOrNull { it.cardId == secondId }
        if (first == null || second == null) {
            firstSelectedCardId = null
            secondSelectedCardId = null
            return@LaunchedEffect
        }

        if (first.pairId == second.pairId) {
            cards = cards.map { card ->
                if (card.cardId == first.cardId || card.cardId == second.cardId) {
                    card.copy(isMatched = true)
                } else {
                    card
                }
            }
        } else {
            delay(900)
            cards = cards.map { card ->
                if (card.cardId == first.cardId || card.cardId == second.cardId) {
                    card.copy(isRevealed = false)
                } else {
                    card
                }
            }
        }

        firstSelectedCardId = null
        secondSelectedCardId = null
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF7F7EF)
    ) { innerPadding ->
        when (screen) {
            AppScreen.Profile -> {
                ProfileSetupScreen(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(16.dp),
                    initialProfile = profile,
                    onSave = { updatedProfile ->
                        profile = updatedProfile
                        profileStore.save(updatedProfile)
                        resetRound()
                        screen = AppScreen.Puzzle
                    }
                )
            }

            AppScreen.Puzzle -> {
                PuzzleScreen(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(16.dp),
                    profile = profile,
                    cards = cards,
                    moves = moves,
                    onCardTapped = ::onCardTapped,
                    onStartNewRound = ::resetRound,
                    onBackToProfile = { screen = AppScreen.Profile }
                )
            }
        }
    }
}

@Composable
private fun ProfileSetupScreen(
    modifier: Modifier,
    initialProfile: UserProfile,
    onSave: (UserProfile) -> Unit
) {
    var name by remember(initialProfile) { mutableStateOf(initialProfile.userName) }
    var likesText by remember(initialProfile) { mutableStateOf(initialProfile.likes.joinToString(", ")) }
    var interestsText by remember(initialProfile) { mutableStateOf(initialProfile.interests.joinToString(", ")) }
    var photos by remember(initialProfile) { mutableStateOf(initialProfile.photoUris) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                photos = photos + uri.toString()
            }
        }
    )

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Memorize It",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Add familiar photos, likes, and interests so every puzzle uses meaningful personal memories.",
            style = MaterialTheme.typography.bodyMedium
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = likesText,
            onValueChange = { likesText = it },
            label = { Text("Likes (comma separated)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = interestsText,
            onValueChange = { interestsText = it },
            label = { Text("Interests (comma separated)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = { imagePicker.launch("image/*") }) {
                Text("Upload Photo")
            }
            Text(
                text = "Photos: ${photos.size}",
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }

        if (photos.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(photos) { uri ->
                    AsyncImage(
                        model = uri,
                        contentDescription = "Selected photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(86.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                val likes = likesText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                val interests = interestsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                onSave(
                    UserProfile(
                        userName = name.trim(),
                        likes = likes,
                        interests = interests,
                        photoUris = photos
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Profile and Start Puzzle")
        }
    }
}

@Composable
private fun PuzzleScreen(
    modifier: Modifier,
    profile: UserProfile,
    cards: List<MemoryCard>,
    moves: Int,
    onCardTapped: (Int) -> Unit,
    onStartNewRound: () -> Unit,
    onBackToProfile: () -> Unit
) {
    val matchedPairs = cards.count { it.isMatched } / 2
    val totalPairs = cards.size / 2
    val gameCompleted = cards.isNotEmpty() && cards.all { it.isMatched }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "${profile.userName.ifBlank { "Player" }}'s Memory Puzzle",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Match the cards using familiar photos, likes, and interests.",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Moves: $moves   Matched: $matchedPairs/$totalPairs",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )

        if (cards.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4CC))) {
                Text(
                    text = "Add at least one photo, like, or interest in the profile before starting a puzzle.",
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(cards.size) { index ->
                    val card = cards[index]
                    MemoryCardView(card = card, onClick = { onCardTapped(card.cardId) })
                }
            }
        }

        if (gameCompleted) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFCDEFD4))) {
                Text(
                    text = "Great job. All matches found.",
                    modifier = Modifier.padding(12.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onStartNewRound,
                modifier = Modifier.weight(1f)
            ) {
                Text("New Round")
            }
            Button(
                onClick = onBackToProfile,
                modifier = Modifier.weight(1f)
            ) {
                Text("Edit Profile")
            }
        }
    }
}

@Composable
private fun MemoryCardView(
    card: MemoryCard,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth()
            .clickable(enabled = !card.isMatched) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (card.isMatched) Color(0xFFD2EED9) else Color.White
        )
    ) {
        if (card.isRevealed || card.isMatched) {
            when (val content = card.content) {
                is PuzzleContent.Image -> {
                    AsyncImage(
                        model = content.uri,
                        contentDescription = "Memory photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is PuzzleContent.Text -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = content.value,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFDCE8FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
