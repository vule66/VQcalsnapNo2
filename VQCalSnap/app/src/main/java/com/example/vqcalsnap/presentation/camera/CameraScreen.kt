package com.example.vqcalsnap.presentation.camera

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.vqcalsnap.presentation.navigation.BottomNavBar
import com.example.vqcalsnap.presentation.navigation.Screen
import java.io.File
import java.util.concurrent.Executor

@Composable
fun CameraScreen(
    navController: NavController,
    viewModel: CameraViewModel? = null
) {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    val runtimeViewModel = if (!inPreview) {
        viewModel ?: hiltViewModel<CameraViewModel>()
    } else {
        null
    }

    var hasCameraPermission by remember { mutableStateOf(false) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) { PreviewView(context) }
    val cameraProviderFuture = remember(context) { ProcessCameraProvider.getInstance(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val encodedPath = Uri.encode(it.toString())
            navController.navigate(Screen.Result.createRoute(encodedPath))
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = {
                    Column {
                        Text("Chụp món ăn", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Hướng camera vào món ăn",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(navController = navController, current = Screen.Camera)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (hasCameraPermission && !inPreview) {

                DisposableEffect(hasCameraPermission, lifecycleOwner) {
                    if (!hasCameraPermission || runtimeViewModel == null) {
                        onDispose { }
                    } else {
                        val executor = ContextCompat.getMainExecutor(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            try {
                                if (runtimeViewModel.shouldRebind(lifecycleOwner)) {
                                    cameraProvider.unbindAll()
                                    runtimeViewModel.previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        runtimeViewModel.previewUseCase,
                                        runtimeViewModel.imageCaptureUseCase
                                    )
                                    runtimeViewModel.markBound(lifecycleOwner)
                                } else {
                                    runtimeViewModel.previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                imageCapture = runtimeViewModel.imageCaptureUseCase
                            } catch (e: Exception) {
                                Log.e("CameraScreen", "Bind failed", e)
                            }
                        }, executor)

                        onDispose {
                            runtimeViewModel.previewUseCase.setSurfaceProvider(null)
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Khung ngắm
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .align(Alignment.Center)
                            .border(2.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Cần quyền truy cập camera")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Cấp quyền")
                        }
                    }
                }
            }

            // Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        (imageCapture ?: runtimeViewModel?.imageCaptureUseCase)?.let { capture ->
                            takePicture(
                                context = context,
                                imageCapture = capture,
                                executor = ContextCompat.getMainExecutor(context),
                                onSuccess = { uri ->
                                    val encodedPath = Uri.encode(uri.toString())
                                    navController.navigate(Screen.Result.createRoute(encodedPath))
                                },
                                onError = { Log.e("Camera", "Chụp thất bại", it) }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = hasCameraPermission
                ) {
                    Text("Chụp ảnh")
                }

                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Chọn từ thư viện")
                }

                Text(
                    text = "AI sẽ tự nhận diện món ăn và tính calo",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

private fun takePicture(
    context: Context,
    imageCapture: ImageCapture,
    executor: Executor,
    onSuccess: (Uri) -> Unit,
    onError: (Exception) -> Unit
) {
    val photoFile = File(
        context.cacheDir,
        "calsnap_${System.currentTimeMillis()}.jpg"
    )
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                onSuccess(Uri.fromFile(photoFile))
            }
            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}

@Preview(showBackground = true, widthDp = 420, heightDp = 900)
@Composable
private fun CameraScreenPreview() {
    CameraScreen(navController = rememberNavController())
}

