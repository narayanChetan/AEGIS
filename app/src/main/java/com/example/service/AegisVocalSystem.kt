package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

enum class AegisState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

enum class UserEmotion {
    CALM,
    STRESSED,
    ANALYTICAL,
    EXCITED,
    CONFUSED
}

/**
 * Offline-capable High-Performance Vocal System coordinating voice capture (STT)
 * and voice synthesis (TTS) directly through local Android libraries. High privacy,
 * zero latency.
 */
class AegisVocalSystem(
    private val context: Context,
    private val scope: kotlinx.coroutines.CoroutineScope
) : TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null

    private val _systemState = MutableStateFlow(AegisState.IDLE)
    val systemState: StateFlow<AegisState> = _systemState

    private val _voiceVolumeLevel = MutableStateFlow(0f)
    val voiceVolumeLevel: StateFlow<Float> = _voiceVolumeLevel // represents dB level for user voice morphing animations
    private val _textVolume = MutableStateFlow(0f)
    val textVolume: StateFlow<Float> = _textVolume

    private val _lastTranscript = MutableStateFlow("")
    val lastTranscript: StateFlow<String> = _lastTranscript

    private val _detectedEmotion = MutableStateFlow(UserEmotion.CALM)
    val detectedEmotion: StateFlow<UserEmotion> = _detectedEmotion

    private var onResultCallback: ((String) -> Unit)? = null
    private var isTtsReady = false

    init {
        initializeTts()
    }

    private fun initializeTts() {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                val result = tts.setLanguage(Locale.US)
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true
                    // Configure futuristic voice accent params
                    tts.setPitch(0.95f) // Deep barytone-ish cybernetic voice (like Jarvis)
                    tts.setSpeechRate(1.05f) // crisp low-latency speech rate

                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _systemState.value = AegisState.SPEAKING
                        }

                        override fun onDone(utteranceId: String?) {
                            _systemState.value = AegisState.IDLE
                            _textVolume.value = 0f
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _systemState.value = AegisState.ERROR
                        }

                        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                            super.onRangeStart(utteranceId, start, end, frame)
                            // Simulate talking facial resonance volume
                            _textVolume.value = (3f + Math.random().toFloat() * 12f)
                        }
                    })
                }
            }
        }
    }

    fun setCallback(callback: (String) -> Unit) {
        onResultCallback = callback
    }

    /**
     * Synthesizes robotic speech vocally.
     */
    fun speak(text: String) {
        if (!isTtsReady) return
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AEGIS_OUT_UTTERANCE")
    }

    /**
     * Starts listening via microphone
     */
    fun startListening() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            _systemState.value = AegisState.LISTENING
            _lastTranscript.value = "Active Voice Stream Connected..."

            val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            scope.launchInMainThread {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _lastTranscript.value = "Listening..."
                        }

                        override fun onBeginningOfSpeech() {}

                        override fun onRmsChanged(rmsdB: Float) {
                            // Extract audio dB levels and pipe directly into the Canvas visualizer
                            // Normalizer map to 0.0 - 1.0 range
                            val norm = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                            _voiceVolumeLevel.value = norm * 15f
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            _systemState.value = AegisState.THINKING
                            _voiceVolumeLevel.value = 0f
                        }

                        override fun onError(error: Int) {
                            val msg = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "Audio record failure"
                                SpeechRecognizer.ERROR_CLIENT -> "Client connection block"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissions disallowed"
                                SpeechRecognizer.ERROR_NETWORK -> "Network channel error"
                                SpeechRecognizer.ERROR_NO_MATCH -> "No audible match detected"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Vocal silence timeout"
                                else -> "Microphone channel reset"
                            }
                            _lastTranscript.value = msg
                            _systemState.value = AegisState.IDLE
                            _voiceVolumeLevel.value = 0f
                        }

                        override fun onResults(results: Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                val transcript = matches[0]
                                _lastTranscript.value = transcript
                                detectEmotion(transcript)
                                onResultCallback?.invoke(transcript)
                            } else {
                                _systemState.value = AegisState.IDLE
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (!matches.isNullOrEmpty()) {
                                _lastTranscript.value = matches[0]
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                    startListening(recognizerIntent)
                }
            }
        } else {
            _lastTranscript.value = "Local offline Voice Recognition is not supported."
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _systemState.value = AegisState.IDLE
        _textVolume.value = 0f
    }

    /**
     * Offline Real-Time Emotion Recognition processor. Evaluates token triggers to detect mood.
     */
    private fun detectEmotion(text: String) {
        val lower = text.lowercase()
        val emotion = when {
            lower.contains("help") || lower.contains("stress") || lower.contains("hurt") || lower.contains("panic") || lower.contains("emergency") -> UserEmotion.STRESSED
            lower.contains("why") || lower.contains("explain") || lower.contains("science") || lower.contains("how does") || lower.contains("calculate") -> UserEmotion.ANALYTICAL
            lower.contains("wow") || lower.contains("awesome") || lower.contains("happy") || lower.contains("great") || lower.contains("let's go") || lower.contains("cool") -> UserEmotion.EXCITED
            lower.contains("what is") || lower.contains("i don't know") || lower.contains("where") || lower.contains("confused") -> UserEmotion.CONFUSED
            else -> UserEmotion.CALM
        }
        _detectedEmotion.value = emotion
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        speechRecognizer?.destroy()
    }

    // Helper dispatcher utility
    private fun kotlinx.coroutines.CoroutineScope.launchInMainThread(block: suspend kotlinx.coroutines.CoroutineScope.() -> Unit) {
        this.launch(kotlinx.coroutines.Dispatchers.Main) { block() }
    }
}
