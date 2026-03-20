package ch.noah.soundboard.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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


	//val soundBoards = viewModel.soundBoards

	HorizontalPager(
		state = pagerState,
		modifier = modifier.fillMaxSize()
	) { page ->
		BoardScreen(boardTitle = boardTitles[page])
	}
}


@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
	SoundboadTheme {
		MainScreen()
	}
}
