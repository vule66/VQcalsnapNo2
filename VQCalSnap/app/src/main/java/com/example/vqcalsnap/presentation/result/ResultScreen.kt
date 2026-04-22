package com.example.vqcalsnap.presentation.result

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.vqcalsnap.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    imagePath: String,
    navController: NavController,
    viewModel: ResultViewModel? = null,
    previewUiState: ResultUiState? = null
) {
    val runtimeViewModel = if (previewUiState == null) {
        viewModel ?: hiltViewModel<ResultViewModel>()
    } else {
        null
    }
    val uiState by (runtimeViewModel?.uiState?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(previewUiState ?: ResultUiState.Loading) })
    val imageUri = Uri.parse(Uri.decode(imagePath))
    var savedSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(imagePath, runtimeViewModel) {
        runtimeViewModel?.analyzeFood(imageUri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kết quả nhận diện", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Kiểm tra & chỉnh khẩu phần",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Ảnh món ăn",
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            when (val state = uiState) {
                is ResultUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("AI đang phân tích món ăn...")
                        }
                    }
                }

                is ResultUiState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(state.message, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { runtimeViewModel?.analyzeFood(imageUri) }) {
                                Text("Thử lại")
                            }
                        }
                    }
                }

                is ResultUiState.Success -> {
                    val foods = state.foods

                    // Tổng calo tất cả món
                    val totalCalories = foods.sumOf { it.calories }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Tổng calo",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "$totalCalories kcal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Từng món
                    foods.forEachIndexed { index, food ->
                        FoodItemCard(food = food, index = index)
                    }

                    // Buttons
                    if (savedSuccess) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Đã lưu vào lịch sử!",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Button(
                        onClick = {
                            runtimeViewModel?.saveMeal(foods)
                            savedSuccess = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !savedSuccess
                    ) {
                        Text(if (savedSuccess) "Đã lưu" else "Thêm tất cả vào lịch sử")
                    }

                    OutlinedButton(
                        onClick = {
                            navController.navigate(Screen.History.route) {
                                popUpTo(Screen.Camera.route) { inclusive = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Xem lịch sử hôm nay")
                    }

                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Chụp lại")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 420, heightDp = 900)
@Composable
private fun ResultScreenPreview() {
    ResultScreen(
        imagePath = Uri.encode("content://preview/image"),
        navController = rememberNavController(),
        previewUiState = ResultUiState.Success(
            foods = listOf(
                FoodResult(name = "Com ga", calories = 520, protein = 28f, carbs = 62f, fat = 15f, portionSize = 320f),
                FoodResult(name = "Rau luoc", calories = 45, protein = 2f, carbs = 7f, fat = 1f, portionSize = 100f)
            )
        )
    )
}

@Composable
fun FoodItemCard(food: FoodResult, index: Int) {
    var portion by remember { mutableFloatStateOf(1f) }
    var customGrams by remember { mutableStateOf("") }

    val gramsValue = customGrams.toFloatOrNull()
    val actualCalories = if (gramsValue != null && gramsValue > 0) {
        (food.calories.toFloat() / food.portionSize * gramsValue).toInt()
    } else (food.calories * portion).toInt()

    val actualProtein = if (gramsValue != null && gramsValue > 0)
        food.protein / food.portionSize * gramsValue
    else food.protein * portion

    val actualCarbs = if (gramsValue != null && gramsValue > 0)
        food.carbs / food.portionSize * gramsValue
    else food.carbs * portion

    val actualFat = if (gramsValue != null && gramsValue > 0)
        food.fat / food.portionSize * gramsValue
    else food.fat * portion

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    food.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        "$actualCalories kcal",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NutritionCard("Carb", "${actualCarbs.toInt()}g", Modifier.weight(1f))
                NutritionCard("Protein", "${actualProtein.toInt()}g", Modifier.weight(1f))
                NutritionCard("Fat", "${actualFat.toInt()}g", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Chỉnh khẩu phần
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalIconButton(onClick = {
                    if (portion > 0.5f) portion -= 0.5f
                    customGrams = ""
                }) { Text("−", fontSize = 18.sp) }
                Text(
                    "${portion}x (${(food.portionSize * portion).toInt()}${food.portionUnit})",
                    fontWeight = FontWeight.Medium
                )
                FilledTonalIconButton(onClick = {
                    if (portion < 5f) portion += 0.5f
                    customGrams = ""
                }) { Text("+", fontSize = 18.sp) }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customGrams,
                onValueChange = {
                    customGrams = it
                    if (it.isNotEmpty()) portion = 1f
                },
                label = { Text("Hoặc nhập gram thực tế") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                suffix = { Text("g") },
                placeholder = { Text("VD: 350") },
                supportingText = {
                    Text("AI ước tính 1 khẩu phần = ${food.portionSize.toInt()}g")
                }
            )
        }
    }
}

@Composable
fun NutritionCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
    }
}