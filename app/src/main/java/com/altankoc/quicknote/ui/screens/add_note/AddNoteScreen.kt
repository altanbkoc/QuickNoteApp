package com.altankoc.quicknote.ui.screens.add_note

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.altankoc.quicknote.R
import com.altankoc.quicknote.util.ImagePicker
import com.altankoc.quicknote.util.rememberImagePermissionState
import com.altankoc.quicknote.util.rememberImagePickerLauncher
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    viewModel: AddNoteViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    var showPermissionSnackbar by remember { mutableStateOf(false) }

    val imagePermissionState = rememberImagePermissionState { isGranted ->
        if (!isGranted) {
            showPermissionSnackbar = true
        }
    }

    val imagePickerLauncher = rememberImagePickerLauncher { imagePath ->
        viewModel.onEvent(AddNoteEvent.ImagePathChanged(imagePath))
    }

    LaunchedEffect(state.isNoteSaved) {
        if (state.isNoteSaved) {
            onNavigateBack()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.add_note_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.onEvent(AddNoteEvent.SaveNote)
                        },
                        enabled = state.canSave() && !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.save_note),
                                tint = if (state.canSave()) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ImagePickerSection(
                    imagePath = state.imagePath,
                    onImageClick = {
                        if (imagePermissionState.hasPermission) {
                            imagePickerLauncher.pickImage()
                        } else {
                            imagePermissionState.requestPermission()
                        }
                    },
                    onRemoveImage = {
                        state.imagePath?.let { path ->
                            ImagePicker.deleteImageFile(path)
                        }
                        viewModel.onEvent(AddNoteEvent.ImagePathChanged(null))
                    }
                )

                NoteTextField(
                    value = state.title,
                    onValueChange = { viewModel.onEvent(AddNoteEvent.TitleChanged(it)) },
                    placeholder = stringResource(R.string.title_placeholder),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true
                )

                NoteTextField(
                    value = state.subtitle,
                    onValueChange = { viewModel.onEvent(AddNoteEvent.SubtitleChanged(it)) },
                    placeholder = stringResource(R.string.subtitle_placeholder),
                    textStyle = MaterialTheme.typography.titleMedium,
                    singleLine = true
                )

                NoteTextField(
                    value = state.description,
                    onValueChange = { viewModel.onEvent(AddNoteEvent.DescriptionChanged(it)) },
                    placeholder = stringResource(R.string.description_placeholder),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = false,
                    modifier = Modifier.weight(1f)
                )

                state.error?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.onEvent(AddNoteEvent.SaveNote)
                    },
                    enabled = state.canSave() && !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isSaving) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.saving))
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.save_note),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (showPermissionSnackbar) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(
                        onClick = {
                            imagePermissionState.requestPermission()
                            showPermissionSnackbar = false
                        }
                    ) {
                        Text(stringResource(R.string.grant_permission))
                    }
                },
                dismissAction = {
                    TextButton(
                        onClick = { showPermissionSnackbar = false }
                    ) {
                        Text(stringResource(R.string.dismiss))
                    }
                }
            ) {
                Text(stringResource(R.string.permission_snackbar_message))
            }
        }
    }

    if (imagePermissionState.showRationale) {
        AlertDialog(
            onDismissRequest = imagePermissionState.onRationaleDismissed,
            title = {
                Text(
                    text = stringResource(R.string.gallery_permission_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.gallery_permission_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = imagePermissionState.onRationaleDismissed) {
                    Text(stringResource(R.string.grant_permission))
                }
            },
            dismissButton = {
                TextButton(onClick = { imagePermissionState.onRationaleDismissed }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    LaunchedEffect(state.title, state.subtitle, state.description) {
        if (state.error != null) {
            viewModel.onEvent(AddNoteEvent.ClearError)
        }
    }
}

@Composable
fun ImagePickerSection(
    imagePath: String?,
    onImageClick: () -> Unit,
    onRemoveImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        if (!imagePath.isNullOrBlank() && File(imagePath).exists()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                AsyncImage(
                    model = File(imagePath),
                    contentDescription = stringResource(R.string.selected_image),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = onRemoveImage,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.remove_image),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clickable { onImageClick() }
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = stringResource(R.string.add_image),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.add_image),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.tap_to_select_gallery),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textStyle: androidx.compose.ui.text.TextStyle,
    singleLine: Boolean,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = textStyle,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        },
        textStyle = textStyle.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary
        ),
        modifier = modifier.fillMaxWidth()
    )
}