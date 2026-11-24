package com.cyberdetective.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import javax.net.ssl.HttpsURLConnection

private val Dark = Color(0xFF07120F)
private val Panel = Color(0xFF10251D)
private val Green = Color(0xFF39F59B)
private val White = Color(0xFFE8FFF2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CyberDetectiveApp()
        }
    }
}

@Composable
fun CyberDetectiveApp() {
    var website by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("Ready to scan.") }
    var loading by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf("Dashboard") }

    val scope = rememberCoroutineScope()

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Dark)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text(
                "CYBER DETECTIVE",
                color = Green,
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Your Personal Cybersecurity Toolkit",
                color = White,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(25.dp))

            Text("SYSTEM STATUS", color = Green, fontWeight = FontWeight.Bold)

            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("● SYSTEM ONLINE", color = Green)
                    Spacer(Modifier.height(8.dp))
                    Text("Security tools are ready.", color = White)
                }
            }

            Spacer(Modifier.height(22.dp))

            Text("SECURITY TOOLS", color = Green, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))

            ToolButton("🌐 HTTPS / SSL CHECKER") {
                selected = "Website"
            }

            ToolButton("🔐 PASSWORD STRENGTH CHECKER") {
                selected = "Password"
            }

            ToolButton("🔎 IP ADDRESS LOOKUP") {
                selected = "IP"
            }

            Spacer(Modifier.height(20.dp))

            when (selected) {
                "Website" -> {
                    Text("WEBSITE SECURITY CHECK", color = Green)
                    OutlinedTextField(
                        value = website,
                        onValueChange = { website = it },
                        label = { Text("Enter website URL") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            scope.launch {
                                loading = true
                                result = checkWebsite(website)
                                loading = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green
                        )
                    ) {
                        Text("CHECK WEBSITE", color = Dark)
                    }
                }

                "Password" -> {
                    Text("PASSWORD STRENGTH", color = Green)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Enter a test password") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    Button(
                        onClick = {
                            result = passwordStrength(password)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green
                        )
                    ) {
                        Text("ANALYZE", color = Dark)
                    }
                }

                "IP" -> {
                    Text("PUBLIC IP LOOKUP", color = Green)
                    Text(
                        "This checks your public IP address.",
                        color = White
                    )

                    Button(
                        onClick = {
                            scope.launch {
                                loading = true
                                result = getPublicIP()
                                loading = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green
                        )
                    ) {
                        Text("LOOK UP IP", color = Dark)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = Panel),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("SCAN RESULTS", color = Green, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        if (loading) "Scanning..." else result,
                        color = White
                    )
                }
            }

            Spacer(Modifier.height(25.dp))

            Text(
                "CYBER DETECTIVE • SECURITY TOOLKIT",
                color = Green,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun ToolButton(title: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .clickable { onClick() }
    ) {
        Text(
            title,
            color = Green,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(18.dp)
        )
    }
}

suspend fun checkWebsite(input: String): String =
    withContext(Dispatchers.IO) {
        try {
            val address = if (
                input.startsWith("https://") ||
                input.startsWith("http://")
            ) input else "https://$input"

            val connection = URL(address).openConnection()
                as HttpsURLConnection

            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.requestMethod = "HEAD"
            connection.connect()

            val code = connection.responseCode
            val certificate = connection.serverCertificates
                .firstOrNull()

            val expiry = if (certificate is java.security.cert.X509Certificate) {
                certificate.notAfter.toString()
            } else {
                "Unavailable"
            }

            connection.disconnect()

            "HTTPS CONNECTION SUCCESSFUL\nHTTP STATUS: $code\nCERTIFICATE EXPIRY: $expiry"
        } catch (e: Exception) {
            "Unable to verify website.\n${e.message ?: "Connection error"}"
        }
    }

fun passwordStrength(value: String): String {
    var score = 0

    if (value.length >= 8) score++
    if (value.length >= 12) score++
    if (value.any { it.isUpperCase() }) score++
    if (value.any { it.isLowerCase() }) score++
    if (value.any { it.isDigit() }) score++
    if (value.any { !it.isLetterOrDigit() }) score++

    return when {
        value.isEmpty() -> "Enter a password first."
        score <= 2 -> "WEAK PASSWORD"
        score <= 4 -> "MEDIUM PASSWORD"
        else -> "STRONG PASSWORD"
    }
}

suspend fun getPublicIP(): String =
    withContext(Dispatchers.IO) {
        try {
            URL("https://api.ipify.org").readText()
                .let { "YOUR PUBLIC IP ADDRESS:\n$it" }
        } catch (e: Exception) {
            "IP lookup failed: ${e.message}"
        }
    }
    
