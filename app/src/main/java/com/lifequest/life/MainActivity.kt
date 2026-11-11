
package com.lifequest.life

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Ink = Color(0xFF25252B)
private val Muted = Color(0xFF777780)
private val Pink = Color(0xFFF27FA5)
private val Yellow = Color(0xFFF4D35E)
private val Orange = Color(0xFFF39A55)
private val Blue = Color(0xFF69BCEB)
private val Cream = Color(0xFFFFF9F7)
private val Green = Color(0xFF69B88A)

private const val XP_PER_EVOLUTION = 300

data class Mission(
    val id: Int,
    val title: String,
    val detail: String,
    val xp: Int,
    val coins: Int,
    val icon: String,
    val done: Boolean = false
)

data class Pet(
    val id: String,
    val name: String,
    val evolutions: List<String>,
    val description: String
)

private val petCatalog = listOf(
    Pet("fox", "Renard magique", listOf("🦊", "🦊✨", "🦊🌟", "🦊👑"), "Un compagnon malicieux."),
    Pet("cat", "Chat lunaire", listOf("🐈", "🐈‍⬛", "🐈🌙", "🐈‍⬛✨"), "Un petit chat mystérieux."),
    Pet("dragon", "Petit dragon", listOf("🐉", "🐲", "🐉🔥", "🐲👑"), "Un dragon courageux."),
    Pet("deer", "Cerf étoilé", listOf("🦌", "🦌✨", "🦌🌟", "🦌👑"), "Un cerf paisible."),
    Pet("axolotl", "Axolotl", listOf("🩷", "🩷✨", "🩷🌸", "🩷👑"), "Un compagnon aquatique."),
    Pet("penguin", "Manchot", listOf("🐧", "🐧❄️", "🐧✨", "🐧👑"), "Un explorateur des terres glacées."),
    Pet("capybara", "Capybara", listOf("🦫", "🦫🌿", "🦫🌸", "🦫👑"), "Un compagnon calme."),
    Pet("rabbit", "Lapin", listOf("🐰", "🐰🌷", "🐰✨", "🐰👑"), "Un petit lapin plein d'énergie."),
    Pet("butterfly", "Papillon féerique", listOf("🦋", "🦋🌸", "🦋✨", "🦋👑"), "Un papillon magique."),
    Pet("polar_bear", "Ours polaire", listOf("🐻‍❄️", "🐻‍❄️❄️", "🐻‍❄️✨", "🐻‍❄️👑"), "Un ours protecteur."),
    Pet("owl", "Hibou magique", listOf("🦉", "🦉🌙", "🦉✨", "🦉👑"), "Un hibou sage."),
    Pet("unicorn", "Licorne", listOf("🦄", "🦄🌸", "🦄🌈", "🦄👑"), "Une créature féerique.")
)

private fun dateLabel(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LifeApp() }
    }
}

