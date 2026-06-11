package com.ryan.scholarspace

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ryan.scholarspace.ui.theme.ScholarSpaceTheme

class SplashActivity : ComponentActivity() {

    // Explicit Handler usage (fulfills lab requirement: Background Thread dengan Handler)
    private val splashHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        splashHandler.postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2800L)

        setContent {
            ScholarSpaceTheme(darkTheme = true) {
                SplashScreen()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        splashHandler.removeCallbacksAndMessages(null)
    }
}

@Composable
fun SplashScreen() {
    var started by remember { mutableStateOf(false) }

    val logoScale by animateFloatAsState(
        targetValue = if (started) 1f else 0.35f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logoScale"
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 700, delayMillis = 380, easing = FastOutSlowInEasing),
        label = "textAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "splashLoop")

    val ringPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring"
    )

    val dotPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot"
    )

    LaunchedEffect(Unit) { started = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0B2A),
                        Color(0xFF1A1648),
                        Color(0xFF2D2B7A),
                        Color(0xFF3730A3)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .scale(ringPulse * (logoScale.coerceAtMost(1f)))
                .alpha(contentAlpha * 0.12f)
                .size(220.dp)
                .background(Color(0xFF818CF8).copy(alpha = 0.4f), CircleShape)
        )

        // Inner glow ring
        Box(
            modifier = Modifier
                .scale(logoScale)
                .alpha(contentAlpha * 0.18f)
                .size(150.dp)
                .background(Color.White.copy(alpha = 0.15f), CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App logo
            Box(
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(contentAlpha)
                    .size(100.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.22f),
                                Color.White.copy(alpha = 0.06f)
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "ScholarSpace",
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-0.8).sp,
                modifier = Modifier.alpha(textAlpha)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Temukan Beasiswa & Kursus Terbaik",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.68f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(textAlpha)
                    .padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(72.dp))

            // Animated loading dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                modifier = Modifier.alpha(dotPulse * contentAlpha)
            ) {
                repeat(3) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(Color.White.copy(alpha = 0.7f), CircleShape)
                    )
                }
            }
        }

        // Bottom developer credits
        Box(
            Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp)
                .alpha(textAlpha),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Ryan Firmansyah  •  H071241082",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.55f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Universitas Hasanuddin  •  Lab Mobile 2026",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.38f)
                        )
                    }
                }
            }
        }
    }
}
