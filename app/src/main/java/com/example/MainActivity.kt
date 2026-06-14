package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.ui.AegisViewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NeonCyan
import com.example.ui.screens.ObsidianBg
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianBg
                ) {
                    VocalPermissionGateway()
                }
            }
        }
    }
}

@Composable
fun VocalPermissionGateway() {
    val context = LocalContext.current
    val viewModel: AegisViewModel = viewModel()
    
    // Check initial permission status
    var isPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        isPermissionGranted = granted
    }

    if (isPermissionGranted) {
        // Permission granted -> enter full futuristic console dashboard
        DashboardScreen(viewModel = viewModel)
    } else {
        // Permission Gate screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ObsidianBg)
                .padding(24.dp)
                .testTag("permission_gate_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0x1A00F0FF), shape = RoundedCornerShape(40.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Voice Required",
                        tint = NeonCyan,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = "AURA AEGIS SYS OVERRIDE",
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = NeonCyan,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Initializing Jarvis-compatible voice neural matrix.\nAegis requires on-device recording authorization to filter verbal requests, detect client stress metrics, and route automation nodes offline.",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                        .testTag("request_audio_permission_button")
                ) {
                    Text(
                        text = "AUTHORIZE VOCAL CORE",
                        color = ObsidianBg,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

