package com.ethran.notable.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.api.services.drive.Drive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.google.api.services.drive.model.File as DriveFile


@Composable
fun DriveFilePicker(
    driveService: Drive?,
    onFileSelected: (String, String) -> Unit, // ID, Name
    onCancel: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var files by remember { mutableStateOf<List<DriveFile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(driveService) {
        if (driveService == null) {
            errorMessage = "Google Drive service not available. Please sign in."
            return@LaunchedEffect
        }
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    driveService.files().list()
                        .setQ("mimeType='text/plain' or mimeType='application/octet-stream'") // Filter for text files or general binary if specific type unknown
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, mimeType)")
                        .execute()
                }
                files = result.files ?: emptyList()
            } catch (e: Exception) {
                errorMessage = "Error fetching files: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select a Text File") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(errorMessage!!, color = MaterialTheme.colors.error)
                }
            } else if (files.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No text files found on your Google Drive.")
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(files) { file ->
                        ListItem(
                            text = { Text(file.name ?: "Unnamed file") },
                            secondaryText = { Text("MIME type: ${file.mimeType ?: "Unknown"}") },
                            modifier = Modifier.clickable {
                                onFileSelected(file.id, file.name)
                            }
                        )
                        Divider()
                    }
                }
            }
            Button(onClick = onCancel, modifier = Modifier.align(Alignment.End)) {
                Text("Cancel")
            }
        }
    }
}
