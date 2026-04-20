package com.mirea.module4.ui.task14

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlin.math.cos
import kotlin.math.sin

// Задание 14: Компас

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Task14Screen(navController: NavController, vm: Task14ViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val azimuth by vm.azimuth.collectAsState()
    val sensorAvailable by vm.sensorAvailable.collectAsState()

    // Lifecycle-aware sensor management
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> vm.registerSensors(context)
                Lifecycle.Event.ON_PAUSE -> vm.unregisterSensors()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            vm.unregisterSensors()
        }
    }

    // Анимированный поворот стрелки
    val animatedAzimuth by animateFloatAsState(
        targetValue = -azimuth,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "compass"
    )

    Scaffold(
        containerColor = Color(0xFF1A1A2E),
        topBar = {
            TopAppBar(
                title = { Text("Задание 14: Компас", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A2E))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!sensorAvailable) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Устройство не поддерживает\nдатчик ориентации",
                        color = Color.Red,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                return@Column
            }

            Spacer(Modifier.height(8.dp))

            // Compass dial
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                val dialColor = Color(0xFF2D2D44)
                val borderColor = Color(0xFF5C5C8A)
                val northColor = Color(0xFFE53935)
                val southColor = Color(0xFFBDBDBD)
                val textColor = Color(0xFFE0E0E0)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val r = size.width / 2f - 8.dp.toPx()

                    // Outer ring
                    drawCircle(color = dialColor, radius = r)
                    drawCircle(color = borderColor, radius = r, style = Stroke(width = 3.dp.toPx()))

                    // Cardinal directions tick marks
                    val dirs = listOf(0 to "N", 90 to "E", 180 to "S", 270 to "W")
                    for ((deg, _) in dirs) {
                        val rad = Math.toRadians(deg.toDouble())
                        val x1 = cx + (r - 20.dp.toPx()) * sin(rad).toFloat()
                        val y1 = cy - (r - 20.dp.toPx()) * cos(rad).toFloat()
                        val x2 = cx + (r - 5.dp.toPx()) * sin(rad).toFloat()
                        val y2 = cy - (r - 5.dp.toPx()) * cos(rad).toFloat()
                        drawLine(color = textColor, start = Offset(x1, y1), end = Offset(x2, y2),
                            strokeWidth = 2.dp.toPx())
                    }
                    // Minor tick marks every 30°
                    for (deg in 0 until 360 step 30) {
                        if (deg % 90 == 0) continue
                        val rad = Math.toRadians(deg.toDouble())
                        val x1 = cx + (r - 12.dp.toPx()) * sin(rad).toFloat()
                        val y1 = cy - (r - 12.dp.toPx()) * cos(rad).toFloat()
                        val x2 = cx + (r - 5.dp.toPx()) * sin(rad).toFloat()
                        val y2 = cy - (r - 5.dp.toPx()) * cos(rad).toFloat()
                        drawLine(color = textColor.copy(alpha = 0.4f), start = Offset(x1, y1), end = Offset(x2, y2),
                            strokeWidth = 1.dp.toPx())
                    }

                    // Compass needle (rotated by azimuth)
                    rotate(animatedAzimuth, pivot = Offset(cx, cy)) {
                        val needleLen = r * 0.78f
                        val needleWidth = 8.dp.toPx()
                        // North (red)
                        drawLine(
                            color = northColor,
                            start = Offset(cx, cy),
                            end = Offset(cx, cy - needleLen),
                            strokeWidth = needleWidth,
                            cap = StrokeCap.Round
                        )
                        // South (grey)
                        drawLine(
                            color = southColor,
                            start = Offset(cx, cy),
                            end = Offset(cx, cy + needleLen * 0.7f),
                            strokeWidth = needleWidth,
                            cap = StrokeCap.Round
                        )
                        // Center dot
                        drawCircle(color = Color.White, radius = 8.dp.toPx(), center = Offset(cx, cy))
                    }
                }

                // "N" label at top
                Text(
                    "N",
                    color = Color(0xFFE53935),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
                )
            }

            // Azimuth value
            Text(
                text = "Азимут: ${azimuth.toInt()}°",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Direction name
            val directionName = when {
                azimuth < 22.5 || azimuth >= 337.5 -> "Север ↑"
                azimuth < 67.5 -> "Северо-восток ↗"
                azimuth < 112.5 -> "Восток →"
                azimuth < 157.5 -> "Юго-восток ↘"
                azimuth < 202.5 -> "Юг ↓"
                azimuth < 247.5 -> "Юго-запад ↙"
                azimuth < 292.5 -> "Запад ←"
                else -> "Северо-запад ↖"
            }
            Text(directionName, color = Color(0xFFBDBDBD), fontSize = 18.sp)
        }
    }
}
