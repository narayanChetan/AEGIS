package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.service.AegisState
import com.example.service.UserEmotion
import com.example.ui.AegisViewModel
import com.example.ui.face.NeuralFaceRenderer

// Futuristic Space Obsidian Color Palette
val ObsidianBg = Color(0xFF040408)
val GlassSurface = Color(0x0DFFFFFF)
val GlassSurfaceBorder = Color(0x1AFFFFFF)
val NeonCyan = Color(0xFF00F0FF)
val NeonPurple = Color(0xFFA855F7)
val NeonAmber = Color(0xFFFBBF24)
val NeonGreen = Color(0xFF10B981)
val MutedSlate = Color(0xFF94A3B8)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: AegisViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) }
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp > 600

    val systemState by viewModel.vocalSystemState.collectAsState()
    val terminalText by viewModel.lastTerminalTranscript.collectAsState()
    val rawVolume by viewModel.voiceVolume.collectAsState()
    val ttsVolume by viewModel.robotTalkVolume.collectAsState()
    val detectedEmotion by viewModel.detectedEmotion.collectAsState()
    val activeModelMode by viewModel.selectedModelMode.collectAsState()

    var showPersistentChat by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (!isTablet) {
                NavigationBar(
                    containerColor = ObsidianBg,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    val tabs = listOf(
                        Triple("AURA", Icons.Filled.Language, "core_tag"),
                        Triple("HOME", Icons.Filled.Home, "home_tag"),
                        Triple("EMAILS", Icons.Filled.Email, "emails_tag"),
                        Triple("VITALITY", Icons.Filled.HealthAndSafety, "health_tag"),
                        Triple("LOGIC", Icons.Filled.Settings, "logic_tag")
                    )
                    tabs.forEachIndexed { index, (label, icon, tag) ->
                        NavigationBarItem(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            icon = { Icon(icon, contentDescription = label, tint = if (activeTab == index) NeonCyan else MutedSlate) },
                            label = { Text(label, color = if (activeTab == index) NeonCyan else MutedSlate, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = GlassSurface
                            ),
                            modifier = Modifier.testTag(tag)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showPersistentChat = !showPersistentChat },
                containerColor = if (showPersistentChat) NeonPurple else NeonCyan,
                contentColor = ObsidianBg,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = if (isTablet) 0.dp else 16.dp)
                    .testTag("floating_chat_toggle_button")
            ) {
                Icon(
                    imageVector = if (showPersistentChat) Icons.Filled.Close else Icons.Filled.Chat,
                    contentDescription = "Toggle Persistent Chat Panel",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = ObsidianBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxSize()
            ) {
            // Navigation Rail for tablets/desktop viewport sizes
            if (isTablet) {
                NavigationRail(
                    containerColor = ObsidianBg,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    val railTabs = listOf(
                        "AURA Core" to Icons.Filled.Language,
                        "Smart Home" to Icons.Filled.Home,
                        "Mass Emails" to Icons.Filled.Email,
                        "Biometrics" to Icons.Filled.HealthAndSafety,
                        "Custom Logic" to Icons.Filled.Settings
                    )
                    railTabs.forEachIndexed { index, (label, icon) ->
                        NavigationRailItem(
                            selected = activeTab == index,
                            onClick = { activeTab = index },
                            icon = { Icon(icon, contentDescription = label, tint = if (activeTab == index) NeonCyan else MutedSlate) },
                            label = { Text(label, color = if (activeTab == index) NeonCyan else MutedSlate, fontSize = 11.sp) },
                            colors = NavigationRailItemDefaults.colors(
                                indicatorColor = GlassSurface
                            )
                        )
                    }
                }
            }

            // Main Tab View Router
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionsToolbar(viewModel)
                
                val isConsoleVisible by viewModel.isSystemConsoleVisible.collectAsState()
                
                if (isConsoleVisible) {
                    SystemConsolePanel(
                        viewModel = viewModel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.42f)
                    )
                }
                
                Box(
                    modifier = Modifier.weight(if (isConsoleVisible) 0.58f else 1f)
                ) {
                    when (activeTab) {
                        0 -> AuraCoreTab(viewModel, systemState, terminalText, rawVolume, ttsVolume, detectedEmotion, activeModelMode)
                        1 -> SmartHomeTab(viewModel)
                        2 -> GmailTab(viewModel)
                        3 -> VitalityAndScannerTab(viewModel)
                        4 -> WorkflowsBuilderTab(viewModel)
                    }
                }
            }
        }

        // Beautiful slide-out panel of Chat Matrix for persistent overlay!
        androidx.compose.animation.AnimatedVisibility(
            visible = showPersistentChat,
            enter = androidx.compose.animation.slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow)
            ) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow)
            ) + androidx.compose.animation.fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .width(if (isTablet) 420.dp else 360.dp)
                .fillMaxWidth(if (isTablet) 0.45f else 1f)
                .background(ObsidianBg)
        ) {
            PersistentAegisChatDrawer(
                viewModel = viewModel,
                onClose = { showPersistentChat = false }
            )
        }
    }
}
}

