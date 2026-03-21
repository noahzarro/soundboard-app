package ch.noah.soundboard.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.noah.soundboard.data.ViewState
import ch.noah.soundboard.logic.MainViewModel
import ch.noah.soundboard.logic.MainViewModelFactory
import ch.noah.soundboard.ui.theme.SoundboadTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
	modifier: Modifier = Modifier,
	viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(LocalContext.current)),
) {
	val soundBoardsState by viewModel.soundBoards.collectAsState()

	when (soundBoardsState) {
		is ViewState.Loading -> {
			LoadingScreen(modifier = modifier)
		}
		is ViewState.Error -> {
			ErrorScreen(
				message = (soundBoardsState as ViewState.Error).message,
				modifier = modifier
			)
		}
		is ViewState.Success, is ViewState.SilentLoading -> {
			val soundBoards = requireNotNull(soundBoardsState.dataOrNull())
			if (soundBoards.isEmpty()) {
				EmptyScreen(modifier = modifier)
			} else {
				val pagerState = rememberPagerState(pageCount = { soundBoards.size })

				HorizontalPager(
					state = pagerState,
					modifier = modifier.fillMaxSize()
				) { page ->
					BoardScreen(boardTitle = soundBoards[page].title, soundBoardId = soundBoards[page].id)
				}
			}
		}
	}
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
	Box(
		modifier = modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			CircularProgressIndicator()
			Text(
				text = "Loading soundboards...",
				style = MaterialTheme.typography.bodyLarge,
				modifier = Modifier.padding(top = 16.dp)
			)
		}
	}
}

@Composable
private fun ErrorScreen(
	message: String,
	modifier: Modifier = Modifier,
) {
	Box(
		modifier = modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.padding(16.dp)
		) {
			Text(
				text = "Error",
				style = MaterialTheme.typography.headlineMedium,
				color = MaterialTheme.colorScheme.error
			)
			Text(
				text = message,
				style = MaterialTheme.typography.bodyLarge,
				textAlign = TextAlign.Center,
				modifier = Modifier.padding(top = 8.dp)
			)
		}
	}
}

@Composable
private fun EmptyScreen(modifier: Modifier = Modifier) {
	Box(
		modifier = modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.padding(16.dp)
		) {
			Text(
				text = "No soundboards available",
				style = MaterialTheme.typography.headlineMedium
			)
			Text(
				text = "Add a soundboard to get started",
				style = MaterialTheme.typography.bodyLarge,
				modifier = Modifier.padding(top = 8.dp)
			)
		}
	}
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
	SoundboadTheme {
		MainScreen()
	}
}

@Preview(showBackground = true)
@Composable
fun LoadingScreenPreview() {
	SoundboadTheme {
		LoadingScreen()
	}
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
	SoundboadTheme {
		ErrorScreen(message = "Failed to load soundboards. Please check your internet connection.")
	}
}

@Preview(showBackground = true)
@Composable
fun EmptyScreenPreview() {
	SoundboadTheme {
		EmptyScreen()
	}
}
