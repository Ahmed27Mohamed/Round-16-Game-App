package com.a2004256_ahmedmohamed.round16app

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a2004256_ahmedmohamed.round16app.ui.theme.RoundOf16Theme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoundOf16Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TournamentApp()
                }
            }
        }
    }
}

@Immutable
data class Player(
    val id: Int,
    val name: String
)

@Immutable
data class MatchUi(
    val player1: Player? = null,
    val player2: Player? = null,
    val winnerId: Int? = null
)

private enum class AppScreen {
    INPUT, BRACKET, CELEBRATION
}

@Composable
fun TournamentApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.INPUT) }

    var round16 by remember { mutableStateOf(emptyList<MatchUi>()) }
    var quarter by remember { mutableStateOf(emptyList<MatchUi>()) }
    var semi by remember { mutableStateOf(emptyList<MatchUi>()) }
    var finalRound by remember { mutableStateOf(emptyList<MatchUi>()) }

    fun resetTournament() {
        round16 = emptyList()
        quarter = emptyList()
        semi = emptyList()
        finalRound = emptyList()
        currentScreen = AppScreen.INPUT
    }

    fun startTournament(names: List<String>) {
        val players = names.shuffled().mapIndexed { index, name ->
            Player(id = index + 1, name = name)
        }

        round16 = players.chunked(2).map { pair ->
            MatchUi(
                player1 = pair.getOrNull(0),
                player2 = pair.getOrNull(1)
            )
        }

        quarter = buildNextRound(round16)
        semi = buildNextRound(quarter)
        finalRound = buildNextRound(semi)

        currentScreen = AppScreen.BRACKET
    }

    fun updateFromRound16(index: Int, winner: Player) {
        round16 = round16.replaceAt(index, round16[index].copy(winnerId = winner.id))
        quarter = buildNextRound(round16, quarter)
        semi = buildNextRound(quarter, semi)
        finalRound = buildNextRound(semi, finalRound)
    }

    fun updateFromQuarter(index: Int, winner: Player) {
        quarter = quarter.replaceAt(index, quarter[index].copy(winnerId = winner.id))
        semi = buildNextRound(quarter, semi)
        finalRound = buildNextRound(semi, finalRound)
    }

    fun updateFromSemi(index: Int, winner: Player) {
        semi = semi.replaceAt(index, semi[index].copy(winnerId = winner.id))
        finalRound = buildNextRound(semi, finalRound)
    }

    fun updateFromFinal(index: Int, winner: Player) {
        finalRound = finalRound.replaceAt(index, finalRound[index].copy(winnerId = winner.id))
        currentScreen = AppScreen.CELEBRATION
    }

    val champion = finalRound.firstOrNull()?.selectedWinner()

    when (currentScreen) {
        AppScreen.INPUT -> {
            InputScreen(
                onStartTournament = ::startTournament
            )
        }

        AppScreen.BRACKET -> {
            BracketScreen(
                round16 = round16,
                quarter = quarter,
                semi = semi,
                finalRound = finalRound,
                champion = champion,
                onSelectRound16Winner = ::updateFromRound16,
                onSelectQuarterWinner = ::updateFromQuarter,
                onSelectSemiWinner = ::updateFromSemi,
                onSelectFinalWinner = ::updateFromFinal,
                onRestart = ::resetTournament
            )
        }

        AppScreen.CELEBRATION -> {
            CelebrationScreen(
                champion = champion,
                onBackToBracket = { currentScreen = AppScreen.BRACKET },
                onRestart = ::resetTournament
            )
        }
    }
}

fun buildNextRound(
    previousRound: List<MatchUi>,
    oldNextRound: List<MatchUi> = emptyList()
): List<MatchUi> {
    val winners = previousRound.map { it.selectedWinner() }

    return winners.chunked(2).mapIndexed { index, pair ->
        val p1 = pair.getOrNull(0)
        val p2 = pair.getOrNull(1)

        val oldWinnerId = oldNextRound.getOrNull(index)?.winnerId
        val stillValidWinner = listOfNotNull(p1, p2).firstOrNull { it.id == oldWinnerId }

        MatchUi(
            player1 = p1,
            player2 = p2,
            winnerId = stillValidWinner?.id
        )
    }
}

