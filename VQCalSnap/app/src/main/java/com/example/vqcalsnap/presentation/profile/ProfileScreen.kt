package com.example.vqcalsnap.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.vqcalsnap.domain.model.UserProfile
import com.example.vqcalsnap.presentation.navigation.Screen
import com.example.vqcalsnap.util.CalorieCalculator
import com.example.vqcalsnap.presentation.navigation.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit = {},
    viewModel: ProfileViewModel? = null,
    previewProfile: UserProfile? = null
) {
    val runtimeViewModel = if (previewProfile == null) {
        viewModel ?: hiltViewModel<ProfileViewModel>()
    } else {
        null
    }
    val runtimeProfile by (runtimeViewModel?.profile?.collectAsStateWithLifecycle()
        ?: remember { mutableStateOf(UserProfile()) })
    val activeProfile = previewProfile ?: runtimeProfile

    var name by remember(activeProfile.name) { mutableStateOf(activeProfile.name) }
    var age by remember(activeProfile.age) { mutableStateOf(activeProfile.age.toString()) }
    var weight by remember(activeProfile.weight) { mutableStateOf(activeProfile.weight.toString()) }
    var height by remember(activeProfile.height) { mutableStateOf(activeProfile.height.toString()) }
    var gender by remember(activeProfile.gender) { mutableStateOf(activeProfile.gender) }
    var goal by remember(activeProfile.goal) { mutableStateOf(activeProfile.goal) }

    var genderExpanded by remember { mutableStateOf(false) }
    var goalExpanded by remember { mutableStateOf(false) }

    val genderOptions = listOf("Nam", "Nữ")
    val goalOptions = listOf("Giảm cân", "Duy trì cân nặng", "Tăng cân")

    val calculatedProfile = UserProfile(
        age = age.toIntOrNull() ?: 22,
        gender = gender,
        weight = weight.toFloatOrNull() ?: 60f,
        height = height.toFloatOrNull() ?: 165f,
        goal = goal
    )
    val suggestedCalorieTarget = CalorieCalculator.calculateDailyTarget(calculatedProfile)

    val persistedCalorieTarget = activeProfile.dailyCalorieTarget.takeIf { it > 0 } ?: suggestedCalorieTarget

    var isEditingCalorie by remember { mutableStateOf(false) }
    var selectedCalorieTarget by remember { mutableIntStateOf(persistedCalorieTarget) }
    var calorieTargetInput by remember { mutableStateOf(selectedCalorieTarget.toString()) }

    LaunchedEffect(persistedCalorieTarget) {
        // Sync from datastore when profile changes; this should not react to temporary goal edits.
        selectedCalorieTarget = persistedCalorieTarget
        if (!isEditingCalorie) {
            calorieTargetInput = selectedCalorieTarget.toString()
        }
    }

    val editedCalorieTarget = calorieTargetInput.toIntOrNull()?.takeIf { it >= 800 }
    val displayCalorieTarget = if (isEditingCalorie) {
        editedCalorieTarget ?: selectedCalorieTarget
    } else {
        selectedCalorieTarget
    }

    val colorScheme = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current

    var lastObservedGoal by remember { mutableStateOf(goal) }
    var hasInitializedGoalObserver by remember { mutableStateOf(false) }

    LaunchedEffect(goal) {
        runtimeViewModel?.previewGoal(goal)

        if (hasInitializedGoalObserver && goal != lastObservedGoal) {
            selectedCalorieTarget = suggestedCalorieTarget
            calorieTargetInput = suggestedCalorieTarget.toString()
            isEditingCalorie = false
        }

        lastObservedGoal = goal
        hasInitializedGoalObserver = true
    }

    DisposableEffect(Unit) {
        onDispose { runtimeViewModel?.clearGoalPreview() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.primaryContainer,
                    titleContentColor = colorScheme.onPrimaryContainer,
                    actionIconContentColor = colorScheme.onPrimaryContainer
                ),
                title = {
                    Column {
                        Text("Hồ sơ của tôi", fontWeight = FontWeight.SemiBold)
                        Text("Cài đặt mục tiêu calo", fontSize = 12.sp,
                            color = colorScheme.onPrimaryContainer.copy(alpha = 0.85f))
                    }
                },
                actions = {
                    TextButton(onClick = onLogout) {
                        Text("Đăng xuất", color = colorScheme.primary)
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(navController = navController, current = Screen.Profile) }
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

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Họ tên") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                singleLine = true
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Tuổi") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine = true,
                    suffix = { Text("tuổi") }
                )
                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Giới tính") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false }
                    ) {
                        genderOptions.forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = { gender = it; genderExpanded = false }
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Cân nặng") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine = true,
                    suffix = { Text("kg") }
                )
                OutlinedTextField(
                    value = height,
                    onValueChange = { height = it },
                    label = { Text("Chiều cao") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    singleLine = true,
                    suffix = { Text("cm") }
                )
            }

            ExposedDropdownMenuBox(
                expanded = goalExpanded,
                onExpandedChange = { goalExpanded = it }
            ) {
                OutlinedTextField(
                    value = goal,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Mục tiêu") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = goalExpanded,
                    onDismissRequest = { goalExpanded = false }
                ) {
                    goalOptions.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                                onClick = {
                                    goal = it
                                    goalExpanded = false
                                }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mục tiêu: $displayCalorieTarget kcal/ngày",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary
                        )
                        IconButton(
                            onClick = {
                                if (isEditingCalorie) {
                                    val parsed = editedCalorieTarget ?: return@IconButton
                                    selectedCalorieTarget = parsed
                                    runtimeViewModel?.saveProfile(
                                        UserProfile(
                                            name = name,
                                            age = age.toIntOrNull() ?: 22,
                                            gender = gender,
                                            weight = weight.toFloatOrNull() ?: 60f,
                                            height = height.toFloatOrNull() ?: 165f,
                                            goal = goal,
                                            dailyCalorieTarget = parsed
                                        )
                                    )
                                    isEditingCalorie = false
                                    focusManager.clearFocus(force = true)
                                } else {
                                    isEditingCalorie = true
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isEditingCalorie) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = if (isEditingCalorie) "Lưu calo" else "Chỉnh calo",
                                tint = colorScheme.primary
                            )
                        }
                    }

                    if (isEditingCalorie) {
                        OutlinedTextField(
                            value = calorieTargetInput,
                            onValueChange = { calorieTargetInput = it },
                            label = { Text("Chỉnh calo mục tiêu") },
                            placeholder = { Text("VD: 1800") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            suffix = { Text("kcal") },
                            supportingText = {
                                Text(
                                    if (editedCalorieTarget == null) "Nhập số >= 800" else "Nhấn dấu check để lưu ngay"
                                )
                            }
                        )
                    }

                    Text(
                        text = "Gợi ý theo hồ sơ: $suggestedCalorieTarget kcal/ngày",
                        fontSize = 12.sp,
                        color = colorScheme.onPrimaryContainer
                    )
                }
            }

            Button(
                onClick = {
                    focusManager.clearFocus(force = true)
                    runtimeViewModel?.saveProfile(
                        UserProfile(
                            name = name,
                            age = age.toIntOrNull() ?: 22,
                            gender = gender,
                            weight = weight.toFloatOrNull() ?: 60f,
                            height = height.toFloatOrNull() ?: 165f,
                            goal = goal,
                            dailyCalorieTarget = selectedCalorieTarget
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary
                )
            ) {
                Text("Lưu hồ sơ")
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    ProfileScreen(
        navController = rememberNavController(),
        previewProfile = UserProfile(
            name = "Nguyen Van A",
            age = 24,
            gender = "Nam",
            weight = 67f,
            height = 172f,
            goal = "Duy trì cân nặng",
            dailyCalorieTarget = 2200
        )
    )
}

