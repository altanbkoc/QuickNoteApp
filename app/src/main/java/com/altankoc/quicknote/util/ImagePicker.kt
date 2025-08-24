package com.altankoc.quicknote.util

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class ImagePicker {
    companion object {
        fun copyImageToInternalStorage(context: Context, uri: Uri): String? {
            return try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {

                    val fileName = "note_image_${UUID.randomUUID()}.jpg"
                    val file = File(context.filesDir, fileName)


                    val outputStream = FileOutputStream(file)
                    inputStream.copyTo(outputStream)

                    inputStream.close()
                    outputStream.close()

                    file.absolutePath
                } else null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }


        fun deleteImageFile(imagePath: String): Boolean {
            return try {
                val file = File(imagePath)
                if (file.exists()) {
                    file.delete()
                } else false
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }


        fun imageExists(imagePath: String?): Boolean {
            return if (imagePath.isNullOrBlank()) {
                false
            } else {
                File(imagePath).exists()
            }
        }


        fun getImageSizeInMB(imagePath: String): Double {
            return try {
                val file = File(imagePath)
                if (file.exists()) {
                    file.length() / (1024.0 * 1024.0)
                } else 0.0
            } catch (e: Exception) {
                0.0
            }
        }
    }
}

@Composable
fun rememberImagePickerLauncher(
    onImageSelected: (String?) -> Unit
): ImagePickerState {
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val imagePath = ImagePicker.copyImageToInternalStorage(context, uri)
            onImageSelected(imagePath)
        } else {
            onImageSelected(null)
        }
    }

    val legacyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val imagePath = ImagePicker.copyImageToInternalStorage(context, uri)
            onImageSelected(imagePath)
        } else {
            onImageSelected(null)
        }
    }

    return ImagePickerState(
        pickImage = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            } else {
                legacyPickerLauncher.launch("image/*")
            }
        }
    )
}

data class ImagePickerState(
    val pickImage: () -> Unit
)

fun getImageErrorMessage(error: ImageError): String {
    return when (error) {
        ImageError.PERMISSION_DENIED -> "Gallery permission is required to add images"
        ImageError.PICK_CANCELLED -> "Image selection was cancelled"
        ImageError.COPY_FAILED -> "Failed to save image. Please try again"
        ImageError.FILE_TOO_LARGE -> "Image file is too large. Please choose a smaller image"
        ImageError.UNSUPPORTED_FORMAT -> "Unsupported image format"
        ImageError.UNKNOWN -> "Unknown error occurred"
    }
}

enum class ImageError {
    PERMISSION_DENIED,
    PICK_CANCELLED,
    COPY_FAILED,
    FILE_TOO_LARGE,
    UNSUPPORTED_FORMAT,
    UNKNOWN
}