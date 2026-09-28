package com.chatpataprani.reeldrop.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chatpataprani.reeldrop.util.InstagramProfileAnalyzer

@Composable
fun InstagramProfilePage(onNavigateBack: () -> Unit) {
    var input by remember { mutableStateOf("") }
    var state by remember { mutableStateOf<InstagramProfileAnalyzer.Result?>(null) }
    var loading by remember { mutableStateOf(false) }

    fun analyze() { loading = true; state = null }

    LaunchedEffect(loading) {
        if (loading) {
            state = InstagramProfileAnalyzer.analyze(input)
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) { Icon(Icons.Outlined.ArrowBack, null) }
            Text("Instagram profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = input, onValueChange = { input = it }, modifier = Modifier.fillMaxWidth(),
            label = { Text("Username or Instagram profile URL") }, singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = ::analyze, enabled = input.isNotBlank() && !loading, modifier = Modifier.fillMaxWidth()) {
            if (loading) CircularProgressIndicator() else {
                Icon(Icons.Outlined.PersonSearch, null)
                Text(" Analyze profile")
            }
        }
        Spacer(Modifier.height(20.dp))
        when (val result = state) {
            null -> Unit
            InstagramProfileAnalyzer.Result.NotFound -> Text(
                "Profile not found or Instagram did not expose public profile data.",
                color = MaterialTheme.colorScheme.error,
            )
            InstagramProfileAnalyzer.Result.PrivateAccount -> Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Lock, null)
                    Spacer(Modifier.height(8.dp))
                    Text("Private account", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("This profile is private. yawr can’t analyze posts, Reels, followers, or other private content.")
                }
            }
            is InstagramProfileAnalyzer.Result.Profile -> {
                val p = result.data
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("@${p.username}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        p.displayName?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
                        p.bio?.let { Text(it) }
                        if (p.verified) Text("✓ Verified")
                        if (p.isBusiness) Text("Business account")
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Metric("Followers", p.followers)
                            Metric("Following", p.following)
                            Metric("Posts", p.posts)
                        }
                        Text("Public profile data only. Counts may be unavailable when Instagram does not expose them.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: Long?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value?.toString() ?: "—", fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}
