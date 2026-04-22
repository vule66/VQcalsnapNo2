package com.example.vqcalsnap.presentation.result

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vqcalsnap.data.local.MealEntity
import com.example.vqcalsnap.data.repository.MealRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

data class FoodResult(
    val name: String = "",
    val calories: Int = 0,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val portionSize: Float = 100f,
    val portionUnit: String = "g"
)

sealed class ResultUiState {
    object Loading : ResultUiState()
    data class Success(val foods: List<FoodResult>) : ResultUiState()
    data class Error(val message: String) : ResultUiState()
}
@HiltViewModel
class ResultViewModel @Inject constructor(
    private val mealRepository: MealRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResultUiState>(ResultUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val apiKeys = listOf(
        "AIzaSyB6fceuAdDNwFVaRbSKYnmqQhKeN0-Dyow",
        "AIzaSyCJMsKxoTw6opFZsewJE1ScnME6-ByV33Y",
        "AIzaSyCO2eH8xVK6H-oLvX_gxG9lsM_EDLJO21c"
    )
    private var currentKeyIndex = 0

    private fun getModel() = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKeys[currentKeyIndex],
        generationConfig = generationConfig {
            responseMimeType = "application/json"
        }
    )

    fun analyzeFood(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = ResultUiState.Loading
            try {
                val bitmap = uriToBitmap(context, imageUri)
                    ?: throw Exception("Không đọc được ảnh")
                val maxSize = 512
                val ratio = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
                val newWidth = (bitmap.width * ratio).toInt()
                val newHeight = (bitmap.height * ratio).toInt()
                val resizedBitmap = android.graphics.Bitmap
                    .createScaledBitmap(bitmap, newWidth, newHeight, true)

                val prompt = """
                    Phân tích món ăn hoặc nguyên liệu trong ảnh và trả về JSON array:
                    [
                        {
                            "name": "tên món hoặc nguyên liệu tiếng Việt",
                            "calories": integer,
                            "protein": float,
                            "carbs": float,
                            "fat": float,
                            "portionSize": float,
                            "portionUnit": "g"
                        }
                    ]
                    Nếu có nhiều món thì trả về nhiều object trong array.
                    Nếu không phải đồ ăn, trả về array rỗng [].
                    Chỉ trả về JSON array, không có markdown.
                """.trimIndent()
                val response = getModel().generateContent(
                    content {
                        image(resizedBitmap)
                        text(prompt)
                    }
                )
                val raw = response.text
                    ?.replace("```json", "")
                    ?.replace("```", "")
                    ?.trim()
                    ?: throw Exception("AI không trả về dữ liệu")

                android.util.Log.d("CalSnap", "Response: $raw")

                val jsonArray = org.json.JSONArray(raw)

                if (jsonArray.length() == 0) {
                    withContext(Dispatchers.Main) {
                        _uiState.value = ResultUiState.Error("Không nhận diện được món ăn")
                    }
                    return@launch
                }

                val foods = mutableListOf<FoodResult>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    foods.add(
                        FoodResult(
                            name = obj.getString("name"),
                            calories = obj.getInt("calories"),
                            protein = obj.optDouble("protein", 0.0).toFloat(),
                            carbs = obj.optDouble("carbs", 0.0).toFloat(),
                            fat = obj.optDouble("fat", 0.0).toFloat(),
                            portionSize = obj.optDouble("portionSize", 100.0).toFloat(),
                            portionUnit = obj.optString("portionUnit", "g")
                        )
                    )
                }

                withContext(Dispatchers.Main) {
                    _uiState.value = ResultUiState.Success(foods)
                }
                } catch (e: Exception) {
                _uiState.value = ResultUiState.Error("Lỗi: ${e.localizedMessage}")
            }
        }
    }

    fun saveMeal(foods: List<FoodResult>) {
        viewModelScope.launch {
            foods.forEach { food ->
                mealRepository.insertMeal(
                    MealEntity(
                        name = food.name,
                        calories = food.calories,
                        protein = food.protein,
                        carbs = food.carbs,
                        fat = food.fat,
                        portionSize = food.portionSize,
                        portionUnit = food.portionUnit
                    )
                )
            }
        }
    }
}

private fun uriToBitmap(context: Context, uri: Uri): android.graphics.Bitmap? {
    return try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
            android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
            }
        } else {
            @Suppress("DEPRECATION")
            android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        null
    }
}