package ch.noah.soundboard

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import ch.noah.soundboard.composables.MainScreen
import ch.noah.soundboard.ui.theme.SoundboadTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val deepLinkUri: Uri? = intent?.data

		enableEdgeToEdge()
		setContent {
			SoundboadTheme {
				Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
					MainScreen(
						deepLinkUri = deepLinkUri,
						modifier = Modifier
					)
				}
			}
		}
	}
}

