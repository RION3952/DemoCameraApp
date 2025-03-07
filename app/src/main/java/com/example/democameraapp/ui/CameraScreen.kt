package com.example.democameraapp.ui

import android.os.Build
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = viewModel()
){

    val cameraUiState by viewModel.cameraState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val cameraController = remember {
        LifecycleCameraController(context).apply {
            isTapToFocusEnabled = true
            bindToLifecycle(lifecycleOwner)
        }
    }
    cameraController.setEnabledUseCases(cameraUiState.useCase)
    cameraController.cameraSelector = cameraUiState.cameraSelector
    cameraController.imageCaptureFlashMode = cameraUiState.isFlash
    cameraController.enableTorch(cameraUiState.isTorch)
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    //権限関係
    val permissions = if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
        listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.RECORD_AUDIO,
        )
    }else{
        listOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
        )
    }
    val permissionState = rememberMultiplePermissionsState(permissions = permissions)

    LaunchedEffect(Unit) {
        if(!permissionState.allPermissionsGranted){
            permissionState.launchMultiplePermissionRequest()
        }
    }

    DisposableEffect(cameraExecutor) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    if (permissionState.allPermissionsGranted){
        Scaffold(
            topBar = {
                CameraTopBar(
                    modifier = modifier,
                    isFlash = cameraUiState.isFlash,
                    isTorch = cameraUiState.isTorch,
                    flashButtonClicked = { viewModel.changeCameraOption("flash") },
                    torchButtonClicked = { viewModel.changeCameraOption("torch") }
                )
            },
            bottomBar = {
                CameraBottomBar(
                    modifier = modifier,
                    useCase = cameraUiState.useCase,
                    isRecording = cameraUiState.isRecording,
                    previewPhotoUri = cameraUiState.previewPhotoUri,
                    previewVideoBitmap = cameraUiState.previewVideoBitmap,
                    takePhoto = {
                        viewModel.takePhoto(
                            context = context,
                            cameraController = cameraController,
                            executor = cameraExecutor
                        )
                    },
                    captureVideo = {
                        viewModel.captureVideo(
                            context = context,
                            cameraController = cameraController,
                            executor = cameraExecutor
                        )
                    },
                    cameraSelectorButtonClicked = { viewModel.changeCameraOption("cameraSelector") },
                    imageCaptureButtonClicked = { viewModel.changeCameraOption("useCaseCamera") },
                    videoCaptureButtonClicked = { viewModel.changeCameraOption("useCaseVideo") }
                )
            }
        ){
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                AndroidView(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
                        .clipToBounds()
                    ,
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            scaleType = PreviewView.ScaleType.FILL_START
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            controller = cameraController
                        }
                    },
                    onRelease = {
                        cameraController.unbind()
                    }
                )
            }
        }
    }else{
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ){
            Button(
                modifier = modifier,
                onClick = { permissionState.launchMultiplePermissionRequest() }
            ) {
                Text("権限を許可する")
            }
        }
    }
}