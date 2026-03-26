package ch.noah.soundboard.composables

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.noah.soundboard.data.ViewState
import ch.noah.soundboard.database.SoundItems
import ch.noah.soundboard.logic.BoardViewModel
import ch.noah.soundboard.logic.BoardViewModelFactory
import ch.noah.soundboard.logic.MainViewModel
import ch.noah.soundboard.logic.MainViewModelFactory
import ch.noah.soundboard.storage.FileStorageRepository
import ch.noah.soundboard.ui.theme.SoundboadTheme
import coil3.compose.AsyncImage
import java.io.File

@Composable
fun BoardScreen(
	soundBoardId: String,
	modifier: Modifier = Modifier,
	viewModel: MainViewModel = viewModel(factory = MainViewModelFactory(LocalContext.current)),
) {
	val viewModel: BoardViewModel = viewModel(
		key = soundBoardId,
		factory = BoardViewModelFactory(LocalContext.current, soundBoardId)
	)
	val soundBoardItemsState by viewModel.soundBoardItems.collectAsState()
	val soundBoardName by viewModel.soundBoardName.collectAsState()

	when (soundBoardItemsState) {
		is ViewState.Loading -> {
			BoardLoadingScreen(modifier = modifier)
		}
		is ViewState.Error -> {
			BoardErrorScreen(
				message = (soundBoardItemsState as ViewState.Error).message,
				modifier = modifier
			)
		}
		is ViewState.Success, is ViewState.SilentLoading, is ViewState.SilentError -> {
			val items = soundBoardItemsState.dataOrNull() ?: emptyList()
			val soundBoardTitle = soundBoardName
			if (items.isEmpty() || soundBoardTitle == null) {
				BoardEmptyScreen(modifier = modifier)
			} else {
				val context = LocalContext.current
				val fileStorageRepository = remember { FileStorageRepository(context) }
				Column(modifier = Modifier.fillMaxSize()) {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.background(MaterialTheme.colorScheme.primaryContainer)
							.statusBarsPadding(),
						verticalAlignment = Alignment.CenterVertically
					) {
						IconButton(
							onClick = {
								Toast.makeText(context, "Delete soundboard not implemented yet", Toast.LENGTH_SHORT).show()
							}
						) {
							Icon(
								imageVector = Icons.Default.Delete,
								contentDescription = "Delete soundboard",
								modifier = Modifier
									.padding(start = 16.dp)
									.size(24.dp)
							)
						}
						Text(
							text = soundBoardTitle,
							style = MaterialTheme.typography.headlineMedium,
							color = MaterialTheme.colorScheme.onPrimaryContainer,
							textAlign = TextAlign.Center,
							modifier = Modifier
								.padding(16.dp)
								.weight(1f)
						)
						Icon(
							imageVector = Icons.Default.Share,
							contentDescription = "Share soundboard",
							modifier = Modifier
								.padding(end = 16.dp)
								.size(24.dp)
						)
					}

					BoardGrid(
						items = items,
						fileStorageRepository = fileStorageRepository,
						onItemClick = { index -> viewModel.playSound(items[index]) },
						modifier = modifier,
					)
				}
				if (soundBoardItemsState is ViewState.SilentError) {
					Toast.makeText(
						LocalContext.current,
						(soundBoardItemsState as ViewState.SilentError).message,
						Toast.LENGTH_LONG
					).show()
				}
			}
		}
	}
}

@Composable
private fun BoardGrid(
	items: List<SoundItems>,
	fileStorageRepository: FileStorageRepository,
	onItemClick: (Int) -> Unit,
	modifier: Modifier = Modifier,
) {
	LazyVerticalGrid(
		columns = GridCells.Fixed(2),
		modifier = modifier
			.fillMaxSize()
			.navigationBarsPadding(),
		contentPadding = PaddingValues(16.dp),
		horizontalArrangement = Arrangement.spacedBy(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		items(items.size) { index ->
			SoundItem(
				item = items[index],
				onClick = { onItemClick(index) },
				fileStorageRepository = fileStorageRepository,
			)
		}
	}
}

@Composable
private fun SoundItem(
	item: SoundItems,
	fileStorageRepository: FileStorageRepository,
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
) {

	val imagePath = remember(item.id, item.imageFile) {
		fileStorageRepository.getImagePath(item.id, item.imageFile)
	}

	Card(
		elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.clickable(onClick = onClick),
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			AsyncImage(
				model = File(imagePath),
				contentDescription = item.name,
				modifier = Modifier
					.fillMaxWidth()
					.aspectRatio(1f),
				contentScale = ContentScale.Crop
			)
			Text(
				text = item.name,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onPrimaryContainer,
				textAlign = TextAlign.Center,
				modifier = Modifier.padding(8.dp)
			)
		}
	}
}

@Composable
private fun BoardLoadingScreen(modifier: Modifier = Modifier) {
	Box(
		modifier = modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			CircularProgressIndicator()
			Text(
				text = "Loading sounds...",
				style = MaterialTheme.typography.bodyLarge,
				modifier = Modifier.padding(top = 16.dp)
			)
		}
	}
}

@Composable
private fun BoardErrorScreen(
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
				text = "Error Loading Board",
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
private fun BoardEmptyScreen(modifier: Modifier = Modifier) {
	Box(
		modifier = modifier.fillMaxSize(),
		contentAlignment = Alignment.Center
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier.padding(16.dp)
		) {
			Text(
				text = "No sounds available",
				style = MaterialTheme.typography.headlineMedium
			)
			Text(
				text = "This board has no sounds yet",
				style = MaterialTheme.typography.bodyLarge,
				modifier = Modifier.padding(top = 8.dp)
			)
		}
	}
}

@Preview(showBackground = true)
@Composable
fun BoardLoadingScreenPreview() {
	SoundboadTheme {
		BoardLoadingScreen()
	}
}

@Preview(showBackground = true)
@Composable
fun BoardErrorScreenPreview() {
	SoundboadTheme {
		BoardErrorScreen(message = "Failed to load sounds. Please try again.")
	}
}

@Preview(showBackground = true)
@Composable
fun BoardEmptyScreenPreview() {
	SoundboadTheme {
		BoardEmptyScreen()
	}
}
