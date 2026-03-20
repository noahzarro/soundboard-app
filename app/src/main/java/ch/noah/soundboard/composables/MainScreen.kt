package ch.noah.soundboard.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
	modifier: Modifier = Modifier,
	viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(LocalContext.current)),
) {
	val boardTitles = listOf("Board 1", "Board 2", "Board 3", "Board 4")
	val pagerState = rememberPagerState(pageCount = { boardTitles.size })
	
	HorizontalPager(
		state = pagerState,
		modifier = modifier.fillMaxSize()
	) { page ->
		BoardScreen(boardTitle = boardTitles[page])
	}
}

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

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
	SoundboadTheme {
		MainScreen()
	}
}
