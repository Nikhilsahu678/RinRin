package com.rinrin.app.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rinrin.app.data.repository.ItemData
import com.rinrin.app.ui.viewmodels.AddItemViewModel
import com.google.firebase.auth.FirebaseAuth
import java.io.File

// Uri to File cache helper
fun uriToFile(context: Context, uri: Uri): File {
    val tempFile = File(context.cacheDir, "rinrin_img_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }
    return tempFile
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    viewModel: AddItemViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val isImageUploading by viewModel.isImageUploading.collectAsState()
    val uploadedImageUrl by viewModel.uploadedImageUrl.collectAsState()
    val isPublishing by viewModel.isPublishing.collectAsState()
    val publishSuccess by viewModel.publishSuccess.collectAsState()
    val vmError by viewModel.error.collectAsState()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tools") }
    var dailyRateStr by remember { mutableStateOf("") }
    var securityDepositStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var validationError by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val categories = listOf("Tools", "Kitchen", "Electronics", "Sports", "Party", "Camera", "Others")

    // Image Picker Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            viewModel.clearUploadUrl() // Reset previous upload on picking new one
            validationError = null
        }
    }

    LaunchedEffect(vmError) {
        if (vmError != null) {
            validationError = vmError
        }
    }

    LaunchedEffect(publishSuccess) {
        if (publishSuccess) {
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("List an Item", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card for image placeholder / selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { imagePickerLauncher.launch("image/*") }
                    .testTag("select_image_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected Image Preview",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Select Photo Icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Tap to Select Item Image",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                "High-quality photos get 3x more rentals",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Upload Button (if image local select but not uploaded)
            if (imageUri != null && uploadedImageUrl == null) {
                if (isImageUploading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Uploading image to Imgbb...")
                    }
                } else {
                    Button(
                        onClick = {
                            val uri = imageUri
                            if (uri != null) {
                                try {
                                    val file = uriToFile(context, uri)
                                    viewModel.uploadImage(file)
                                } catch (e: Exception) {
                                    validationError = "Error: " + (e.localizedMessage ?: "Image error")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("upload_image_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Upload")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Photo")
                    }
                }
            } else if (uploadedImageUrl != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0F2F1))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "✓ Photo Uploaded Successfully",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Form Title
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    validationError = null
                },
                label = { Text("Item Title (e.g. Cordless Drill)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("item_title_field"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Picker Chips
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState(), enabled = false),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            categories.take(4).forEach { cat ->
                                val isSelected = cat == selectedCategory
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            categories.drop(4).forEach { cat ->
                                val isSelected = cat == selectedCategory
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Rate
            OutlinedTextField(
                value = dailyRateStr,
                onValueChange = {
                    dailyRateStr = it
                    validationError = null
                },
                label = { Text("Daily Rate ($ / day)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_rate_field"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Security Deposit (optional)
            OutlinedTextField(
                value = securityDepositStr,
                onValueChange = {
                    securityDepositStr = it
                    validationError = null
                },
                label = { Text("Security Deposit ($ - Optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("security_deposit_field"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    validationError = null
                },
                label = { Text("Describe location, pick-up preferences & specifications") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("description_field"),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Validation Error text
            if (validationError != null) {
                Text(
                    text = validationError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("form_error_text")
                )
            }

            // Publish Button
            if (isPublishing) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        val dailyRate = dailyRateStr.toIntOrNull()
                        val securityDeposit = securityDepositStr.toIntOrNull()

                        if (title.isBlank()) {
                            validationError = "Title cannot be blank"
                        } else if (uploadedImageUrl == null) {
                            validationError = "You must select and upload an image"
                        } else if (dailyRate == null || dailyRate <= 0) {
                            validationError = "Daily rate must be a valid positive integer price"
                        } else {
                            validationError = null

                            val currentUser = FirebaseAuth.getInstance().currentUser
                            val ownerName = currentUser?.displayName?.ifEmpty { null }
                                ?: currentUser?.email?.substringBefore("@")
                                ?: "Neighbor"
                            val ownerId = currentUser?.uid ?: ""

                            val item = ItemData(
                                title = title,
                                category = selectedCategory,
                                dailyRate = dailyRate,
                                securityDeposit = securityDeposit,
                                description = description,
                                imageUrl = uploadedImageUrl,
                                ownerId = ownerId,
                                ownerName = ownerName,
                                createdAt = System.currentTimeMillis()
                            )

                            viewModel.publishItem(item)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("publish_button")
                ) {
                    Text(
                        text = "Publish Item",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    // Success Dialog on Success
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* Force choose Go to Home */ },
            title = { Text("Listing Published! 🌟") },
            text = { Text("Your peer item has been published successfully and is now live on the hyperlocal marketplace map!") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        viewModel.resetState()
                        onNavigateBack()
                    },
                    modifier = Modifier.testTag("success_confirm_button")
                ) {
                    Text("Go to Home")
                }
            }
        )
    }
}
