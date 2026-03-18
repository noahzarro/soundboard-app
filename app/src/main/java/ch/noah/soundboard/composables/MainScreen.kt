package ch.noah.soundboard.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.noah.soundboard.logic.MainViewModel
import ch.noah.soundboard.logic.MainViewModelFactory
import ch.noah.soundboard.ui.theme.SoundboadTheme

@Composable
fun Greeting(
	name: String,
	modifier: Modifier = Modifier,
	viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(LocalContext.current)),
) {
	Column() {
		Text(
			text = "Hello $name!",
			modifier = modifier
		)
		Button(onClick = { viewModel.load() }) {
			Text("Load")
		}
	}
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
	SoundboadTheme {
		Greeting("Android")
	}
}