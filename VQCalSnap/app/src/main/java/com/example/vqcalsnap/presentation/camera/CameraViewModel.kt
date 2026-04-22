package com.example.vqcalsnap.presentation.camera

import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    val previewUseCase: Preview by lazy {
        Preview.Builder().build()
    }

    val imageCaptureUseCase: ImageCapture by lazy {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    private var boundLifecycleHash: Int? = null

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri = _imageUri.asStateFlow()

    fun setImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    fun shouldRebind(lifecycleOwner: LifecycleOwner): Boolean {
        return boundLifecycleHash != lifecycleOwner.hashCode()
    }

    fun markBound(lifecycleOwner: LifecycleOwner) {
        boundLifecycleHash = lifecycleOwner.hashCode()
    }
}