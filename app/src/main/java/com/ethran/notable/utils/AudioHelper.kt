package com.ethran.notable.utils

import android.content.Context
import android.util.Log
import com.google.firebase.vertexai.FirebaseVertexAI
import com.google.firebase.vertexai.type.generationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AudioHelper {

    private val TAG = "AudioHelper"

    fun startSpellingTestTutor(context: Context) {
        val prompt = "Hello! I'm your spelling test tutor. Are you ready to start practicing your spelling words?"

        try {
            val generativeModel = FirebaseVertexAI.getInstance()
                .generativeModel(
                    modelName = "gemini-1.5-flash",
                    // For demonstration purposes, setting a low temperature. Adjust as needed.
                    generationConfig = generationConfig {
                        temperature = 0.1f
                    }
                )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val chat = generativeModel.startChat()
                    val response = chat.sendMessage(prompt)
                    Log.d(TAG, "Spelling test tutor started successfully. Response: ${response.text}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during Firebase AI interaction: ${e.message}")
                    // Handle exceptions appropriately
                }
            }
            Log.d(TAG, "Firebase AI request initiated for prompt: $prompt")

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase AI or starting coroutine: ${e.message}")
            // Handle exceptions appropriately
        }
    }
}