fun MatchUi.selectedWinner(): Player? {
    return when (winnerId) {
        player1?.id -> player1
        player2?.id -> player2
        else -> null
    }
}

fun <T> List<T>.replaceAt(index: Int, newItem: T): List<T> {
    return toMutableList().apply { this[index] = newItem }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputScreen(
    onStartTournament: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val names = remember { mutableStateListOf(*Array(16) { "" }) }

    val infiniteTransition = rememberInfiniteTransition(label = "bgGradient")
    val shift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )

    val leftNames = names.take(8)
    val rightNames = names.drop(8)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "دور الـ16 🏆",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedGradientBackground(shift = shift)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "ابدأ بطولة إقصائية كاملة",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "اكتب 16 اسمًا، مقسمين في عمودين. بعد ذلك سيتم توزيعهم عشوائيًا على شجرة البطولة وتختار الفائز من كل مواجهة حتى تصل إلى البطل النهائي.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            leftNames.forEachIndexed { index, value ->
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = { names[index] = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    singleLine = true,
                                    label = { Text("الاسم ${index + 1}") },
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words
                                    )
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rightNames.forEachIndexed { index, value ->
                                val actualIndex = index + 8

                                OutlinedTextField(
                                    value = value,
                                    onValueChange = { names[actualIndex] = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    singleLine = true,
                                    label = { Text("الاسم ${actualIndex + 1}") },
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.Words
                                    )
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val finalNames = names.map { it.trim() }
                            if (finalNames.any { it.isBlank() }) {
                                Toast.makeText(
                                    context,
                                    "من فضلك املأ كل الخانات الـ16",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            onStartTournament(finalNames)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Text(
                            "ابدأ البطولة",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedGradientBackground(shift: Float) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primary = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    val secondary = MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
    val tertiary = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    backgroundColor,
                    surfaceColor,
                    backgroundColor
                ),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
        )

        drawCircle(
            color = primary,
            radius = size.minDimension * 0.35f,
            center = Offset(size.width * 0.2f + shift * 0.05f, size.height * 0.15f)
        )

        drawCircle(
            color = secondary,
            radius = size.minDimension * 0.28f,
            center = Offset(size.width * 0.8f - shift * 0.04f, size.height * 0.25f)
        )

        drawCircle(
            color = tertiary,
            radius = size.minDimension * 0.30f,
            center = Offset(size.width * 0.5f, size.height * 0.8f - shift * 0.03f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BracketScreen(
    round16: List<MatchUi>,
    quarter: List<MatchUi>,
    semi: List<MatchUi>,
    finalRound: List<MatchUi>,
    champion: Player?,
    onSelectRound16Winner: (Int, Player) -> Unit,
    onSelectQuarterWinner: (Int, Player) -> Unit,
    onSelectSemiWinner: (Int, Player) -> Unit,
    onSelectFinalWinner: (Int, Player) -> Unit,
    onRestart: () -> Unit
) {
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "bracketGradient")
    val shift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shiftBracket"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "شجرة البطولة",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    OutlinedButton(
                        onClick = onRestart,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("بطولة جديدة")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedGradientBackground(shift = shift)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                ChampionHeaderCompact(champion = champion)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    RoundColumnCompact(
                        title = "دور 16",
                        matches = round16.take(4),
                        topSpacer = 4.dp,
                        betweenMatches = 8.dp,
                        columnWidth = 150.dp,
                        onPlayerSelected = { localIndex, winner ->
                            onSelectRound16Winner(localIndex, winner)
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    BracketConnectorColumnCompact(height = 420.dp)
                    Spacer(modifier = Modifier.width(8.dp))

                    RoundColumnCompact(
                        title = "ربع النهائي",
                        matches = quarter.take(2),
                        topSpacer = 34.dp,
                        betweenMatches = 64.dp,
                        columnWidth = 150.dp,
                        onPlayerSelected = { localIndex, winner ->
                            onSelectQuarterWinner(localIndex, winner)
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    BracketConnectorColumnCompact(height = 340.dp)
                    Spacer(modifier = Modifier.width(8.dp))

                    RoundColumnCompact(
                        title = "نصف النهائي",
                        matches = semi.take(1),
                        topSpacer = 92.dp,
                        betweenMatches = 0.dp,
                        columnWidth = 150.dp,
                        onPlayerSelected = { localIndex, winner ->
                            onSelectSemiWinner(localIndex, winner)
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    BracketConnectorColumnCompact(height = 240.dp)
                    Spacer(modifier = Modifier.width(8.dp))

                    FinalCenterColumnCompact(
                        finalMatch = finalRound.firstOrNull(),
                        champion = champion,
                        onPlayerSelected = { winner ->
                            onSelectFinalWinner(0, winner)
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    BracketConnectorColumnCompact(height = 240.dp)
                    Spacer(modifier = Modifier.width(8.dp))

                    RoundColumnCompact(
                        title = "نصف النهائي",
                        matches = semi.drop(1).take(1),
                        topSpacer = 92.dp,
                        betweenMatches = 0.dp,
                        columnWidth = 150.dp,
                        onPlayerSelected = { localIndex, winner ->
                            onSelectSemiWinner(localIndex + 1, winner)
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    BracketConnectorColumnCompact(height = 340.dp)
                    Spacer(modifier = Modifier.width(8.dp))

                    RoundColumnCompact(
                        title = "ربع النهائي",
                        matches = quarter.drop(2).take(2),
                        topSpacer = 34.dp,
                        betweenMatches = 64.dp,
                        columnWidth = 150.dp,
                        onPlayerSelected = { localIndex, winner ->
                            onSelectQuarterWinner(localIndex + 2, winner)
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    BracketConnectorColumnCompact(height = 420.dp)
                    Spacer(modifier = Modifier.width(8.dp))

                    RoundColumnCompact(
                        title = "دور 16",
                        matches = round16.drop(4).take(4),
                        topSpacer = 4.dp,
                        betweenMatches = 8.dp,
                        columnWidth = 150.dp,
                        onPlayerSelected = { localIndex, winner ->
                            onSelectRound16Winner(localIndex + 4, winner)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChampionHeaderCompact(champion: Player?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.93f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "البطل:",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = champion?.name ?: "بانتظار الحسم",
                style = MaterialTheme.typography.titleMedium,
                color = if (champion != null) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun RoundColumnCompact(
    title: String,
    matches: List<MatchUi>,
    topSpacer: Dp,
    betweenMatches: Dp,
    columnWidth: Dp,
    onPlayerSelected: (Int, Player) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(columnWidth)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Spacer(modifier = Modifier.height(topSpacer))

        matches.forEachIndexed { index, match ->
            BracketMatchCardCompact(
                match = match,
                onPlayerSelected = { onPlayerSelected(index, it) }
            )
            if (index != matches.lastIndex) {
                Spacer(modifier = Modifier.height(betweenMatches))
            }
        }
    }
}

@Composable
fun FinalCenterColumnCompact(
    finalMatch: MatchUi?,
    champion: Player?,
    onPlayerSelected: (Player) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(165.dp)
    ) {
        Text(
            text = "النهائي",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Spacer(modifier = Modifier.height(92.dp))

        BracketMatchCardCompact(
            match = finalMatch ?: MatchUi(),
            onPlayerSelected = onPlayerSelected,
            highlightStrongly = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.10f)
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏆 البطل",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = champion?.name ?: "بانتظار الحسم",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = if (champion != null) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BracketMatchCardCompact(
    match: MatchUi,
    onPlayerSelected: (Player) -> Unit,
    highlightStrongly: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PlayerItemCompact(
                player = match.player1,
                isWinner = match.winnerId == match.player1?.id,
                onClick = { match.player1?.let(onPlayerSelected) },
                highlightStrongly = highlightStrongly
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            )

            PlayerItemCompact(
                player = match.player2,
                isWinner = match.winnerId == match.player2?.id,
                onClick = { match.player2?.let(onPlayerSelected) },
                highlightStrongly = highlightStrongly
            )
        }
    }
}

@Composable
fun PlayerItemCompact(
    player: Player?,
    isWinner: Boolean,
    onClick: () -> Unit,
    highlightStrongly: Boolean
) {
    val enabled = player != null

    val targetScale = if (isWinner) 1.02f else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label = "winnerScaleCompact"
    )

    val pulseTransition = rememberInfiniteTransition(label = "winnerPulseCompact")
    val glowAlpha by pulseTransition.animateFloat(
        initialValue = if (isWinner) 0.28f else 0f,
        targetValue = if (isWinner) 0.55f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlphaCompact"
    )

    val bgColor = when {
        isWinner && highlightStrongly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.24f)
        isWinner -> MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        else -> Color.Transparent
    }

    val borderColor = when {
        isWinner -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        if (isWinner) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        MaterialTheme.colorScheme.secondary.copy(alpha = glowAlpha * 0.10f)
                    )
            )
        }

        Text(
            text = player?.name ?: "بانتظار",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = when {
                isWinner -> MaterialTheme.colorScheme.secondary
                enabled -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            },
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
    }
}

@Composable
fun BracketConnectorColumnCompact(height: Dp) {
    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    val circleColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)

    Box(
        modifier = Modifier
            .width(18.dp)
            .height(height)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 2.dp.toPx()
            val w = size.width
            val h = size.height

            drawLine(
                color = lineColor,
                start = Offset(w * 0.2f, 0f),
                end = Offset(w * 0.8f, h),
                strokeWidth = stroke
            )

            drawLine(
                color = lineColor,
                start = Offset(w * 0.8f, 0f),
                end = Offset(w * 0.2f, h),
                strokeWidth = stroke
            )

            drawCircle(
                color = circleColor,
                radius = 3.dp.toPx(),
                center = Offset(w / 2f, h / 2f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}

@Composable
fun BracketConnectorColumn(height: Dp) {
    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    val circleColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .width(36.dp)
            .height(height)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 4.dp.toPx()
            val w = size.width
            val h = size.height

            drawLine(
                color = lineColor,
                start = Offset(w * 0.2f, 0f),
                end = Offset(w * 0.8f, h),
                strokeWidth = stroke
            )

            drawLine(
                color = lineColor,
                start = Offset(w * 0.8f, 0f),
                end = Offset(w * 0.2f, h),
                strokeWidth = stroke
            )

            drawCircle(
                color = circleColor,
                radius = 5.dp.toPx(),
                center = Offset(w / 2f, h / 2f),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
fun ChampionHeader(champion: Player?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "البطل الحالي",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedContent(targetState = champion, label = "championText") { currentChampion ->
                if (currentChampion == null) {
                    Text(
                        text = "لم يتم تحديد البطل بعد",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = currentChampion.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoundColumn(
    title: String,
    matches: List<MatchUi>,
    topSpacer: Dp,
    betweenMatches: Dp,
    onPlayerSelected: (Int, Player) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(220.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Spacer(modifier = Modifier.height(topSpacer))

        matches.forEachIndexed { index, match ->
            BracketMatchCard(
                match = match,
                onPlayerSelected = { onPlayerSelected(index, it) }
            )
            if (index != matches.lastIndex) {
                Spacer(modifier = Modifier.height(betweenMatches))
            }
        }
    }
}

@Composable
fun FinalCenterColumn(
    finalMatch: MatchUi?,
    champion: Player?,
    onPlayerSelected: (Player) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(250.dp)
    ) {
        Text(
            text = "النهائي",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Spacer(modifier = Modifier.height(150.dp))

        BracketMatchCard(
            match = finalMatch ?: MatchUi(),
            onPlayerSelected = onPlayerSelected,
            highlightStrongly = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        val pulseTransition = rememberInfiniteTransition(label = "championPulse")
        val pulse by pulseTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(if (champion != null) pulse else 1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
            ),
            border = BorderStroke(
                1.2.dp,
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏆 البطل",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = champion?.name ?: "بانتظار الحسم",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (champion != null) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BracketMatchCard(
    match: MatchUi,
    onPlayerSelected: (Player) -> Unit,
    highlightStrongly: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PlayerItem(
                player = match.player1,
                isWinner = match.winnerId == match.player1?.id,
                onClick = { match.player1?.let(onPlayerSelected) },
                highlightStrongly = highlightStrongly
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            )

            PlayerItem(
                player = match.player2,
                isWinner = match.winnerId == match.player2?.id,
                onClick = { match.player2?.let(onPlayerSelected) },
                highlightStrongly = highlightStrongly
            )
        }
    }
}

@Composable
fun PlayerItem(
    player: Player?,
    isWinner: Boolean,
    onClick: () -> Unit,
    highlightStrongly: Boolean
) {
    val enabled = player != null

    val targetScale = if (isWinner) 1.03f else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 400f),
        label = "winnerScale"
    )

    val pulseTransition = rememberInfiniteTransition(label = "winnerPulse")
    val glowAlpha by pulseTransition.animateFloat(
        initialValue = if (isWinner) 0.35f else 0f,
        targetValue = if (isWinner) 0.75f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val bgColor = when {
        isWinner && highlightStrongly -> MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)
        isWinner -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        else -> Color.Transparent
    }

    val borderColor = when {
        isWinner -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.3.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(14.dp)
    ) {
        if (isWinner) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        MaterialTheme.colorScheme.secondary.copy(alpha = glowAlpha * 0.12f)
                    )
            )
        }

        Text(
            text = player?.name ?: "بانتظار المتأهل",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = when {
                isWinner -> MaterialTheme.colorScheme.secondary
                enabled -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            },
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CelebrationScreen(
    champion: Player?,
    onBackToBracket: () -> Unit,
    onRestart: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val rotate by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotate"
    )
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(120)
        visible = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("الاحتفال بالبطل") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedGradientBackground(shift = 650f)

            CelebrationParticles()

            val context = LocalContext.current
            val winSound = MediaPlayer.create(context, R.raw.win)
            winSound.start()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(700)) + scaleIn(
                        initialScale = 0.7f,
                        animationSpec = tween(700)
                    ),
                    exit = fadeOut() + scaleOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🏆",
                            modifier = Modifier
                                .scale(scale)
                                .rotate(rotate),
                            style = MaterialTheme.typography.displayLarge
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "الفائز بالبطولة",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                            ),
                            border = BorderStroke(
                                1.2.dp,
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                        ) {
                            Text(
                                text = champion?.name ?: "لا يوجد بطل",
                                modifier = Modifier.padding(
                                    horizontal = 26.dp,
                                    vertical = 20.dp
                                ),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.ExtraBold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = onBackToBracket,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("الرجوع إلى الشجرة")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onRestart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("بدء بطولة جديدة")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CelebrationParticles() {
    val goldColor = MaterialTheme.colorScheme.tertiary
    val cyanColor = MaterialTheme.colorScheme.secondary
    val purpleColor = MaterialTheme.colorScheme.primary

    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha1"
    )

    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha2"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val points = listOf(
            Offset(size.width * 0.15f, size.height * 0.18f),
            Offset(size.width * 0.78f, size.height * 0.22f),
            Offset(size.width * 0.25f, size.height * 0.72f),
            Offset(size.width * 0.82f, size.height * 0.66f),
            Offset(size.width * 0.52f, size.height * 0.12f),
            Offset(size.width * 0.56f, size.height * 0.85f)
        )

        points.forEachIndexed { index, point ->
            drawCircle(
                color = when (index % 3) {
                    0 -> goldColor.copy(alpha = alpha1)
                    1 -> cyanColor.copy(alpha = alpha2)
                    else -> purpleColor.copy(alpha = alpha1)
                },
                radius = if (index % 2 == 0) 12.dp.toPx() else 8.dp.toPx(),
                center = point
            )
        }
    }
}