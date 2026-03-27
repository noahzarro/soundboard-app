package ch.noah.soundboard.composables

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.noah.soundboard.data.ViewState
import ch.noah.soundboard.logic.MainViewModel
import ch.noah.soundboard.logic.MainViewModelFactory

@Composable
fun AddScreen(
	viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(LocalContext.current)),
) {
	val addScreenViewState by viewModel.addScreenViewState.collectAsState()
	var urlText by remember { mutableStateOf("") }

	Column(
		modifier = Modifier.fillMaxSize()
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.primaryContainer)
				.statusBarsPadding(),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = "Add Soundboard",
				style = MaterialTheme.typography.headlineMedium,
				color = MaterialTheme.colorScheme.onPrimaryContainer,
				textAlign = TextAlign.Center,
				modifier = Modifier.padding(16.dp)
			)
		}

		Box(
			modifier = Modifier
				.fillMaxSize()
				.padding(32.dp),
			contentAlignment = Alignment.Center
		) {
			when (addScreenViewState) {
				is ViewState.Success, is ViewState.Error -> {
					Column(
						modifier = Modifier.fillMaxWidth(),
						horizontalAlignment = Alignment.CenterHorizontally,
						verticalArrangement = Arrangement.Center
					) {

						OutlinedTextField(
							value = urlText,
							onValueChange = { urlText = it },
							label = { Text("Soundboard URL") },
							placeholder = { Text("https://example.com/soundboard.json") },
							modifier = Modifier.fillMaxWidth(),
							singleLine = true,
							isError = addScreenViewState is ViewState.Error
						)

						if (addScreenViewState is ViewState.Error) {
							Spacer(modifier = Modifier.height(8.dp))
							Text(
								text = (addScreenViewState as ViewState.Error).message,
								color = MaterialTheme.colorScheme.error,
								style = MaterialTheme.typography.bodyMedium,
								textAlign = TextAlign.Center
							)
						}

						Spacer(modifier = Modifier.height(32.dp))

						FloatingActionButton(
							onClick = {
								if (urlText.isNotBlank()) {
									viewModel.addSoundboard(Uri.parse(urlText.trim()))
								}
							},
							modifier = Modifier.size(72.dp)
						) {

							Icon(
								imageVector = Icons.Default.Add,
								contentDescription = "Add soundboard",
								modifier = Modifier.size(36.dp)
							)
						}
					}
				}
				is ViewState.Loading -> {
					Column(
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						CircularProgressIndicator()
						Text(
							text = "Loading soundboard...",
							style = MaterialTheme.typography.bodyLarge,
							modifier = Modifier.padding(top = 16.dp)
						)
					}
				}
			}
		}
	}
}
