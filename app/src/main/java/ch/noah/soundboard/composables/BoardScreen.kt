package ch.noah.soundboard.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.noah.soundboard.ui.theme.SoundboadTheme

@Composable
fun BoardScreen(
	boardTitle: String,
	modifier: Modifier = Modifier
) {
	LazyVerticalGrid(
		columns = GridCells.Fixed(2),
		modifier = modifier.fillMaxSize(),
		contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
		horizontalArrangement = Arrangement.spacedBy(16.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		items(12) { index ->
			SoundItemPlaceholder(index = index)
		}
	}
}

@Composable
private fun SoundItemPlaceholder(
	index: Int,
	modifier: Modifier = Modifier
) {
	Card(
		modifier = modifier.aspectRatio(1f),
		elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.primaryContainer),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = "Item $index",
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onPrimaryContainer
			)
		}
	}
}

@Preview(showBackground = true)
@Composable
fun BoardScreenPreview() {
	SoundboadTheme {
		BoardScreen(boardTitle = "Sample Board")
	}
}
