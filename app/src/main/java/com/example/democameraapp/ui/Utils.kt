package com.example.democameraapp.ui

import android.graphics.Bitmap
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.view.CameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun CameraTopBar(
    modifier: Modifier = Modifier,
    isFlash: Int,
    isTorch: Boolean,
    flashButtonClicked: () -> Unit,
    torchButtonClicked: () -> Unit,
){
    Row(
        modifier = modifier
            .height(60.dp)
            .fillMaxWidth()
            .background(Color.Black),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Icon(
            modifier = modifier
                .padding(start = 30.dp)
                .size(40.dp)
                .clickable { torchButtonClicked() },
            imageVector = Icons.Outlined.LightMode,
            contentDescription = null,
            tint = if ( isTorch ){ Color.Yellow } else{ Color.White },
        )
        Icon(
            modifier = modifier
                .padding(end = 30.dp)
                .size(40.dp)
                .clickable { flashButtonClicked() },
            imageVector = Icons.Outlined.FlashOn,
            contentDescription = null,
            tint = if ( isFlash == ImageCapture.FLASH_MODE_ON ){ Color.Yellow } else{ Color.White },
        )
    }
}

@Composable
fun CameraBottomBar(
    modifier: Modifier = Modifier,
    useCase: Int,
    isRecording: Boolean,
    previewPhotoUri: Uri? = null,
    previewVideoBitmap: Bitmap? = null,
    takePhoto: () -> Unit,
    captureVideo: () -> Unit,
    cameraSelectorButtonClicked: () -> Unit,
    imageCaptureButtonClicked: () -> Unit,
    videoCaptureButtonClicked: () -> Unit,
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .background(Color.Black),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        if ( useCase == CameraController.IMAGE_CAPTURE ){
            AsyncImage(
                modifier = modifier.size(130.dp),
                model = previewPhotoUri,
                contentDescription = null
            )
        }else{
            AsyncImage(
                modifier = modifier.size(130.dp),
                model = previewVideoBitmap,
                contentDescription = null
            )
        }
        Box(
            modifier = modifier
                .size(130.dp),
            contentAlignment = Alignment.Center
        ){
            Icon(
                modifier = modifier
                    .height(80.dp)
                    .width(80.dp)
                    .clickable {
                        if ( useCase == CameraController.IMAGE_CAPTURE){
                            takePhoto()
                        }else{
                            captureVideo()
                        }
                    },
                imageVector = Icons.Default.Camera,
                contentDescription = null,
                tint = if ( useCase == CameraController.VIDEO_CAPTURE && isRecording == true ){
                    Color.Red
                }else{
                    Color.White
                }
            )
        }
        Column(
            modifier = modifier
                .height(100.dp)
                .width(130.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            CameraUseCaseButton(
                modifier = modifier,
                useCase = useCase,
                imageCaptureButtonClicked = imageCaptureButtonClicked,
                videoCaptureButtonClicked = videoCaptureButtonClicked,
            )
            Icon(
                modifier = modifier
                    .padding( top = 15.dp )
                    .size(40.dp)
                    .clickable { cameraSelectorButtonClicked() },
                imageVector = Icons.Outlined.Cameraswitch,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Composable
fun CameraUseCaseButton(
    modifier: Modifier = Modifier,
    useCase: Int,
    imageCaptureButtonClicked: () -> Unit,
    videoCaptureButtonClicked: () -> Unit,
){
    Row(
        modifier = modifier.height(40.dp)
    ) {
        Box(
            modifier = modifier
                .width(60.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStartPercent = 30, bottomStartPercent = 30))
                .background(
                    if ( useCase == CameraController.IMAGE_CAPTURE ){
                        Color.Yellow
                    }else{
                        Color.White
                    }
                )
                .clickable(
                    enabled = useCase != CameraController.IMAGE_CAPTURE,
                    onClick = { imageCaptureButtonClicked() }
                ),
            contentAlignment = Alignment.Center
        ){
            Text(text = "写真")
        }
        VerticalDivider(
            modifier = modifier
                .fillMaxHeight(),
            thickness = 1.dp,
            color = Color.Black
        )
        Box(
            modifier = modifier
                .width(60.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topEndPercent = 30, bottomEndPercent = 30))
                .background(
                    if ( useCase == CameraController.VIDEO_CAPTURE ){
                        Color.Yellow
                    }else{
                        Color.White
                    }
                )
                .clickable(
                    enabled = useCase != CameraController.VIDEO_CAPTURE,
                    onClick = { videoCaptureButtonClicked() }
                ),
            contentAlignment = Alignment.Center
        ){
            Text(text = "動画")
        }
    }
}

@Preview
@Composable
fun Preview(){
    CameraTopBar(
        modifier = Modifier,
        isFlash = 0,
        isTorch = false,
        flashButtonClicked = {},
        torchButtonClicked = {}
    )
}