package com.lifequest.life

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF25252B)
private val Muted = Color(0xFF777780)
private val Pink = Color(0xFFF27FA5)
private val Yellow = Color(0xFFF4D35E)
private val Orange = Color(0xFFF39A55)
private val Blue = Color(0xFF69BCEB)
private val Cream = Color(0xFFFFF9F7)
private val Green = Color(0xFF69B88A)

data class Mission(
    val id: Int,
    val title: String,
    val detail: String,
    val xp: Int,
    val coins: Int,
    val icon: String,
    val done: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LifeApp() }
    }
}

@Composable
fun LifeApp() {
    var tab by remember { mutableStateOf(0) }
    var xp by remember { mutableIntStateOf(120) }
    var coins by remember { mutableIntStateOf(45) }
    var petStage by remember { mutableIntStateOf(0) }
    var missions by remember {
        mutableStateOf(
            listOf(
                Mission(1, "Ranger ma chambre", "90 min · photo avant / après", 100, 20, "✨"),
                Mission(2, "Faire les courses", "Acheter les ingrédients", 30, 8, "🛍️"),
                Mission(3, "Travailler les cours", "45 min · minuteur", 70, 14, "📚"),
                Mission(4, "Atelier pâtisserie", "Gaufres et roses des sables", 50, 10, "🧁")
            )
        )
    }
    val completed = missions.count { it.done }
    val progress = if (missions.isEmpty()) 0f else completed.toFloat() / missions.size

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Pink,
            secondary = Blue,
            background = Cream,
            surface = Color.White,
            onSurface = Ink
        )
    ) {
        Scaffold(
            containerColor = Cream,
            bottomBar = {
                NavigationBar(containerColor = Color.White) {
                    val labels = listOf("Aujourd'hui", "Collection", "Récompenses", "Profil")
                    val icons = listOf(Icons.Default.CheckCircle, Icons.Default.Pets, Icons.Default.CardGiftcard, Icons.Default.BarChart)
                    labels.forEachIndexed { index, label ->
                        NavigationBarItem(
                            selected = tab == index,
                            onClick = { tab = index },
                            icon = { Icon(icons[index], contentDescription = label) },
                            label = { Text(label, fontSize = 10.sp) }
                        )
                    }
                }
            }
        ) { padding ->
            when (tab) {
                0 -> HomeScreen(
                    padding = padding, xp = xp, coins = coins, progress = progress,
                    missions = missions,
                    onComplete = { id ->
                        val target = missions.firstOrNull { it.id == id }
                        if (target != null && !target.done) {
                            missions = missions.map { if (it.id == id) it.copy(done = true) else it }
                            xp += target.xp
                            coins += target.coins
                            if (xp >= (petStage + 1) * 300) petStage = (petStage + 1).coerceAtMost(3)
                        }
                    }
                )
                1 -> CollectionScreen(padding, petStage)
                2 -> RewardsScreen(padding, coins, onSpend = { cost -> if (coins >= cost) coins -= cost })
                else -> ProfileScreen(padding, xp, coins, completed, missions.size)
            }
        }
    }
}

