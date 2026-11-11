
package com.lifequest.life

import android.os.Bundle
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.delay
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


private fun saveMissionPhoto(context: Context, uri: Uri): String? {
    return try {
        val directory = File(context.filesDir, "mission_photos")
        if (!directory.exists()) directory.mkdirs()
        val destination = File(directory, "${UUID.randomUUID()}.photo")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destination).use { output -> input.copyTo(output) }
        } ?: return null
        destination.absolutePath
    } catch (_: Exception) {
        null
    }
}

data class Mission(
    val id: Int,
    val title: String,
    val detail: String,
    val xp: Int,
    val coins: Int,
    val icon: String,
    val done: Boolean = false,
    val validation: String = "manual",
    val photoMode: String = "single",
    val timerMinutes: Int = 30,
    val beforePhotoUri: String? = null,
    val afterPhotoUri: String? = null,
    val timerRemainingSeconds: Int? = null,
    val timerEndTimeMillis: Long? = null
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


private fun loadMissions(prefs: SharedPreferences, today: LocalDate): Map<String, List<Mission>> {
    val saved = prefs.getString("missionsByDate", null)
    if (saved == null) return mapOf(today.toString() to listOf(
        Mission(1, "Ranger ma chambre", "90 min · photo avant / après", 100, 20, "✨", validation = "photo", photoMode = "before_after"),
        Mission(2, "Faire les courses", "Acheter les ingrédients", 30, 8, "🛍️"),
        Mission(3, "Travailler les cours", "45 min · minuteur", 70, 14, "📚", validation = "timer", timerMinutes = 45),
        Mission(4, "Atelier pâtisserie", "Gaufres et roses des sables", 50, 10, "🧁")
    ))
    return try {
        val root = JSONObject(saved)
        val result = mutableMapOf<String, List<Mission>>()
        val keys = root.keys()
        while (keys.hasNext()) {
            val date = keys.next()
            val array = root.optJSONArray(date) ?: JSONArray()
            val list = mutableListOf<Mission>()
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                list.add(Mission(
                    item.optInt("id"), item.optString("title"), item.optString("detail"),
                    item.optInt("xp"), item.optInt("coins"), item.optString("icon", "⭐"),
                    item.optBoolean("done", false),
                    item.optString("validation", "manual"),
                    item.optString("photoMode", "single"),
                    item.optInt("timerMinutes", 30),
                    item.optString("beforePhotoUri").takeIf { it.isNotBlank() },
                    item.optString("afterPhotoUri").takeIf { it.isNotBlank() },
                    if (item.has("timerRemainingSeconds") && !item.isNull("timerRemainingSeconds")) item.optInt("timerRemainingSeconds") else null,
                    if (item.has("timerEndTimeMillis") && !item.isNull("timerEndTimeMillis")) item.optLong("timerEndTimeMillis") else null
                ))
            }
            result[date] = list
        }
        result
    } catch (_: Exception) { emptyMap() }
}

private fun encodeMissions(data: Map<String, List<Mission>>): String {
    val root = JSONObject()
    data.forEach { (date, list) ->
        val array = JSONArray()
        list.forEach { m ->
            array.put(JSONObject().put("id", m.id).put("title", m.title)
                .put("detail", m.detail).put("xp", m.xp).put("coins", m.coins)
                .put("icon", m.icon).put("done", m.done)
                .put("validation", m.validation).put("photoMode", m.photoMode)
                .put("timerMinutes", m.timerMinutes)
                .put("beforePhotoUri", m.beforePhotoUri ?: "")
                .put("afterPhotoUri", m.afterPhotoUri ?: "")
                .put("timerRemainingSeconds", m.timerRemainingSeconds ?: JSONObject.NULL)
                .put("timerEndTimeMillis", m.timerEndTimeMillis ?: JSONObject.NULL))
        }
        root.put(date, array)
    }
    return root.toString()
}