@Composable
fun LifeApp() {

    var tab by remember { mutableIntStateOf(0) }

    var xp by remember { mutableIntStateOf(120) }
    var coins by remember { mutableIntStateOf(45) }

    var petStage by remember { mutableIntStateOf(0) }
    var petXp by remember { mutableIntStateOf(0) }

    var activePetId by remember { mutableStateOf<String?>("fox") }

    var unlockedPets by remember { mutableStateOf(setOf("fox")) }
    var completedPets by remember { mutableStateOf(emptySet<String>()) }

    val today = remember { LocalDate.now() }
    val tomorrow = remember { today.plusDays(1) }

    var nextMissionId by remember { mutableIntStateOf(5) }

    var missionsByDate by remember {
        mutableStateOf(
            mapOf(
                today.toString() to listOf(
                    Mission(1, "Ranger ma chambre", "90 min · photo avant / après", 100, 20, "✨"),
                    Mission(2, "Faire les courses", "Acheter les ingrédients", 30, 8, "🛍️"),
                    Mission(3, "Travailler les cours", "45 min · minuteur", 70, 14, "📚"),
                    Mission(4, "Atelier pâtisserie", "Gaufres et roses des sables", 50, 10, "🧁")
                )
            )
        )
    }

    val todayMissions = missionsByDate[today.toString()] ?: emptyList()
    val tomorrowMissions = missionsByDate[tomorrow.toString()] ?: emptyList()

    val activePet = petCatalog.firstOrNull { it.id == activePetId }

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

                    val labels = listOf(
                        "Aujourd'hui",
                        "Collection",
                        "Récompenses",
                        "Profil"
                    )

                    val icons = listOf(
                        Icons.Default.CheckCircle,
                        Icons.Default.Pets,
                        Icons.Default.CardGiftcard,
                        Icons.Default.BarChart
                    )

                    labels.forEachIndexed { index, label ->
                        NavigationBarItem(
                            selected = tab == index,
                            onClick = { tab = index },
                            icon = {
                                Icon(icons[index], contentDescription = label)
                            },
                            label = {
                                Text(label, fontSize = 10.sp)
                            }
                        )
                    }
                }
            }

        ) { padding ->

            when (tab) {

                0 -> HomeScreen(
                    padding = padding,
                    xp = xp,
                    coins = coins,
                    missions = todayMissions,
                    tomorrowMissions = tomorrowMissions,
                    tomorrow = tomorrow,
                    activePet = activePet,
                    petStage = petStage,
                    petXp = petXp,

                    onAddMission = { title, detail, rewardXp, rewardCoins, icon ->

                        val newMission = Mission(
                            nextMissionId,
                            title,
                            detail,
                            rewardXp,
                            rewardCoins,
                            icon
                        )

                        missionsByDate = missionsByDate +
                            (tomorrow.toString() to (tomorrowMissions + newMission))

                        nextMissionId++
                    },

                    onDeleteTomorrowMission = { id ->
                        missionsByDate = missionsByDate +
                            (tomorrow.toString() to tomorrowMissions.filterNot { it.id == id })
                    },

                    onComplete = { id ->

                        val target = todayMissions.firstOrNull { it.id == id }

                        if (target != null && !target.done) {

                            missionsByDate = missionsByDate +
                                (today.toString() to todayMissions.map {
                                    if (it.id == id) it.copy(done = true) else it
                                })

                            xp += target.xp
                            coins += target.coins

                            if (activePetId != null) {

                                petXp += target.xp

                                if (petXp >= XP_PER_EVOLUTION) {

                                    petXp -= XP_PER_EVOLUTION

                                    if (petStage < 3) {
                                        petStage++
                                    } else {

                                        val finishedId = activePetId!!

                                        completedPets = completedPets + finishedId

                                        val nextPet = petCatalog.firstOrNull {
                                            it.id !in unlockedPets &&
                                            it.id !in completedPets
                                        }

                                        if (nextPet != null) {
                                            unlockedPets = unlockedPets + nextPet.id
                                            activePetId = nextPet.id
                                            petStage = 0
                                            petXp = 0
                                        } else {
                                            activePetId = null
                                            petStage = 0
                                            petXp = 0
                                        }
                                    }
                                }
                            }
                        }
                    }
                )

                1 -> CollectionScreen(
                    padding,
                    unlockedPets,
                    completedPets,
                    activePetId,
                    petStage,
                    petXp
                )

                2 -> RewardsScreen(
                    padding,
                    coins,
                    onSpend = { cost ->
                        if (coins >= cost) coins -= cost
                    }
                )

                else -> ProfileScreen(
                    padding,
                    xp,
                    coins,
                    todayMissions.count { it.done },
                    todayMissions.size,
                    completedPets.size
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    padding: PaddingValues,
    xp: Int,
    coins: Int,
    missions: List<Mission>,
    tomorrowMissions: List<Mission>,
    tomorrow: LocalDate,
    activePet: Pet?,
    petStage: Int,
    petXp: Int,
    onAddMission: (String, String, Int, Int, String) -> Unit,
    onDeleteTomorrowMission: (Int) -> Unit,
    onComplete: (Int) -> Unit
) {

    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("LIFE", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Ink, letterSpacing = 2.sp)
                    Text("Ton quotidien, ton aventure.", color = Muted, fontSize = 13.sp)
                }

                Surface(shape = CircleShape, color = Color.White) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🪙 ")
                        Text("$coins", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Ton compagnon", color = Muted, fontSize = 12.sp)
                            Text(
                                activePet?.name ?: "Collection terminée !",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text(
                                if (activePet != null) "Évolution ${petStage + 1}/4"
                                else "Tu as réuni tous les compagnons.",
                                color = Muted,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            activePet?.evolutions?.get(petStage) ?: "🏆",
                            fontSize = 48.sp
                        )
                    }

                    if (activePet != null) {
                        LinearProgressIndicator(
                            progress = { petXp / XP_PER_EVOLUTION.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Pink,
                            trackColor = Pink.copy(alpha = .15f)
                        )

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("$petXp / $XP_PER_EVOLUTION XP", fontWeight = FontWeight.Bold, color = Pink)
                            Text("Prochaine évolution", color = Muted, fontSize = 11.sp)
                        }
                    }

                    Text("XP total : $xp", color = Muted, fontSize = 11.sp)
                }
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tes missions", fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("${missions.count { it.done }}/${missions.size}", color = Muted, fontWeight = FontWeight.SemiBold)
            }
        }

        item {
            if (missions.isNotEmpty()) {
                LinearProgressIndicator(
                    progress = {
                        missions.count { it.done }.toFloat() / missions.size
                    },
                    modifier = Modifier.fillMaxWidth().height(7.dp),
                    color = Blue,
                    trackColor = Blue.copy(alpha = .15f)
                )
            }
        }

        items(missions, key = { it.id }) { mission ->
            MissionCard(mission) {
                onComplete(mission.id)
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFF0D9),
                modifier = Modifier.fillMaxWidth()
            ) {
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

        item {
            Divider(color = Pink.copy(alpha = .25f))

            Text(
                "Préparer demain · ${dateLabel(tomorrow)}",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )

            Text(
                "Organise ta prochaine journée tranquillement.",
                color = Muted,
                fontSize = 12.sp
            )

            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Pink)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Ajouter une mission")
            }

            if (tomorrowMissions.isEmpty()) {
                Text("Aucune mission prévue pour demain.", color = Muted, fontSize = 12.sp)
            }
        }

        items(tomorrowMissions, key = { "tomorrow_${it.id}" }) { mission ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(mission.icon, fontSize = 24.sp)
                    Spacer(Modifier.width(10.dp))

                    Column(Modifier.weight(1f)) {
                        Text(mission.title, fontWeight = FontWeight.Bold, color = Ink)
                        Text("+${mission.xp} XP · +${mission.coins} pièces", color = Orange, fontSize = 11.sp)
                    }

                    IconButton(onClick = { onDeleteTomorrowMission(mission.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Muted)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddMissionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, detail, rewardXp, rewardCoins, icon ->
                onAddMission(title, detail, rewardXp, rewardCoins, icon)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddMissionDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Int, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var xpText by remember { mutableStateOf("50") }
    var coinsText by remember { mutableStateOf("10") }
    var icon by remember { mutableStateOf("⭐") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouvelle mission") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nom de la mission") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text("Description (facultative)") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text("Emoji") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = xpText,
                    onValueChange = { xpText = it.filter(Char::isDigit) },
                    label = { Text("XP gagnés") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = coinsText,
                    onValueChange = { coinsText = it.filter(Char::isDigit) },
                    label = { Text("Pièces gagnées") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val xpValue = xpText.toIntOrNull() ?: 50
                    val coinValue = coinsText.toIntOrNull() ?: 10

                    if (title.isNotBlank()) {
                        onSave(
                            title.trim(),
                            detail.trim(),
                            xpValue.coerceAtLeast(0),
                            coinValue.coerceAtLeast(0),
                            icon.ifBlank { "⭐" }
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Ajouter", color = Pink)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun MissionCard(mission: Mission, onComplete: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = when (mission.id % 4) {
                    0 -> Yellow.copy(alpha = .25f)
                    1 -> Pink.copy(alpha = .18f)
                    2 -> Blue.copy(alpha = .18f)
                    else -> Orange.copy(alpha = .18f)
                }
            ) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Text(mission.icon, fontSize = 23.sp)
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(mission.title, fontWeight = FontWeight.Bold, color = Ink)
                Text(mission.detail, color = Muted, fontSize = 11.sp)
                Text("+${mission.xp} XP · +${mission.coins} pièces", color = Orange, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            if (mission.done) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Terminée", tint = Green)
            } else {
                FilledTonalButton(
                    onClick = onComplete,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Pink.copy(alpha = .16f),
                        contentColor = Ink
                    )
                ) {
                    Text("Valider", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun CollectionScreen(
    padding: PaddingValues,
    unlockedPets: Set<String>,
    completedPets: Set<String>,
    activePetId: String?,
    petStage: Int,
    petXp: Int
) {
    Column(
        Modifier.fillMaxSize().padding(padding).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Ta collection", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text("Découvre les 12 compagnons et leurs évolutions.", color = Muted)

        petCatalog.forEach { pet ->

            val isUnlocked = pet.id in unlockedPets
            val isCompleted = pet.id in completedPets
            val isActive = pet.id == activePetId

            val stage = when {
                isCompleted -> 3
                isActive -> petStage
                else -> 0
            }

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Pink.copy(alpha = .13f)
                    ) {
                        Box(Modifier.size(58.dp), contentAlignment = Alignment.Center) {
                            Text(
                                if (isUnlocked) pet.evolutions[stage] else "🔒",
                                fontSize = 32.sp
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(pet.name, fontWeight = FontWeight.Bold, color = Ink)

                        Text(
                            when {
                                isCompleted -> "Collection terminée · 4/4"
                                isActive -> "Ton compagnon actuel · ${stage + 1}/4"
                                isUnlocked -> "Débloqué"
                                else -> "À découvrir"
                            },
                            color = if (isCompleted) Green else Muted,
                            fontSize = 11.sp
                        )

                        if (isActive) {
                            LinearProgressIndicator(
                                progress = { petXp / XP_PER_EVOLUTION.toFloat() },
                                modifier = Modifier.fillMaxWidth().height(5.dp),
                                color = Pink,
                                trackColor = Pink.copy(alpha = .15f)
                            )
                        }
                    }

                    if (isCompleted) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green)
                    } else if (isActive) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Pink)
                    }
                }
            }
        }

        Text(
            "300 XP sont nécessaires pour chaque évolution.",
            color = Muted,
            fontSize = 11.sp
        )
    }
}

@Composable
fun RewardsScreen(
    padding: PaddingValues,
    coins: Int,
    onSpend: (Int) -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(padding).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Tes récompenses", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text("Tu as $coins pièces à dépenser.", color = Muted)

        RewardCard("Pause détente", "15 minutes de pause téléphone", "☕", 20) {
            onSpend(20)
        }

        RewardCard("Pause bonus", "30 minutes de pause téléphone", "🎧", 35) {
            onSpend(35)
        }

        RewardCard("Accessoire animal", "Un accessoire pour ton compagnon", "🎀", 25) {
            onSpend(25)
        }

        Text(
            "Les pauses téléphone seront connectées au futur mode de concentration.",
            fontSize = 11.sp,
            color = Muted
        )
    }
}

@Composable
fun RewardCard(
    title: String,
    detail: String,
    emoji: String,
    cost: Int,
    onBuy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 30.sp)
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(detail, color = Muted, fontSize = 11.sp)
                Text("$cost pièces", color = Orange, fontSize = 11.sp)
            }

            TextButton(
                onClick = onBuy,
                enabled = cost <= 100000
            ) {
                Text("Obtenir")
            }
        }
    }
}

@Composable
fun ProfileScreen(
    padding: PaddingValues,
    xp: Int,
    coins: Int,
    completed: Int,
    total: Int,
    petsCompleted: Int
) {
    Column(
        Modifier.fillMaxSize().padding(padding).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Ton espace", fontSize = 27.sp, fontWeight = FontWeight.Bold, color = Ink)

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                StatLine("Expérience gagnée", "$xp XP")
                StatLine("Pièces disponibles", "$coins")
                StatLine("Missions terminées aujourd'hui", "$completed / $total")
                StatLine("Compagnons terminés", "$petsCompleted / ${petCatalog.size}")
                StatLine("Temps d'écran", "À connecter au mode concentration")
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Blue.copy(alpha = .14f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = Blue)
                Spacer(Modifier.width(10.dp))

                Column {
                    Text("Mode concentration", fontWeight = FontWeight.Bold)
                    Text(
                        "Le blocage Android sera ajouté dans une prochaine étape.",
                        color = Muted,
                        fontSize = 11.sp
                    )
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

