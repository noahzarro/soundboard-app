package ch.noah.soundboard.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import ch.noah.soundboard.storage.FileStorageRepository
import ch.noah.soundboard.ui.theme.SoundboadTheme
import coil3.compose.AsyncImage
import java.io.File

@Composable
fun BoardScreen(
	soundBoardId: String,
	modifier: Modifier = Modifier,
) {
	val viewModel: BoardViewModel = viewModel(
		key = soundBoardId,
		factory = BoardViewModelFactory(LocalContext.current, soundBoardId)
	)
	val soundBoardItemsState by viewModel.soundBoardItems.collectAsState()

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
		is ViewState.Success, is ViewState.SilentLoading -> {
			val items = soundBoardItemsState.dataOrNull() ?: emptyList()
			if (items.isEmpty()) {
				BoardEmptyScreen(modifier = modifier)
			} else {
				val context = LocalContext.current
				val fileStorageRepository = remember { FileStorageRepository(context) }

				BoardGrid(
					items = items,
					fileStorageRepository = fileStorageRepository,
					onItemClick = { index -> viewModel.playSound(items[index]) },
					modifier = modifier,
				)
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
		modifier = modifier.fillMaxSize(),
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
		modifier = modifier
			.aspectRatio(1f)
			.clickable(onClick = onClick),
		elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
	) {
		Box(
			modifier = Modifier.fillMaxSize()
		) {
			AsyncImage(
				model = File(imagePath),
				contentDescription = item.name,
				modifier = Modifier.fillMaxSize(),
				contentScale = ContentScale.Crop
			)
			// Overlay text on the image
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)),
				contentAlignment = Alignment.Center
			) {
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
