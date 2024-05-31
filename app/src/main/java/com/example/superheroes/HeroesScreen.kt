package com.example.superheroes

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring.DampingRatioLowBouncy
import androidx.compose.animation.core.Spring.StiffnessVeryLow
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.superheroes.model.Hero
import com.example.superheroes.model.HeroesRepository
import com.example.superheroes.ui.theme.SuperheroesTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HeroesList(
    heroes: List<Hero>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    textState: String = ""
) {
    var heroesList by remember { mutableStateOf(heroes) }
    heroesList = if (textState != "") {
        heroes.filter {
            stringResource(id = it.nameRes).lowercase().contains(textState.lowercase()) ||
                    stringResource(id = it.descriptionRes).lowercase().contains(textState.lowercase())
        }
    } else {
        heroes
    }
    val visibleState = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(
            animationSpec = spring(dampingRatio = DampingRatioLowBouncy)
        ),
        exit = fadeOut(),
        modifier = modifier
    ) {
        LazyColumn(contentPadding = contentPadding) {
            itemsIndexed(heroesList, key = { _, hero -> hero.id }) { index, hero ->
                SwipeToDismissHeroItem(
                    hero = hero,
                    onDismiss = { dismissedHero ->
                        heroesList = heroesList.filter { it.id != dismissedHero.id }
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .animateEnterExit(
                            enter = slideInVertically(
                                animationSpec = spring(
                                    stiffness = StiffnessVeryLow,
                                    dampingRatio = DampingRatioLowBouncy
                                ),
                                initialOffsetY = { it * (index + 1) } // staggered entrance
                            )
                        )
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterialApi::class)
@Composable
fun SwipeToDismissHeroItem(
    hero: Hero,
    onDismiss: (Hero) -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberDismissState(
        confirmStateChange = {
            it == DismissValue.DismissedToEnd || it == DismissValue.DismissedToStart
        }
    )
    if (dismissState.isDismissed(DismissDirection.StartToEnd) || dismissState.isDismissed(DismissDirection.EndToStart)) {
        LaunchedEffect(key1 = hero) {
            delay(500L) // Delay for 1 second
            onDismiss(hero)
        }
    }
    SwipeToDismiss(
        state = dismissState,
        background = {
            val color = when (dismissState.dismissDirection) {
                DismissDirection.StartToEnd -> MaterialTheme.colorScheme.primary
                DismissDirection.EndToStart -> MaterialTheme.colorScheme.secondary
                null -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Deleting...",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
        },
        dismissContent = {
            HeroListItem(
                hero = hero,
                modifier = modifier
            )
        }
    )
}
@Composable
fun HeroListItem(
    hero: Hero,
    modifier: Modifier = Modifier
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .sizeIn(minHeight = 72.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(hero.nameRes),
                    style = MaterialTheme.typography.displaySmall
                )
                Text(
                    text = stringResource(hero.descriptionRes),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))

            ) {
                Image(
                    painter = painterResource(hero.imageRes),
                    contentDescription = null,
                    alignment = Alignment.TopCenter,
                    contentScale = ContentScale.FillWidth
                )
            }
        }
    }
}

@Preview("Light Theme")
@Preview("Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HeroPreview() {
    val hero = Hero(
        1,
        R.string.hero1,
        R.string.description1,
        R.drawable.android_superhero1
    )
    SuperheroesTheme {
        HeroListItem(hero = hero)
    }
}

@Preview("Heroes List")
@Composable
fun HeroesPreview() {
    SuperheroesTheme(darkTheme = false) {
        Surface (
            color = MaterialTheme.colorScheme.background
        ) {
            HeroesList(heroes = HeroesRepository.heroes)
        }
    }
}