@Composable
fun HomeScreen(
    padding: PaddingValues, xp: Int, coins: Int, progress: Float,
    missions: List<Mission>, onComplete: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text("LIFE", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Ink, letterSpacing = 2.sp)
                    Text("Ton quotidien, ton aventure.", color = Muted, fontSize = 13.sp)
                }
                Surface(shape = CircleShape, color = Color.White) {
                    Row(Modifier.padding(horizontal = 12.dp, vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🪙 ")
                        Text("$coins", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Ton compagnon", color = Muted, fontSize = 12.sp)
                            Text("Petit renard · Niveau 1", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
                            Text("Il grandit grâce à tes missions", color = Muted, fontSize = 12.sp)
                        }
                        Text("🦊", fontSize = 48.sp)
                    }
                    LinearProgressIndicator(progress = { (xp % 300) / 300f }, modifier = Modifier.fillMaxWidth().height(8.dp), color = Pink, trackColor = Pink.copy(alpha = .15f))
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("$xp XP", fontWeight = FontWeight.Bold, color = Pink)
                        Text("Prochain niveau : 300 XP", color = Muted, fontSize = 11.sp)
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Tes missions", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text("${missions.count { it.done }}/${missions.size}", color = Muted, fontWeight = FontWeight.SemiBold)
                }
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(7.dp), color = Blue, trackColor = Blue.copy(alpha = .15f))
            }
        }
        items(missions, key = { it.id }) { mission ->
            MissionCard(mission, onComplete = { onComplete(mission.id) })
        }
        item {
            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFFFF0D9), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🌞", fontSize = 25.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Petit rappel", fontWeight = FontWeight.Bold, color = Ink)
                        Text("Chaque petite action compte. Avance à ton rythme.", color = Muted, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MissionCard(mission: Mission, onComplete: () -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = when (mission.id % 4) { 0 -> Yellow.copy(alpha=.25f); 1 -> Pink.copy(alpha=.18f); 2 -> Blue.copy(alpha=.18f); else -> Orange.copy(alpha=.18f) }) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { Text(mission.icon, fontSize = 23.sp) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(mission.title, fontWeight = FontWeight.Bold, color = Ink)
                Text(mission.detail, color = Muted, fontSize = 11.sp)
                Text("+${mission.xp} XP  ·  +${mission.coins} pièces", color = Orange, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
            if (mission.done) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Terminée", tint = Green)
            } else {
                FilledTonalButton(onClick = onComplete, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp), colors = ButtonDefaults.filledTonalButtonColors(containerColor = Pink.copy(alpha=.16f), contentColor = Ink)) {
                    Text("Valider", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun CollectionScreen(padding: PaddingValues, stage: Int) {
    Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Ta collection", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text("Fais grandir tes compagnons en accomplissant tes missions.", color = Muted)
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🦊", fontSize = 82.sp)
                Text("Renard des nuages", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Évolution ${stage + 1}/4", color = Pink, fontWeight = FontWeight.SemiBold)
                Text("Continue tes missions pour le faire évoluer.", color = Muted, fontSize = 12.sp)
            }
        }
        Text("À découvrir", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PetTile("🐈", "Chat lunaire")
            PetTile("🐉", "Petit dragon")
            PetTile("🦌", "Cerf étoilé")
        }
    }
}

@Composable
fun PetTile(emoji: String, name: String) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color.White, modifier = Modifier.width(100.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 32.sp)
            Text(name, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
            Text("Verrouillé", fontSize = 9.sp, color = Muted)
        }
    }
}

@Composable
fun RewardsScreen(padding: PaddingValues, coins: Int, onSpend: (Int) -> Unit) {
    Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Tes récompenses", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text("Tu as $coins pièces à dépenser.", color = Muted)
        RewardCard("Pause détente", "15 minutes de pause téléphone", "☕", 20) { onSpend(20) }
        RewardCard("Pause bonus", "30 minutes de pause téléphone", "🎧", 35) { onSpend(35) }
        RewardCard("Accessoire animal", "Un accessoire pour ton compagnon", "🎀", 25) { onSpend(25) }
        Text("Les pauses téléphone sont des récompenses à connecter au futur mode de concentration.", fontSize = 11.sp, color = Muted)
    }
}

@Composable
fun RewardCard(title: String, detail: String, emoji: String, cost: Int, onBuy: () -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 30.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(detail, color = Muted, fontSize = 11.sp)
                Text("$cost pièces", color = Orange, fontSize = 11.sp)
            }
            TextButton(onClick = onBuy) { Text("Obtenir") }
        }
    }
}

@Composable
fun ProfileScreen(padding: PaddingValues, xp: Int, coins: Int, completed: Int, total: Int) {
    Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Ton espace", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Ink)
        Surface(shape = RoundedCornerShape(22.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatLine("Expérience gagnée", "$xp XP")
                StatLine("Pièces disponibles", "$coins")
                StatLine("Missions terminées", "$completed / $total")
                StatLine("Temps d'écran", "À connecter au mode concentration")
            }
        }
        Surface(shape = RoundedCornerShape(20.dp), color = Blue.copy(alpha=.14f), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Blue)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Mode concentration", fontWeight = FontWeight.Bold)
                    Text("Le blocage Android sera ajouté dans une prochaine étape.", color = Muted, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun StatLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted)
        Text(value, fontWeight = FontWeight.Bold, color = Ink)
    }
}
