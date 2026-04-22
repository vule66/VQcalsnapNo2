package com.example.vqcalsnap.presentation.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.vqcalsnap.data.local.MealEntity
import com.example.vqcalsnap.presentation.navigation.BottomNavBar
import com.example.vqcalsnap.presentation.navigation.Graph
import com.example.vqcalsnap.presentation.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel? = null,
    previewMeals: List<MealEntity>? = null,
    previewTotalCalories: Int? = null,
    previewProfile: com.example.vqcalsnap.domain.model.UserProfile? = null
) {
    val runtimeViewModel = if (previewMeals == null || previewTotalCalories == null || previewProfile == null) {
        viewModel ?: hiltViewModel<HistoryViewModel>()
    } else {
        null
    }

    val meals by (runtimeViewModel?.meals?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(previewMeals ?: emptyList()) })
    val totalCalories by (runtimeViewModel?.totalCalories?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(previewTotalCalories ?: 0) })
    val profile by (runtimeViewModel?.profile?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(previewProfile ?: com.example.vqcalsnap.domain.model.UserProfile()) })
    val target = profile.dailyCalorieTarget
    val consumed = totalCalories ?: 0
    val remaining = target - consumed
    val progress = if (target > 0) (consumed.toFloat() / target).coerceIn(0f, 1f) else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hôm nay", fontWeight = FontWeight.SemiBold)
                        Text(
                            SimpleDateFormat("EEEE, dd/MM/yyyy", Locale("vi")).format(Date()),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController, current = Screen.History)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Progress calo
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Calo đã nạp", fontWeight = FontWeight.Medium)
                            Text(
                                "$consumed / $target kcal",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = if (progress >= 1f)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                if (remaining >= 0) "Còn lại: $remaining kcal"
                                else "Vượt quá: ${-remaining} kcal",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (remaining < 0)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "${(progress * 100).toInt()}%",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Tổng dinh dưỡng
            if (meals.isNotEmpty()) {
                item {
                    val totalProtein = meals.sumOf { it.protein.toDouble() }.toInt()
                    val totalCarbs = meals.sumOf { it.carbs.toDouble() }.toInt()
                    val totalFat = meals.sumOf { it.fat.toDouble() }.toInt()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NutritionSummaryCard("Carb", "${totalCarbs}g", Modifier.weight(1f))
                        NutritionSummaryCard("Protein", "${totalProtein}g", Modifier.weight(1f))
                        NutritionSummaryCard("Fat", "${totalFat}g", Modifier.weight(1f))
                    }
                }
            }

            // Tiêu đề danh sách
            item {
                Text(
                    "Bữa ăn hôm nay (${meals.size} món)",
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }

            // Danh sách món
            if (meals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Chưa có món ăn nào",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    navController.navigate(Screen.Camera.route) {
                                        popUpTo(Graph.Main.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text("Chụp món ăn ngay")
                            }
                        }
                    }
                }
            } else {
                items(meals, key = { it.id }) { meal ->
                    MealItem(
                        meal = meal,
                        onDelete = { runtimeViewModel?.deleteMeal(meal.id) }
                    )
                }
            }

            // Tổng kết
            if (meals.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Tổng calo hôm nay",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "$consumed kcal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 900)
@Composable
private fun HistoryScreenPreview() {
    HistoryScreen(
        navController = rememberNavController(),
        previewMeals = listOf(
            MealEntity(id = 1, userId = 1, name = "Pho bo", calories = 450, protein = 20f, carbs = 58f, fat = 14f, portionSize = 350f, portionUnit = "g"),
            MealEntity(id = 2, userId = 1, name = "Sua chua", calories = 120, protein = 4f, carbs = 18f, fat = 3f, portionSize = 100f, portionUnit = "g")
        ),
        previewTotalCalories = 570,
        previewProfile = com.example.vqcalsnap.domain.model.UserProfile(dailyCalorieTarget = 2000)
    )
}

@Composable
fun MealItem(meal: MealEntity, onDelete: () -> Unit) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = timeFormat.format(Date(meal.timestamp))

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(meal.name, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Text(
                    "${meal.portionSize.toInt()}${meal.portionUnit} — $time",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "${meal.calories} kcal",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Xóa",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NutritionSummaryCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}