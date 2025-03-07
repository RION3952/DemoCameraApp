package com.example.democameraapp.ui

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaActionSound
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.CameraController
import androidx.camera.view.video.AudioConfig
import androidx.core.app.ActivityCompat
import androidx.core.util.Consumer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

class CameraViewModel : ViewModel() {
    private val _cameraState = MutableStateFlow(CameraState())
    val cameraState: StateFlow<CameraState> =  _cameraState.asStateFlow()
    val mediaActionSound = MediaActionSound()
    private var recording: Recording? = null

    fun changeCameraOption( optionName: String, ){
        when(optionName){
            "flash" -> {
                _cameraState.update { cameraState ->
                    cameraState.copy(
                        isFlash = if(cameraState.isFlash == ImageCapture.FLASH_MODE_ON){
                            ImageCapture.FLASH_MODE_OFF
                        }else{
                            ImageCapture.FLASH_MODE_ON
                        },
                    )
                }
            }
            "torch" -> {
                _cameraState.update { cameraState ->
                    cameraState.copy(
                        isTorch = cameraState.isTorch != true,
                    )
                }
            }
            "useCase" -> {
                _cameraState.update { cameraState ->
                    cameraState.copy(
                        useCase = if(cameraState.useCase == CameraController.IMAGE_CAPTURE){
                            CameraController.VIDEO_CAPTURE
                        }else{
                            CameraController.IMAGE_CAPTURE
                        },
                    )
                }
            }
            "useCaseCamera" -> {
                _cameraState.update { cameraState ->
                    cameraState.copy(
                        useCase = CameraController.IMAGE_CAPTURE
                    )
                }
            }
            "useCaseVideo" -> {
                _cameraState.update { cameraState ->
                    cameraState.copy(
                        useCase = CameraController.VIDEO_CAPTURE
                    )
                }
            }
            "cameraSelector" -> {
                _cameraState.update { cameraState ->
                    cameraState.copy(
                        cameraSelector = if(cameraState.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA){
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        }else{
                            CameraSelector.DEFAULT_BACK_CAMERA
                        },
                    )
                }
            }
            "topOption" -> {
                _cameraState.update { cameraState ->
                    cameraState.copy(
                        isTopOption = !cameraState.isTopOption
                    )
                }
            }
            else -> {}
        }
    }

    fun takePhoto(
        context: Context,
        cameraController: CameraController,
        executor: Executor,
    ) {

        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.JAPAN)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DemoCameraApp")
            }
        }
        //Android10~対応の為
        val saveCollection = if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT) {
            // データ書き込みの場合は MediaStore.VOLUME_EXTERNAL_PRIMARY が適切
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        // キャプチャした画像を保存する為の出力オプション
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                saveCollection,
                contentValues
            )
            .build()

        // キャプチャの実行
        cameraController.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                // 結果はImageCapture.OnImageSavedCallbackでコールバックされる
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    mediaActionSound.play(MediaActionSound.SHUTTER_CLICK)
                    _cameraState.update { cameraState ->
                        cameraState.copy(
                            previewPhotoUri = output.savedUri
                        )
                    }
                }
                override fun onError(e: ImageCaptureException) {
                    Log.e("DemoCameraApp", "Photo capture failed: ${e.message}",e)
                }
            }
        )
    }

    fun captureVideo(
        context: Context,
        cameraController: CameraController,
        executor: Executor,
    ){
        val curRecording = recording
        if (curRecording != null) {
            curRecording.stop()
            recording = null
            return
        }
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.JAPAN)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/LocaCamera-Video")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        val audioConfig = if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            AudioConfig.AUDIO_DISABLED
        }else{
            AudioConfig.create(true)
        }

        val listener = Consumer<VideoRecordEvent>{ event->
            when(event){
                is VideoRecordEvent.Start -> {
                    // 録画開始時の処理
                    mediaActionSound.play(MediaActionSound.START_VIDEO_RECORDING)
                    _cameraState.update { cameraState ->
                        cameraState.copy(
                            isRecording = true
                        )
                    }
                }
                is VideoRecordEvent.Finalize -> {
                    mediaActionSound.play(MediaActionSound.STOP_VIDEO_RECORDING)
                    _cameraState.update { cameraState ->
                        cameraState.copy(
                            isRecording = false
                        )
                    }

                    // 録画終了時の処理
                    if (!event.hasError()){
                        Log.d("OK", "Video capture succeeded: " +
                                "${event.outputResults.outputUri}")
                        _cameraState.update { cameraState ->
                            cameraState.copy(
                                previewVideoBitmap = getVideoFrame(context = context, videoUri = event.outputResults.outputUri)
                            )
                        }
                    }else{
                        recording?.close()
                        recording = null
                    }
                }
            }
        }
        recording = cameraController.startRecording(
            mediaStoreOutputOptions,
            audioConfig,
            executor,
            listener
        )
    }

    fun getVideoFrame(context: Context, videoUri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, videoUri)
            retriever.getFrameAtTime(0) // 0ミリ秒で最初のフレームを取得
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            retriever.release()
        }
    }
}

data class CameraState(
    val cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    val isFlash: Int = ImageCapture.FLASH_MODE_OFF,
    val isRecording: Boolean = false,
    val isTorch: Boolean = false,
    val useCase: Int = CameraController.IMAGE_CAPTURE,
    val isTopOption: Boolean = false,
    val previewPhotoUri: Uri? = null,
    val previewVideoBitmap: Bitmap? = null,
)