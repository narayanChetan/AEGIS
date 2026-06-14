package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "triggers_workflows")
data class TriggerWorkflow(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val triggerType: String, // e.g., "Voice", "Biometric", "Time", "Device"
    val triggerValue: String, // e.g., "Deploy security protocol", "HR > 100", "08:00"
    val actionType: String, // e.g., "SmartHome", "Gmail", "SecurityScan", "TTS"
    val actionValue: String, // e.g., "Lights OFF, Lock ON", "Alert: Heart stress detected", "Send status update"
    val isActive: Boolean = true,
    val isQuickAction: Boolean = false
)

@Entity(tableName = "smart_devices")
data class SmartDevice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "Light", "Camera", "Lock", "Thermostat", "Vacuum"
    val state: String, // "ON", "OFF", "Locked", "Unlocked", "72°F", "Charging"
    val location: String, // "Entrance", "Living Room", "Kitchen", "Server Room"
    val internalIp: String = "192.168.1.10",
    val isEncrypted: Boolean = true
)

@Entity(tableName = "email_routines")
data class EmailRoutine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipient: String,
    val subject: String,
    val body: String,
    val category: String, // "Automation", "Inquiry", "Alert", "Response"
    val status: String, // "DRAFT", "PENDING", "SENT", "FAILED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "health_telemetry")
data class HealthTelemetry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val heartRate: Int,
    val oxygenLevelByPercent: Int,
    val stressLevel: String, // "Normal", "Elevated", "Critical"
    val detectedEmotion: String // "Calm", "Analytical", "Empathetic", "Serious"
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "aegis"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