// ================= TAB 0: AURA COGNITIVE SPEECH CENTER =================

@Composable
fun AuraCoreTab(
    viewModel: AegisViewModel,
    state: AegisState,
    terminalText: String,
    micVolume: Float,
    speakerVolume: Float,
    emotion: UserEmotion,
    modelMode: String
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Holographic App Header
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "AEGIS VOICE INTEGRATION",
            fontSize = 18.sp,
            fontFamily = FontFamily.Monospace,
            color = NeonCyan,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("app_title_label")
        )

        // Model selector buttons (Dynamic AI Routing)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(GlassSurface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val modes = listOf(
                "LOW_LATENCY" to "Flash-Lite",
                "THINKING" to "Thinking-Pro",
                "SEARCH" to "Grounded-Search"
            )
            modes.forEach { (key, display) ->
                val isSelected = modelMode == key
                Button(
                    onClick = { viewModel.selectModelMode(key) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) NeonCyan else Color.Transparent,
                        contentColor = if (isSelected) ObsidianBg else MutedSlate
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f).testTag("select_mode_${key.lowercase()}")
                ) {
                    Text(
                        text = display,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Active 3D Holographic AI face
        NeuralFaceRenderer(
            aegisState = state,
            detectedEmotion = emotion,
            userVoiceVolume = micVolume,
            robotTalkVolume = speakerVolume,
            modifier = Modifier.fillMaxWidth().testTag("active_face_renderer_canvas")
        )

        // Dynamic State Indicators HUD
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HubParamCard(
                label = "AI LOGIC STATUS",
                value = when (state) {
                    AegisState.IDLE -> "STANDBY"
                    AegisState.LISTENING -> "LISTENING..."
                    AegisState.THINKING -> "COMPUTING..."
                    AegisState.SPEAKING -> "TALKING..."
                    AegisState.ERROR -> "SIG RESYNCH"
                },
                color = when (state) {
                    AegisState.LISTENING -> NeonCyan
                    AegisState.THINKING -> NeonPurple
                    AegisState.SPEAKING -> NeonGreen
                    else -> MutedSlate
                },
                modifier = Modifier.weight(1f)
            )

            HubParamCard(
                label = "EMOTION TUNING",
                value = emotion.name,
                color = when (emotion) {
                    UserEmotion.STRESSED -> NeonGreen
                    UserEmotion.ANALYTICAL -> NeonAmber
                    UserEmotion.EXCITED -> Color.Red
                    else -> NeonCyan
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Reusable Unified Synchronized Chat Matrix Console
        AegisSharedChatConsole(
            viewModel = viewModel,
            modifier = Modifier.fillMaxWidth(),
            listModifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        )

        val isHandsFree by viewModel.isHandsFreeActive.collectAsState()

        Card(
            colors = CardDefaults.cardColors(containerColor = GlassSurface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, if (isHandsFree) NeonGreen.copy(alpha = 0.5f) else GlassSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.toggleHandsFreeActive() }
                .testTag("hands_free_toggle_card")
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isHandsFree) Icons.Filled.HearingDisabled else Icons.Filled.Hearing,
                        contentDescription = "Hands-Free Auto-Activation",
                        tint = if (isHandsFree) NeonGreen else NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "HANDS-FREE AUTO-LISTENING",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (isHandsFree) "Continuous stream active. Auto-restart standbys." else "Push-to-talk mode. Manual triggering active.",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MutedSlate
                        )
                    }
                }
                Switch(
                    checked = isHandsFree,
                    onCheckedChange = { viewModel.toggleHandsFreeActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ObsidianBg,
                        checkedTrackColor = NeonGreen,
                        uncheckedThumbColor = MutedSlate,
                        uncheckedTrackColor = GlassSurfaceBorder
                    ),
                    modifier = Modifier.testTag("hands_free_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Kinetic Voice Trigger Button
        Button(
            onClick = { viewModel.triggerMicStart() },
            colors = ButtonDefaults.buttonColors(
                containerColor = when (state) {
                    AegisState.LISTENING -> NeonGreen
                    AegisState.THINKING -> NeonPurple
                    else -> NeonCyan
                }
            ),
            contentPadding = PaddingValues(16.dp),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(64.dp)
                .testTag("vocal_orb_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = when (state) {
                        AegisState.LISTENING -> Icons.Filled.Stop
                        else -> Icons.Filled.Mic
                    },
                    contentDescription = "Trigger Mic",
                    tint = ObsidianBg,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = when (state) {
                        AegisState.LISTENING -> "TAP TO COMPLETE"
                        AegisState.THINKING -> "COMPUTING NEURALS..."
                        AegisState.SPEAKING -> "TRANSMITTING AIR..."
                        else -> "TAP TO SPEAK"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = ObsidianBg,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
    }
}

// ================= TAB 1: SMARTHOME INTERCONNECT =================

@Composable
fun SmartHomeTab(viewModel: AegisViewModel) {
    val devices by viewModel.smartDevices.collectAsState()
    var devName by remember { mutableStateOf("") }
    var devType by remember { mutableStateOf("Light") }
    val types = listOf("Light", "Camera", "Lock", "Thermostat", "Vacuum")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderSection("SMART HOME INTERCONNECTED GRID", "Total Nodes online: ${devices.size}")

        // Add Device Widget
        Card(
            colors = CardDefaults.cardColors(containerColor = GlassSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GlassSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "REGISTER NEW PHYSICAL ENDPOINT",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = devName,
                    onValueChange = { devName = it },
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                    placeholder = { Text("Device name (e.g. Server AC)", fontSize = 12.sp, color = MutedSlate, fontFamily = FontFamily.Monospace) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = ObsidianBg,
                        unfocusedContainerColor = ObsidianBg,
                        focusedContainerColor = ObsidianBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_device_name_field")
                )

                // Type select buttons
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    types.forEach { t ->
                        val isSel = devType == t
                        FilterChip(
                            selected = isSel,
                            onClick = { devType = t },
                            label = { Text(t, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = MutedSlate,
                                selectedLabelColor = ObsidianBg,
                                selectedContainerColor = NeonCyan
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (devName.isNotBlank()) {
                            val ip = "192.168.1.${(10..250).random()}"
                            viewModel.addSmartDevice(devName, devType, "Living Room", ip)
                            devName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    modifier = Modifier.align(Alignment.End).testTag("add_device_confirm_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = ObsidianBg)
                        Text("CONNECT NODE", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ObsidianBg)
                    }
                }
            }
        }

        // Devices list
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            devices.forEach { dev ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = GlassSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GlassSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = when (dev.type) {
                                        "Light" -> Icons.Filled.Lightbulb
                                        "Lock" -> Icons.Filled.Lock
                                        "Camera" -> Icons.Filled.Videocam
                                        "Thermostat" -> Icons.Filled.Thermostat
                                        else -> Icons.Filled.Memory
                                    },
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = dev.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "IP ACCESS: ${dev.internalIp} | ENCRYPTED: ${if (dev.isEncrypted) "AES-CBC" else "OFF"}",
                                color = if (dev.isEncrypted) NeonGreen else NeonAmber,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Button(
                            onClick = { viewModel.toggleDevice(dev) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dev.state == "ON" || dev.state == "Locked" || dev.state.contains("°")) NeonGreen else Color(0x33FFFFFF)
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("toggle_device_${dev.name.lowercase().replace(" ", "_")}")
                        ) {
                            Text(
                                text = dev.state,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (dev.state == "ON" || dev.state == "Locked" || dev.state.contains("°")) ObsidianBg else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
    }
}

// ================= TAB 2: GMAIL AUTOMATION DOCK =================

@Composable
fun GmailTab(viewModel: AegisViewModel) {
    val emailQueue by viewModel.emailQueue.collectAsState()
    var emailRecipient by remember { mutableStateOf("") }
    var emailSubject by remember { mutableStateOf("") }
    var emailBodyPrompt by remember { mutableStateOf("") }
    var emailCategory by remember { mutableStateOf("Automation") }
    val categories = listOf("Automation", "Inquiry", "Alert", "Response")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderSection("GMAIL MASS TRANSIT ENGINE", "Mail Queued: ${emailQueue.size}")

        // Composer form
        Card(
            colors = CardDefaults.cardColors(containerColor = GlassSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GlassSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "COMPILE PROFESSIONAL BROADCAST",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = emailRecipient,
                    onValueChange = { emailRecipient = it },
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                    placeholder = { Text("Recipient email address", fontSize = 12.sp, color = MutedSlate, fontFamily = FontFamily.Monospace) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = ObsidianBg,
                        unfocusedContainerColor = ObsidianBg,
                        focusedContainerColor = ObsidianBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("email_recipient_field")
                )

                OutlinedTextField(
                    value = emailSubject,
                    onValueChange = { emailSubject = it },
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                    placeholder = { Text("Subject (or keywords for auto-generate)", fontSize = 12.sp, color = MutedSlate, fontFamily = FontFamily.Monospace) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = ObsidianBg,
                        unfocusedContainerColor = ObsidianBg,
                        focusedContainerColor = ObsidianBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("email_subject_field")
                )

                OutlinedTextField(
                    value = emailBodyPrompt,
                    onValueChange = { emailBodyPrompt = it },
                    minLines = 3,
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                    placeholder = { Text("What should Aegis draft? (e.g. Congratulate team on successful security launch and invite to server room 7)", fontSize = 11.sp, color = MutedSlate, fontFamily = FontFamily.Monospace) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = ObsidianBg,
                        unfocusedContainerColor = ObsidianBg,
                        focusedContainerColor = ObsidianBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("email_prompt_field")
                )

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { c ->
                        val isMatch = emailCategory == c
                        FilterChip(
                            selected = isMatch,
                            onClick = { emailCategory = c },
                            label = { Text(c, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = MutedSlate,
                                selectedLabelColor = ObsidianBg,
                                selectedContainerColor = NeonCyan
                            )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (emailRecipient.isNotBlank() && emailBodyPrompt.isNotBlank()) {
                            viewModel.queueMassEmails(emailRecipient, emailSubject, emailBodyPrompt, emailCategory)
                            emailRecipient = ""
                            emailSubject = ""
                            emailBodyPrompt = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    modifier = Modifier.align(Alignment.End).testTag("email_submit_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null, tint = ObsidianBg)
                        Text("QUEUE AUTO-DRAFT", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ObsidianBg)
                    }
                }
            }
        }

        // Ledger logs
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("AUTOMATED COMMS LEDGER", fontSize = 12.sp, color = MutedSlate, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text(
                    "PURGE ALL DRAFTS",
                    fontSize = 11.sp,
                    color = NeonAmber,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.clickable { viewModel.purgeEmails() }
                )
            }

            emailQueue.forEach { mail ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = GlassSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GlassSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TO: ${mail.recipient}",
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (mail.status == "SENT") NeonGreen else NeonAmber)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    mail.status,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = ObsidianBg,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "SUBJECT: ${mail.subject}",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan
                        )

                        Text(
                            text = mail.body,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MutedSlate,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
    }
}

// ================= TAB 3: HEALTH VITALITY & SECURITY COMMAND =================

@Composable
fun VitalityAndScannerTab(viewModel: AegisViewModel) {
    val telemetryList by viewModel.recentTelemetry.collectAsState()
    val logsList by viewModel.securityLogLedger.collectAsState()
    val isScanning by viewModel.isSecurityScanning.collectAsState()

    val currentBio = telemetryList.firstOrNull() ?: HealthTelemetry(heartRate = 72, oxygenLevelByPercent = 98, stressLevel = "Normal", detectedEmotion = "Calm")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderSection("VITALITY COMPASS & SECURITY CORE", "Secure Shield: ACTIVE")

        // Health meters Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GlassSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GlassSurfaceBorder),
                modifier = Modifier.weight(1f).aspectRatio(1.2f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(24.dp))
                    Text(text = "${currentBio.heartRate} BPM", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text(text = "LIVE HEART RATE", fontSize = 10.sp, color = MutedSlate, fontFamily = FontFamily.Monospace)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = GlassSurface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GlassSurfaceBorder),
                modifier = Modifier.weight(1f).aspectRatio(1.2f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Icon(Icons.Filled.Bloodtype, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                    Text(text = "${currentBio.oxygenLevelByPercent}% O2", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text(text = "SPO2 MARKS", fontSize = 10.sp, color = MutedSlate, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Cyber Security Scanner Button
        Button(
            onClick = { viewModel.runCyberSecurityScan() },
            colors = ButtonDefaults.buttonColors(containerColor = if (isScanning) NeonPurple else NeonCyan),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("cyber_scan_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = ObsidianBg)
                Text(
                    text = if (isScanning) "AUDITING PERIMETER CHANNELS..." else "DISPATCH CYBER SECURITY PENETRATION AUDIT",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = ObsidianBg
                )
            }
        }

        // Terminal Log Ledger
        Card(
            colors = CardDefaults.cardColors(containerColor = GlassSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GlassSurfaceBorder),
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("AEGIS SECURITY AUDIT LOGS", fontSize = 11.sp, color = NeonCyan, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isScanning) NeonPurple else NeonGreen)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        logsList.forEach { log ->
                            Text(
                                text = log,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = if (log.contains("COMPLETE") || log.contains("Shield")) NeonGreen else if (log.contains("WARNING") || log.contains("Remote")) NeonAmber else MutedSlate
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
    }
}

// ================= TAB 4: WORKFLOW ARCHITECT =================

@Composable
fun WorkflowsBuilderTab(viewModel: AegisViewModel) {
    val workflows by viewModel.workflows.collectAsState()
    var wfName by remember { mutableStateOf("") }
    var triggerOption by remember { mutableStateOf("Voice") }
    var triggerValue by remember { mutableStateOf("") }
    var actionOption by remember { mutableStateOf("SmartHome") }
    var actionValue by remember { mutableStateOf("") }
    var startAsQuickAction by remember { mutableStateOf(false) }

    val triggers = listOf("Voice", "Biometric", "Time")
    val actions = listOf("SmartHome", "Gmail", "SecurityScan")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderSection("NEURAL AUTOMATION ROUTINES", "Total custom workflows: ${workflows.size}")

        // Aegis firebase account sync replicator panel
        val isFirebaseUserLoggedIn by viewModel.isUserLoggedIn.collectAsState()
        val firebaseUser by viewModel.firebaseAccountUser.collectAsState()
        val isSyncingData by viewModel.isSyncing.collectAsState()
        val authError by viewModel.authErrorMessage.collectAsState()
        val authSuccess by viewModel.authSuccessMessage.collectAsState()

        var authEmail by remember { mutableStateOf("") }
        var authPassword by remember { mutableStateOf("") }

        Card(
            colors = CardDefaults.cardColors(containerColor = GlassSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, if (isFirebaseUserLoggedIn) NeonPurple.copy(alpha = 0.5f) else GlassSurfaceBorder),
            modifier = Modifier.fillMaxWidth().testTag("firebase_sync_matrix_card")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudSync,
                            contentDescription = "Cloud Replicator",
                            tint = if (isFirebaseUserLoggedIn) NeonPurple else MutedSlate,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "AEGIS SECURE CLOUD REPLICATOR",
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isFirebaseUserLoggedIn) NeonPurple else Color.Red)
                    )
                }

                Text(
                    text = "Backup of customized logic automation rules and settings directly to safe cloud vault.",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MutedSlate
                )

                if (isFirebaseUserLoggedIn) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianBg.copy(alpha = 0.6f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ESTABLISHED LINK: ${firebaseUser ?: "Secured Account"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.backupSettingsAndFlows() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                enabled = !isSyncingData,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("backup_flows_button")
                            ) {
                                Text(
                                    text = "UPLOAD CLOUD",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = { viewModel.restoreSettingsAndFlows() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                enabled = !isSyncingData,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("restore_flows_button")
                            ) {
                                Text(
                                    text = "DOWNLOAD CLOUD",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = ObsidianBg
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.logoutUser() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("deauth_sync_button")
                        ) {
                            Text(
                                text = "DE-AUTHORIZE SECURE NODE",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = authEmail,
                            onValueChange = { authEmail = it },
                            placeholder = { Text("Identity email...", fontSize = 11.sp, color = MutedSlate, fontFamily = FontFamily.Monospace) },
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassSurfaceBorder,
                                unfocusedContainerColor = ObsidianBg,
                                focusedContainerColor = ObsidianBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_field")
                        )

                        OutlinedTextField(
                            value = authPassword,
                            onValueChange = { authPassword = it },
                            placeholder = { Text("Vault key passsequence (min 6)...", fontSize = 11.sp, color = MutedSlate, fontFamily = FontFamily.Monospace) },
                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = GlassSurfaceBorder,
                                unfocusedContainerColor = ObsidianBg,
                                focusedContainerColor = ObsidianBg,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_field")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.loginUser(authEmail, authPassword) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                enabled = !isSyncingData && authEmail.isNotBlank() && authPassword.isNotBlank(),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("login_sync_button")
                            ) {
                                Text(
                                    text = "SYNC KEY",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = ObsidianBg
                                )
                            }

                            Button(
                                onClick = { viewModel.signUpUser(authEmail, authPassword) },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                enabled = !isSyncingData && authEmail.isNotBlank() && authPassword.isNotBlank(),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("register_sync_button")
                            ) {
                                Text(
                                    text = "DEPLOY NODE",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                if (isSyncingData) {
                    LinearProgressIndicator(
                        color = NeonPurple,
                        trackColor = GlassSurfaceBorder,
                        modifier = Modifier.fillMaxWidth().height(2.dp)
                    )
                }

                authError?.let {
                    Text(
                        text = "ALERT: $it",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }

                authSuccess?.let {
                    Text(
                        text = "STATUS: $it",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = NeonGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Creator widget
        Card(
            colors = CardDefaults.cardColors(containerColor = GlassSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, GlassSurfaceBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "COMPILE AUTOMATION NODE",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )

                // Workflow title name
                OutlinedTextField(
                    value = wfName,
                    onValueChange = { wfName = it },
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                    placeholder = { Text("Workflow name (e.g., Lockdown)", fontSize = 12.sp, color = MutedSlate, fontFamily = FontFamily.Monospace) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = ObsidianBg,
                        unfocusedContainerColor = ObsidianBg,
                        focusedContainerColor = ObsidianBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_workflow_name_field")
                )

                // Trigger Type Selector Row
                Text("1. CHOOSE TRIGGER CLASSIFICATION:", fontSize = 10.sp, color = MutedSlate, fontFamily = FontFamily.Monospace)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    triggers.forEach { t ->
                        val isPicked = triggerOption == t
                        FilterChip(
                            selected = isPicked,
                            onClick = { triggerOption = t },
                            label = { Text(t, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = MutedSlate,
                                selectedLabelColor = ObsidianBg,
                                selectedContainerColor = NeonCyan
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = triggerValue,
                    onValueChange = { triggerValue = it },
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                    placeholder = {
                        Text(
                            text = when (triggerOption) {
                                "Voice" -> "Voice keyword (e.g. abort flight)"
                                "Biometric" -> "Value formula (e.g. HR > 100)"
                                else -> "Specific clock time (e.g. 08:00)"
                            },
                            fontSize = 11.sp,
                            color = MutedSlate,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = ObsidianBg,
                        unfocusedContainerColor = ObsidianBg,
                        focusedContainerColor = ObsidianBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_workflow_trigger_val")
                )

                // Action Type Selector Row
                Text("2. COMPLY CORRESPONDING AUTOMATED RESPONSE ACTION:", fontSize = 10.sp, color = MutedSlate, fontFamily = FontFamily.Monospace)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    actions.forEach { a ->
                        val isPrk = actionOption == a
                        FilterChip(
                            selected = isPrk,
                            onClick = { actionOption = a },
                            label = { Text(a, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = MutedSlate,
                                selectedLabelColor = ObsidianBg,
                                selectedContainerColor = NeonCyan
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = actionValue,
                    onValueChange = { actionValue = it },
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                    placeholder = { Text("Action parameter (e.g. Core Cooling ON)", fontSize = 11.sp, color = MutedSlate, fontFamily = FontFamily.Monospace) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = ObsidianBg,
                        unfocusedContainerColor = ObsidianBg,
                        focusedContainerColor = ObsidianBg,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_workflow_action_val")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = startAsQuickAction,
                        onCheckedChange = { startAsQuickAction = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = NeonCyan,
                            checkmarkColor = ObsidianBg,
                            uncheckedColor = GlassSurfaceBorder
                        ),
                        modifier = Modifier.testTag("add_workflow_quick_checkbox")
                    )
                    Text(
                        text = "ADD DIRECTLY TO QUICK ACTIONS TOOLBAR",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                }

                Button(
                    onClick = {
                        if (wfName.isNotBlank() && triggerValue.isNotBlank() && actionValue.isNotBlank()) {
                            viewModel.addTriggerWorkflow(wfName, triggerOption, triggerValue, actionOption, actionValue, startAsQuickAction)
                            wfName = ""
                            triggerValue = ""
                            actionValue = ""
                            startAsQuickAction = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    modifier = Modifier.align(Alignment.End).testTag("add_workflow_submit_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.AccountTree, contentDescription = null, tint = ObsidianBg)
                        Text("COMPILE ACTION ROUTINE", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color = ObsidianBg)
                    }
                }
            }
        }

        // Active workflows list
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("ACTIVE AUTOMATED PIPELINES", fontSize = 12.sp, color = MutedSlate, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)

            workflows.forEach { wf ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = GlassSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GlassSurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ROUTINE: ${wf.name}",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Switch(
                                    checked = wf.isActive,
                                    onCheckedChange = { viewModel.toggleWorkflowActive(wf) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = NeonCyan,
                                        checkedTrackColor = ObsidianBg
                                    )
                                )
                                IconButton(
                                    onClick = { viewModel.toggleWorkflowQuickAction(wf) },
                                    modifier = Modifier.size(24.dp).testTag("toggle_workflow_quick_action_${wf.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bolt,
                                        contentDescription = "Toggle Quick Action State",
                                        tint = if (wf.isQuickAction) NeonCyan else MutedSlate
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.removeWorkflow(wf) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                                }
                            }
                        }

                        // Node link visualization
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(ObsidianBg)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.ArrowUpward, contentDescription = "Trigger", tint = NeonCyan, modifier = Modifier.size(14.dp))
                            Text(text = "[${wf.triggerType}] matches \"${wf.triggerValue}\"", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MutedSlate, modifier = Modifier.size(12.dp))
                            Icon(Icons.Filled.KeyboardDoubleArrowDown, contentDescription = "Action", tint = NeonPurple, modifier = Modifier.size(14.dp))
                            Text(text = "[${wf.actionType}] dispatch \"${wf.actionValue}\"", color = NeonPurple, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
    }
}

// --- Aegis Chat Components (Persistent & Unified) ---

@Composable
fun PersistentAegisChatDrawer(
    viewModel: AegisViewModel,
    onClose: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianBg),
        shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
        border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                    )
                    Text(
                        text = "AEGIS CONTEXT EYE",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp).testTag("close_persistent_chat")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close persistent console",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Standard Matrix console inside
            AegisSharedChatConsole(
                viewModel = viewModel,
                modifier = Modifier.weight(1f),
                listModifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                showVoiceControls = true,
                onClose = onClose
            )
        }
    }
}

@Composable
fun AegisSharedChatConsole(
    viewModel: AegisViewModel,
    modifier: Modifier = Modifier,
    listModifier: Modifier = Modifier.fillMaxWidth().height(220.dp),
    showVoiceControls: Boolean = false,
    onClose: (() -> Unit)? = null
) {
    val systemState by viewModel.vocalSystemState.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var keyboardInputBuffer by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, GlassSurfaceBorder),
        modifier = modifier.fillMaxWidth().testTag("transcript_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                     verticalAlignment = Alignment.CenterVertically,
                     horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Language,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "AURA AI CHAT MATRIX",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MutedSlate,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.clearChatHistory() },
                        modifier = Modifier.size(24.dp).testTag("chat_purge_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Purge logs",
                            tint = NeonAmber,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (systemState == AegisState.LISTENING) NeonCyan else MutedSlate)
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = listModifier,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = chatMessages,
                    key = { it.id }
                ) { msg ->
                    val isUser = msg.sender == "user"
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .clip(RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isUser) 12.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 12.dp
                                ))
                                .background(
                                    if (isUser) GlassSurface.copy(alpha = 0.5f)
                                    else Color(0x3B1F293D)
                                )
                                .border(
                                    1.dp,
                                    if (isUser) GlassSurfaceBorder else NeonPurple.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isUser) 12.dp else 2.dp,
                                        bottomEnd = if (isUser) 2.dp else 12.dp
                                    )
                                )
                                .padding(vertical = 10.dp, horizontal = 12.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (isUser) "[OPERATOR]" else "[AEGIS VOICE CORE]",
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUser) NeonCyan else NeonPurple
                                )
                                Text(
                                    text = msg.text,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Silent command input box integrated
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showVoiceControls) {
                    IconButton(
                        onClick = { viewModel.triggerMicStart() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (systemState) {
                                    AegisState.LISTENING -> NeonGreen
                                    AegisState.THINKING -> NeonPurple
                                    else -> NeonCyan.copy(alpha = 0.2f)
                                }
                            )
                            .border(1.dp, NeonCyan, RoundedCornerShape(12.dp))
                            .testTag("chat_voice_mic_button")
                    ) {
                        Icon(
                            imageVector = when (systemState) {
                                AegisState.LISTENING -> Icons.Filled.Stop
                                else -> Icons.Filled.Mic
                            },
                            contentDescription = "Trigger voice in chat",
                            tint = if (systemState == AegisState.LISTENING) ObsidianBg else NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = keyboardInputBuffer,
                    onValueChange = { keyboardInputBuffer = it },
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                    placeholder = { Text("Compile directive...", fontSize = 12.sp, color = MutedSlate, fontFamily = FontFamily.Monospace) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = GlassSurfaceBorder,
                        unfocusedContainerColor = ObsidianBg.copy(alpha = 0.5f),
                        focusedContainerColor = ObsidianBg.copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("manual_text_input")
                )

                IconButton(
                    onClick = {
                        if (keyboardInputBuffer.isNotBlank()) {
                            viewModel.submitTextCommand(keyboardInputBuffer)
                            keyboardInputBuffer = ""
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonCyan)
                        .testTag("manual_text_submit_button")
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Submit", tint = ObsidianBg, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// --- Dynamic Common Custom Widgets ---

@Composable
fun HeaderSection(title: String, subtitle: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, fontSize = 16.sp, fontFamily = FontFamily.Monospace, color = NeonCyan, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = subtitle, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MutedSlate)
    }
}

@Composable
fun HubParamCard(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GlassSurfaceBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = MutedSlate, fontWeight = FontWeight.Bold)
            Text(text = value, fontSize = 14.sp, fontFamily = FontFamily.Monospace, color = color, fontWeight = FontWeight.Black)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionsToolbar(viewModel: AegisViewModel) {
    val workflows by viewModel.workflows.collectAsState()
    val quickActions = workflows.filter { it.isQuickAction }
    var showConfigDialog by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = GlassSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, GlassSurfaceBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_actions_toolbar")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "Quick Actions Icon",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "AUTOMATION QUICK CHIPS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isConsoleVisible by viewModel.isSystemConsoleVisible.collectAsState()
                    IconButton(
                        onClick = { viewModel.toggleSystemConsole() },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("toggle_system_console_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Terminal,
                            contentDescription = "Toggle System Console Panel",
                            tint = if (isConsoleVisible) NeonCyan else MutedSlate,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = { showConfigDialog = true },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("quick_action_config_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Configure Quick Actions",
                            tint = MutedSlate,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (quickActions.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "No active quick chips. Tap ⚙️ to configure shortcuts.",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MutedSlate
                    )
                }
            } else {
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(quickActions) { action ->
                        // Pick icon depending on action type
                        val icon = when (action.actionType) {
                            "SecurityScan" -> Icons.Filled.Shield
                            "Gmail" -> Icons.Filled.Email
                            "SmartHome" -> Icons.Filled.Lightbulb
                            else -> Icons.Filled.Star
                        }

                        val accentColor = when (action.actionType) {
                            "SecurityScan" -> NeonGreen
                            "Gmail" -> NeonPurple
                            "SmartHome" -> NeonCyan
                            else -> NeonAmber
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(accentColor.copy(alpha = 0.15f))
                                .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable { viewModel.triggerWorkflowDirectly(action) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("quick_action_btn_${action.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = action.name,
                                    tint = accentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = action.name.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfigDialog) {
        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            containerColor = ObsidianBg,
            titleContentColor = Color.White,
            textContentColor = MutedSlate,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bolt,
                        contentDescription = "Bolt",
                        tint = NeonCyan
                    )
                    Text(
                        text = "TOOLBAR CONFIGURATOR",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Toggle which automated routines are instantly available in your Quick Action toolbar at the top. They can also be spoken as 'Run [Name]'.",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MutedSlate
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (workflows.isEmpty()) {
                        Text(
                            text = "No custom logic workflows to display. Go to Custom Logic tab to compile new offline automations first.",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = NeonAmber
                        )
                    } else {
                        workflows.forEach { wf ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(GlassSurface)
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = wf.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Voice cmd: \"Run ${wf.name}\"",
                                        fontSize = 9.sp,
                                        color = MutedSlate,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Switch(
                                    checked = wf.isQuickAction,
                                    onCheckedChange = { viewModel.toggleWorkflowQuickAction(wf) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = ObsidianBg,
                                        checkedTrackColor = NeonCyan,
                                        uncheckedThumbColor = MutedSlate,
                                        uncheckedTrackColor = GlassSurfaceBorder
                                    ),
                                    modifier = Modifier.testTag("quick_action_dialog_toggle_wf_${wf.id}")
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showConfigDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = ObsidianBg),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("quick_action_dialog_close")
                ) {
                    Text(
                        text = "SECURE & ACCEDE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        )
    }
}

@Composable
fun SystemConsolePanel(
    viewModel: AegisViewModel,
    modifier: Modifier = Modifier
) {
    val devLogs by viewModel.systemConsoleLogs.collectAsState()
    var expandedLogId by remember { mutableStateOf<String?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = ObsidianBg),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, NeonPurple.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("system_console_panel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Code,
                        contentDescription = "Console Icon",
                        tint = NeonPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "AEGIS SECURE SYSTEM CONSOLE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // CLEAR CONSOLE
                    IconButton(
                        onClick = { viewModel.clearConsoleLogs() },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("system_console_clear_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Clear Console Shell",
                            tint = MutedSlate,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // DISMISS
                    IconButton(
                        onClick = { viewModel.toggleSystemConsole() },
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("system_console_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Developer Console",
                            tint = MutedSlate,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NeonPurple.copy(alpha = 0.3f))
                    .padding(bottom = 4.dp)
            )

            // Scrollable logs list
            if (devLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CONSOLE STREAMS IDLE. NO ACTIVE EVENTS.",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MutedSlate
                    )
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("system_console_log_list"),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(devLogs, key = { it.id }) { log ->
                        val isExpanded = expandedLogId == log.id

                        val categoryColor = when (log.type) {
                            "API_REQUEST" -> NeonPurple
                            "API_RESPONSE" -> NeonGreen
                            "AUTOMATION_TRIGGER" -> NeonAmber
                            "ACTION_EXECUTION" -> NeonCyan
                            else -> MutedSlate
                        }

                        val formattedTime = remember(log.timestamp) {
                            val timePart = log.timestamp % 1000000 / 1000
                            "T+$timePart"
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(GlassSurface)
                                .border(
                                    1.dp,
                                    if (isExpanded) categoryColor.copy(alpha = 0.4f) else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    expandedLogId = if (isExpanded) null else log.id
                                }
                                .padding(8.dp)
                                .testTag("system_console_log_item_${log.id}")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "[${log.type}]",
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = categoryColor
                                    )
                                    Text(
                                        text = log.message,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White,
                                        maxLines = if (isExpanded) Int.MAX_VALUE else 1
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = formattedTime,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MutedSlate
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "Expand Log Meta",
                                        tint = MutedSlate,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            if (isExpanded && !log.detail.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ObsidianBg)
                                        .border(1.dp, GlassSurfaceBorder, RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = log.detail ?: "",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFFD4D4D4),
                                        modifier = Modifier.fillMaxWidth().testTag("system_console_log_detail_${log.id}")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

