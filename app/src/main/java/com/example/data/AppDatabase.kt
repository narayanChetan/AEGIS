package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TriggerWorkflow::class, SmartDevice::class, EmailRoutine::class, HealthTelemetry::class, ChatMessage::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun triggerWorkflowDao(): TriggerWorkflowDao
    abstract fun smartDeviceDao(): SmartDeviceDao
    abstract fun emailRoutineDao(): EmailRoutineDao
    abstract fun healthTelemetryDao(): HealthTelemetryDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aegis_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Populate default test items so user starts with a responsive Smart Home workspace
                        INSTANCE?.let { appDb ->
                            scope.launch(Dispatchers.IO) {
                                populateDefaultData(appDb)
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateDefaultData(db: AppDatabase) {
            // Default Smart Home devices
            val devices = listOf(
                SmartDevice(name = "Orb Reactor Core", type = "Light", state = "ON", location = "Server Room", internalIp = "192.168.1.5"),
                SmartDevice(name = "Lab High-Volt Lights", type = "Light", state = "OFF", location = "Living Room", internalIp = "192.168.1.17"),
                SmartDevice(name = "Main Vault Entry Lock", type = "Lock", state = "Locked", location = "Entrance", internalIp = "192.168.1.20"),
                SmartDevice(name = "Perimeter Defense Camera", type = "Camera", state = "ON", location = "Entrance", internalIp = "192.168.1.44"),
                SmartDevice(name = "Core Cooling System", type = "Thermostat", state = "68°F", location = "Server Room", internalIp = "192.168.1.10")
            )
            for (dev in devices) {
                db.smartDeviceDao().insertDevice(dev)
            }

            // Default automated workflows
            val workflows = listOf(
                TriggerWorkflow(
                    name = "Vanguard Security Override",
                    triggerType = "Voice",
                    triggerValue = "Deploy security protocol",
                    actionType = "SecurityScan",
                    actionValue = "Perception analysis active; Encrypt all sensitive parameters",
                    isActive = true,
                    isQuickAction = true
                ),
                TriggerWorkflow(
                    name = "Cardiac Stress Response",
                    triggerType = "Biometric",
                    triggerValue = "HR > 110",
                    actionType = "SmartHome",
                    actionValue = "Core Cooling to 65°F; Dim Reactor lights to Calm Green",
                    isActive = true,
                    isQuickAction = true
                ),
                TriggerWorkflow(
                    name = "Cyber Intel Daily Email",
                    triggerType = "Time",
                    triggerValue = "08:00",
                    actionType = "Gmail",
                    actionValue = "Send server logs to admin; Run penetration report",
                    isActive = true,
                    isQuickAction = false
                )
            )
            for (wf in workflows) {
                db.triggerWorkflowDao().insertWorkflow(wf)
            }

            // Default bio log
            db.healthTelemetryDao().insertTelemetry(
                HealthTelemetry(heartRate = 72, oxygenLevelByPercent = 98, stressLevel = "Normal", detectedEmotion = "Calm")
            )

            // Seed initial chatbot welcome message
            db.chatMessageDao().insertMessage(
                ChatMessage(sender = "aegis", text = "Aegis online. Vocal matrix initialized. Standing by.")
            )
        }
    }
}
