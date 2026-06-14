package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TriggerWorkflowDao {
    @Query("SELECT * FROM triggers_workflows ORDER BY id DESC")
    fun getAllWorkflows(): Flow<List<TriggerWorkflow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkflow(workflow: TriggerWorkflow)

    @Update
    suspend fun updateWorkflow(workflow: TriggerWorkflow)

    @Delete
    suspend fun deleteWorkflow(workflow: TriggerWorkflow)
}

@Dao
interface SmartDeviceDao {
    @Query("SELECT * FROM smart_devices ORDER BY name ASC")
    fun getAllDevices(): Flow<List<SmartDevice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: SmartDevice)

    @Update
    suspend fun updateDevice(device: SmartDevice)

    @Delete
    suspend fun deleteDevice(device: SmartDevice)
}

@Dao
interface EmailRoutineDao {
    @Query("SELECT * FROM email_routines ORDER BY createdAt DESC")
    fun getAllEmails(): Flow<List<EmailRoutine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmail(email: EmailRoutine)

    @Update
    suspend fun updateEmail(email: EmailRoutine)

    @Delete
    suspend fun deleteEmail(email: EmailRoutine)
}

@Dao
interface HealthTelemetryDao {
    @Query("SELECT * FROM health_telemetry ORDER BY timestamp DESC LIMIT 50")
    fun getRecentTelemetry(): Flow<List<HealthTelemetry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelemetry(telemetry: HealthTelemetry)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

