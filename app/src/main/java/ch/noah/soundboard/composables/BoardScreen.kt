package ch.noah.soundboard.composables

import android.content.Intent
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.noah.soundboard.Constants
import ch.noah.soundboard.data.ViewStateWithData
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
	mainViewModel: MainViewModel = viewModel(factory = MainViewModelFactory(LocalContext.current)),
) {
	val viewModel: BoardViewModel = viewModel(
		key = soundBoardId,
		factory = BoardViewModelFactory(LocalContext.current, soundBoardId)
	)
	val soundBoardItemsState by viewModel.soundBoardItems.collectAsState()
	val soundBoard by viewModel.soundBoard.collectAsState()
	var showDeleteDialog by remember { mutableStateOf(false) }

	when (soundBoardItemsState) {
		is ViewStateWithData.Loading -> {
			BoardLoadingScreen(modifier = modifier)
		}
		is ViewStateWithData.Error -> {
			BoardErrorScreen(
				message = (soundBoardItemsState as ViewStateWithData.Error).message,
				modifier = modifier
			)
		}
		is ViewStateWithData.Success, is ViewStateWithData.SilentLoading, is ViewStateWithData.SilentError -> {
			val items = soundBoardItemsState.dataOrNull() ?: emptyList()
			val soundBoardTitle = soundBoard?.title
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
								showDeleteDialog = true
							},
							modifier = Modifier.padding(start = 16.dp)
						) {
							Icon(
								imageVector = Icons.Default.Delete,
								contentDescription = "Delete soundboard",
								modifier = Modifier
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
						IconButton(
							onClick = {
								val configUrl = soundBoard?.configUrl ?: return@IconButton
								val configUrlEncoded = configUrl.let { java.net.URLEncoder.encode(it, "UTF-8") }
								val deepLinkUri = "${Constants.DEEP_LINK_URL}$configUrlEncoded"
								val sharingText = "Hound, check out my soundboard:\n$deepLinkUri"
								val sendIntent = Intent().apply {
									action = Intent.ACTION_SEND
									putExtra(Intent.EXTRA_TEXT, sharingText)
									type = "text/plain"
								}
								val shareIntent = Intent.createChooser(sendIntent, null)
								context.startActivity(shareIntent)

							},
							modifier = Modifier.padding(end = 16.dp)
						) {
							Icon(
								imageVector = Icons.Default.Share,
								contentDescription = "Share soundboard",
								modifier = Modifier
									.size(24.dp)
							)
						}
					}

					BoardGrid(
						items = items,
						fileStorageRepository = fileStorageRepository,
						onItemClick = { index -> viewModel.playSound(items[index]) },
						modifier = modifier,
					)
				}

				if (showDeleteDialog) {
					AlertDialog(
						onDismissRequest = { showDeleteDialog = false },
						title = { Text("Delete Soundboard") },
						text = { Text("Are you sure you want to delete this soundboard? This action cannot be undone.") },
						confirmButton = {
							TextButton(
								onClick = {
									showDeleteDialog = false
									mainViewModel.deleteSoundboard(soundBoardId)
								}
							) {
								Text("Delete")
							}
						},
						dismissButton = {
							TextButton(
								onClick = { showDeleteDialog = false }
							) {
								Text("Cancel")
							}
						}
					)
				}

				if (soundBoardItemsState is ViewStateWithData.SilentError) {
					Toast.makeText(
						LocalContext.current,
						(soundBoardItemsState as ViewStateWithData.SilentError).message,
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