private fun loadSavedSet(prefs: SharedPreferences, key: String, default: Set<String>): Set<String> =
    prefs.getStringSet(key, default)?.toSet() ?: default

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

    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("life_data", Context.MODE_PRIVATE) }

    var tab by remember { mutableIntStateOf(0) }
    var xp by remember { mutableIntStateOf(prefs.getInt("xp", 120)) }
    var coins by remember { mutableIntStateOf(prefs.getInt("coins", 45)) }
    var petStage by remember { mutableIntStateOf(prefs.getInt("petStage", 0)) }
    var petXp by remember { mutableIntStateOf(prefs.getInt("petXp", 0)) }
    var activePetId by remember { mutableStateOf(prefs.getString("activePetId", "fox")) }
    var unlockedPets by remember { mutableStateOf(loadSavedSet(prefs, "unlockedPets", setOf("fox"))) }
    var completedPets by remember { mutableStateOf(loadSavedSet(prefs, "completedPets", emptySet())) }

    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)
    var nextMissionId by remember { mutableIntStateOf(prefs.getInt("nextMissionId", 5)) }
    var missionsByDate by remember { mutableStateOf(loadMissions(prefs, today)) }

    LaunchedEffect(xp, coins, petStage, petXp, activePetId, unlockedPets, completedPets, nextMissionId, missionsByDate) {
        prefs.edit()
            .putInt("xp", xp).putInt("coins", coins)
            .putInt("petStage", petStage).putInt("petXp", petXp)
            .putString("activePetId", activePetId)
            .putStringSet("unlockedPets", unlockedPets)
            .putStringSet("completedPets", completedPets)
            .putInt("nextMissionId", nextMissionId)
            .putString("missionsByDate", encodeMissions(missionsByDate))
            .apply()
    }

    val todayMissions = missionsByDate[today.toString()] ?: emptyList()
    val tomorrowMissions = missionsByDate[tomorrow.toString()] ?: emptyList()
    val historyMissions = missionsByDate.entries
        .filter { it.key < today.toString() }
        .sortedByDescending { it.key }
        .map { it.key to it.value }

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
                    historyMissions = historyMissions,
                    tomorrow = tomorrow,
                    activePet = activePet,
                    petStage = petStage,
                    petXp = petXp,

                    onAddMission = { title, detail, rewardXp, rewardCoins, icon, validation, photoMode, timerMinutes ->

                        val newMission = Mission(
                            nextMissionId,
                            title,
                            detail,
                            rewardXp,
                            rewardCoins,
                            icon,
                            validation = validation,
                            photoMode = photoMode,
                            timerMinutes = timerMinutes
                        )

                        missionsByDate = missionsByDate +
                            (tomorrow.toString() to (tomorrowMissions + newMission))

                        nextMissionId++
                    },

                    onDeleteTomorrowMission = { id ->
                        missionsByDate = missionsByDate +
                            (tomorrow.toString() to tomorrowMissions.filterNot { it.id == id })
                    },

                    onEditTomorrowMission = { id, title, detail, rewardXp, rewardCoins, icon, validation, photoMode, timerMinutes ->
                        missionsByDate = missionsByDate +
                            (tomorrow.toString() to tomorrowMissions.map { mission ->
                                if (mission.id == id) mission.copy(title = title, detail = detail,
                                    xp = rewardXp, coins = rewardCoins, icon = icon,
                                    validation = validation, photoMode = photoMode,
                                    timerMinutes = timerMinutes) else mission
                            })
                    },

                    onPhotoSelected = { id, uri, before ->
                        val currentTodayMissions = missionsByDate[today.toString()] ?: emptyList()
                        missionsByDate = missionsByDate + (today.toString() to currentTodayMissions.map { mission ->
                            if (mission.id == id) {
                                if (before) mission.copy(beforePhotoUri = uri) else mission.copy(afterPhotoUri = uri)
                            } else mission
                        })
                    },

                    onTimerStateChanged = { id, endTime, remaining ->
                        val currentTodayMissions = missionsByDate[today.toString()] ?: emptyList()
                        missionsByDate = missionsByDate + (today.toString() to currentTodayMissions.map { mission ->
                            if (mission.id == id) mission.copy(timerEndTimeMillis = endTime, timerRemainingSeconds = remaining) else mission
                        })
                    },

                    onComplete = { id ->

                        val currentTodayMissions = missionsByDate[today.toString()] ?: emptyList()
                        val target = currentTodayMissions.firstOrNull { it.id == id }

                        if (target != null && !target.done) {

                            missionsByDate = missionsByDate +
                                (today.toString() to currentTodayMissions.map {
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
    historyMissions: List<Pair<String, List<Mission>>>,
    tomorrow: LocalDate,
    activePet: Pet?,
    petStage: Int,
    petXp: Int,
    onAddMission: (String, String, Int, Int, String, String, String, Int) -> Unit,
    onDeleteTomorrowMission: (Int) -> Unit,
    onEditTomorrowMission: (Int, String, String, Int, Int, String, String, String, Int) -> Unit,
    onPhotoSelected: (Int, String, Boolean) -> Unit,
    onTimerStateChanged: (Int, Long?, Int) -> Unit,
    onComplete: (Int) -> Unit
) {

    var showAddDialog by remember { mutableStateOf(false) }
    var editingMission by remember { mutableStateOf<Mission?>(null) }

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
            MissionCard(mission,
                onComplete = { onComplete(mission.id) },
                onTimerStateChanged = { endTime, remaining -> onTimerStateChanged(mission.id, endTime, remaining) },
                onPhotoSelected = { uri, before ->
                    val otherPhotoExists = if (before) mission.afterPhotoUri != null else mission.beforePhotoUri != null
                    onPhotoSelected(mission.id, uri, before)
                    if (mission.photoMode == "single" || otherPhotoExists) onComplete(mission.id)
                }
            )
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

                    IconButton(onClick = { editingMission = mission; showAddDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = Pink)
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
            initialMission = editingMission,
            onDismiss = { showAddDialog = false; editingMission = null },
            onSave = { title, detail, rewardXp, rewardCoins, icon, validation, photoMode, timerMinutes ->
                val current = editingMission
                if (current == null) onAddMission(title, detail, rewardXp, rewardCoins, icon, validation, photoMode, timerMinutes)
                else onEditTomorrowMission(current.id, title, detail, rewardXp, rewardCoins, icon, validation, photoMode, timerMinutes)
                showAddDialog = false
                editingMission = null
            }
        )
    }

    item {
        if (historyMissions.isNotEmpty()) {
            Divider(color = Pink.copy(alpha = .25f))
            Text("Historique", fontSize = 19.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text("Retrouve les missions de tes journées précédentes.", color = Muted, fontSize = 12.sp)
        }
    }
    items(historyMissions, key = { "history_${it.first}" }) { entry ->
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(dateLabel(LocalDate.parse(entry.first)), fontWeight = FontWeight.Bold, color = Ink)
                Text("${entry.second.count { it.done }}/${entry.second.size} missions terminées", color = Muted, fontSize = 12.sp)
                entry.second.forEach { oldMission ->
                    Text("${if (oldMission.done) "✓" else "•"} ${oldMission.title}",
                        color = if (oldMission.done) Green else Muted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AddMissionDialog(
    initialMission: Mission? = null,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Int, String, String, String, Int) -> Unit
) {
    var title by remember(initialMission?.id) { mutableStateOf(initialMission?.title ?: "") }
    var detail by remember(initialMission?.id) { mutableStateOf(initialMission?.detail ?: "") }
    var xpText by remember(initialMission?.id) { mutableStateOf((initialMission?.xp ?: 50).toString()) }
    var coinsText by remember(initialMission?.id) { mutableStateOf((initialMission?.coins ?: 10).toString()) }
    var icon by remember(initialMission?.id) { mutableStateOf(initialMission?.icon ?: "⭐") }
    var validation by remember(initialMission?.id) { mutableStateOf(initialMission?.validation ?: "manual") }
    var photoMode by remember(initialMission?.id) { mutableStateOf(initialMission?.photoMode ?: "single") }
    var timerText by remember(initialMission?.id) { mutableStateOf((initialMission?.timerMinutes ?: 30).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialMission == null) "Nouvelle mission" else "Modifier la mission") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Nom de la mission") }, singleLine = true)
                OutlinedTextField(value = detail, onValueChange = { detail = it }, label = { Text("Description (facultative)") }, singleLine = true)
                OutlinedTextField(value = icon, onValueChange = { icon = it }, label = { Text("Emoji") }, singleLine = true)
                OutlinedTextField(value = xpText, onValueChange = { xpText = it.filter(Char::isDigit) }, label = { Text("XP gagnés") }, singleLine = true)
                OutlinedTextField(value = coinsText, onValueChange = { coinsText = it.filter(Char::isDigit) }, label = { Text("Pièces gagnées") }, singleLine = true)
                Text("Comment valider cette mission ?", fontWeight = FontWeight.SemiBold, color = Ink)
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    TextButton(onClick = { validation = "manual" }) { Text(if (validation == "manual") "✓ Manuelle" else "Manuelle") }
                    TextButton(onClick = { validation = "photo" }) { Text(if (validation == "photo") "✓ Photo" else "Photo") }
                    TextButton(onClick = { validation = "timer" }) { Text(if (validation == "timer") "✓ Minuteur" else "Minuteur") }
                }
                if (validation == "photo") {
                    Text("Type de photo", fontWeight = FontWeight.SemiBold, color = Ink)
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        TextButton(onClick = { photoMode = "single" }) { Text(if (photoMode == "single") "✓ Simple" else "Simple") }
                        TextButton(onClick = { photoMode = "before_after" }) { Text(if (photoMode == "before_after") "✓ Avant / après" else "Avant / après") }
                    }
                }
                if (validation == "timer") {
                    OutlinedTextField(value = timerText, onValueChange = { timerText = it.filter(Char::isDigit).take(3) },
                        label = { Text("Durée en minutes") }, singleLine = true)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) onSave(title.trim(), detail.trim(),
                    (xpText.toIntOrNull() ?: 50).coerceAtLeast(0),
                    (coinsText.toIntOrNull() ?: 10).coerceAtLeast(0),
                    icon.ifBlank { "⭐" }, validation, photoMode,
                    (timerText.toIntOrNull() ?: 30).coerceIn(1, 240))
            }, enabled = title.isNotBlank()) {
                Text(if (initialMission == null) "Ajouter" else "Enregistrer", color = Pink)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
fun MissionCard(mission: Mission, onComplete: () -> Unit, onPhotoSelected: (String, Boolean) -> Unit, onTimerStateChanged: (Long?, Int) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var photoStage by remember(mission.id) { mutableStateOf("single") }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val timerEnd = mission.timerEndTimeMillis
    val timerSeconds = if (timerEnd != null) ((timerEnd - nowMillis) / 1000L).toInt().coerceAtLeast(0)
        else (mission.timerRemainingSeconds ?: (mission.timerMinutes * 60)).coerceAtLeast(0)
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val savedPath = saveMissionPhoto(context, uri)
            if (savedPath != null) onPhotoSelected(savedPath, photoStage == "before")
        }
    }
    LaunchedEffect(timerEnd, mission.done) {
        if (timerEnd != null && !mission.done) {
            while (true) {
                nowMillis = System.currentTimeMillis()
                if (nowMillis >= timerEnd) {
                    onTimerStateChanged(null, 0)
                    onComplete()
                    break
                }
                delay(250)
            }
        }
    }
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = Pink.copy(alpha = .15f)) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) { Text(mission.icon, fontSize = 23.sp) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(mission.title, fontWeight = FontWeight.Bold, color = Ink)
                Text(mission.detail, color = Muted, fontSize = 11.sp)
                Text("+${mission.xp} XP · +${mission.coins} pièces", color = Orange, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                if (mission.done) {
                    Text("Mission terminée ✓", color = Green, fontSize = 12.sp)
                } else when (mission.validation) {
                    "photo" -> if (mission.photoMode == "single") {
                        Text(if (mission.beforePhotoUri != null) "Photo ajoutée ✓" else "Ajoute une photo pour valider", color = Muted, fontSize = 11.sp)
                        TextButton(onClick = { photoStage = "single"; photoPicker.launch("image/*") }) { Text("📷 Ajouter une photo") }
                    } else {
                        Text("Avant : ${if (mission.beforePhotoUri != null) "✓" else "à ajouter"}", color = Muted, fontSize = 11.sp)
                        Text("Après : ${if (mission.afterPhotoUri != null) "✓" else "à ajouter"}", color = Muted, fontSize = 11.sp)
                        Row {
                            TextButton(onClick = { photoStage = "before"; photoPicker.launch("image/*") }) { Text("Photo avant") }
                            TextButton(onClick = { photoStage = "after"; photoPicker.launch("image/*") }) { Text("Photo après") }
                        }
                    }
                    "timer" -> {
                        Text("Temps restant : %02d:%02d".format(timerSeconds / 60, timerSeconds % 60), color = Blue, fontSize = 12.sp)
                        Row {
                            TextButton(onClick = {
                                val activeEnd = mission.timerEndTimeMillis
                                if (activeEnd != null) {
                                    val remaining = ((activeEnd - System.currentTimeMillis()) / 1000L).toInt().coerceAtLeast(0)
                                    onTimerStateChanged(null, remaining)
                                } else {
                                    onTimerStateChanged(System.currentTimeMillis() + timerSeconds * 1000L, timerSeconds)
                                }
                            }) { Text(if (mission.timerEndTimeMillis != null) "Pause" else "Démarrer") }
                            TextButton(onClick = { onTimerStateChanged(null, mission.timerMinutes * 60) }) { Text("Réinitialiser") }
                        }
                    }
                    else -> FilledTonalButton(onClick = onComplete,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Pink.copy(alpha = .16f), contentColor = Ink)) {
                        Text("Valider", fontSize = 11.sp)
                    }
                }
            }
            if (mission.done) Icon(Icons.Default.CheckCircle, contentDescription = "Terminée", tint = Green)
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

        RewardCard("Pause détente", "15 minutes de pause téléphone", "☕", 20, canAfford = coins >= 20) { onSpend(20) }
        RewardCard("Pause bonus", "30 minutes de pause téléphone", "🎧", 35, canAfford = coins >= 35) { onSpend(35) }
        RewardCard("Accessoire animal", "Un accessoire pour ton compagnon", "🎀", 25, canAfford = coins >= 25) { onSpend(25) }

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
    canAfford: Boolean,
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
                enabled = canAfford
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

