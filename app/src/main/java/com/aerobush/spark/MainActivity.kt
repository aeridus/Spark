package com.aerobush.spark

import android.Manifest
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aerobush.spark.data.ThemeMode
import com.aerobush.spark.ui.item.SparkTimeItemViewModel
import com.aerobush.spark.ui.theme.SparkTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        setContent {
            val viewModel: SparkTimeItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val themeUiState = viewModel.themeUiState.collectAsState()
            SparkTheme (themeMode = themeUiState.value.themeMode) {
                Spark(
                    modifier = Modifier
                )
            }
        }
    }
}

@Composable
fun Spark(
    modifier: Modifier = Modifier,
    viewModel: SparkTimeItemViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val themeUiState = viewModel.themeUiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var hasNotificationPermission = false
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasNotificationPermission = it }
    )

    val currentThemeMode = themeUiState.value.themeMode

    SparkPanel(
        currentThemeMode,
        onThemeModeClick = {
            coroutineScope.launch {
                viewModel.cycleThemeMode(currentThemeMode)
            }
        },
        uiState.value.dayThresholdHour,
        onHourDecrease = {
            var newValue = uiState.value.dayThresholdHour - 1
            if (newValue < 0) {
                newValue = 23
            }
            coroutineScope.launch {
                viewModel.updateDayThresholdHour(newValue)
            }
        },
        onHourIncrease = {
            var newValue = uiState.value.dayThresholdHour + 1
            if (newValue > 23) {
                newValue = 0
            }
            coroutineScope.launch {
                viewModel.updateDayThresholdHour(newValue)
            }
        },
        uiState.value.totalHours,
        uiState.value.totalMinutes,
        uiState.value.sparkChoices,
        onChoiceClick = {
            if (!hasNotificationPermission) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            coroutineScope.launch {
                viewModel.saveSparkTimeItem(it)
            }
        },
        modifier
    )
}

@Composable
fun SparkPanel(
    currentThemeMode: ThemeMode,
    onThemeModeClick: () -> Unit,
    dayThresholdHour: Int,
    onHourDecrease: () -> Unit,
    onHourIncrease: () -> Unit,
    totalHours: Int,
    totalMinutes: Int,
    sparkChoices: List<String>,
    onChoiceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.TopEnd,
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top,
            modifier = Modifier
                .height(64.dp)
        ) {
            SettingButtons(
                currentThemeMode,
                onThemeModeClick,
                dayThresholdHour,
                onHourDecrease,
                onHourIncrease
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            LastSparkTime(
                totalHours,
                totalMinutes
            )

            ChoiceButtonGrid(
                onChoiceClick
            )

            WeeklyMoods(
                sparkChoices
            )
        }
    }
}

@Composable
fun SettingButtons(
    currentThemeMode: ThemeMode,
    onThemeModeClick: () -> Unit,
    dayThresholdHour: Int,
    onHourDecrease: () -> Unit,
    onHourIncrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    @DrawableRes var themeModeIcon: Int = R.drawable.cpu
    @StringRes var themeModeIconDescription: Int = R.string.cpu
    when (currentThemeMode) {
        ThemeMode.DARK -> {
            themeModeIcon = R.drawable.moon
            themeModeIconDescription = R.string.moon
        }
        ThemeMode.LIGHT -> {
            themeModeIcon = R.drawable.sun
            themeModeIconDescription = R.string.sun
        }
        else -> { }
    }

    Button(
        onClick = {
            onThemeModeClick()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.onBackground,
            contentColor = colorScheme.background
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxHeight()
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Image(
            painter = painterResource(themeModeIcon),
            contentDescription = stringResource(themeModeIconDescription),
            colorFilter = ColorFilter.tint(colorScheme.background),
            modifier = Modifier
                .height(24.dp)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .width(32.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.sleeping),
            contentDescription = stringResource(R.string.sleeping),
            colorFilter = ColorFilter.tint(colorScheme.onBackground),
            modifier = Modifier
                .height(32.dp)
        )
    }

    Button(
        onClick = {
            onHourDecrease()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxHeight()
            .width(32.dp)
    ) {
        Text(
            text = "—",
            color = colorScheme.onBackground,
            modifier = Modifier
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .width(48.dp)
    ) {
        Text(
            text = "$dayThresholdHour:00",
            color = colorScheme.onBackground,
            modifier = Modifier
        )
    }

    Button(
        onClick = {
            onHourIncrease()
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxHeight()
            .width(32.dp)
    ) {
        Text(
            text = "+",
            color = colorScheme.onBackground,
            modifier = Modifier
        )
    }
}

@Composable
fun LastSparkTime(
    totalHours: Int,
    totalMinutes: Int,
    modifier: Modifier = Modifier
) {
    val phrase = if (totalHours >= 4) {
        stringResource(R.string.time_to_mood)
    }
    else if (totalHours >= 3) {
        stringResource(R.string.safe_to_mood)
    }
    else {
        stringResource(R.string.please_wait_before_mood)
    }

    // Pretty format for time since last Spark
    var timeSinceLastSpark = ""
    if (totalHours > 0) {
        timeSinceLastSpark += if (totalHours == 1) {
            "$totalHours ${stringResource(R.string.hour)} "
        }
        else {
            "$totalHours ${stringResource(R.string.hours)} "
        }
    }
    timeSinceLastSpark += if (totalMinutes == 1) {
        "$totalMinutes ${stringResource(R.string.minute)}"
    }
    else {
        "$totalMinutes ${stringResource(R.string.minutes)}"
    }

    Text(
        text = phrase,
        color = colorScheme.onBackground,
        modifier = modifier
            .padding(8.dp)
    )

    Text(
        text = stringResource(R.string.last_spark_time, timeSinceLastSpark),
        color = colorScheme.onBackground,
        modifier = modifier
            .padding(8.dp)
    )
}

@Composable
fun ChoiceButtonGrid(
    onChoiceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(R.string.select_your_mood),
        color = colorScheme.onBackground,
        modifier = Modifier
            .padding(8.dp)
    )

    var theme by remember { mutableStateOf("") }

    var choices = mutableListOf<String>()
    if (theme.isEmpty()) {
        choices = mutableListOf<String>(
            "desert",
            "ocean",
            "desert",
            "ocean",
            "desert",
            "ocean",
            "desert",
            "ocean",
            "desert"
        )
    } else {
        (1..9).forEach {
            choices.add("${theme}_${it}")
        }
        choices.add("back")
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
    ) {
        items(choices) { sparkChoice ->
            ChoiceButton(
                value = sparkChoice,
                onChoiceClick = {
                    if (sparkChoice == "back") {
                        theme = ""
                    }
                    else if (theme.isNotEmpty()) {
                        onChoiceClick(sparkChoice)
                        theme = ""
                    } else {
                        theme = sparkChoice
                    }
                }
            )
        }
    }
}

@Composable
fun ChoiceButton(
    value: String,
    onChoiceClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = {
            onChoiceClick(value)
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.onBackground,
            contentColor = colorScheme.background
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxHeight()
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)
    ) {
        Image(
            painter = painterResourceFromString(value),
            contentDescription = stringResourceFromString(value),
            colorFilter = ColorFilter.tint(colorScheme.background),
            modifier = Modifier
                .height(24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyMoods(
    sparkChoices: List<String>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(8.dp)
    ) {
        // Show selected Spark choices
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .border(BorderStroke(1.dp, colorScheme.onBackground))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
            )

            sparkChoices.forEach {
                Image(
                    painter = painterResourceFromString(it),
                    contentDescription = stringResourceFromString(it),
                    colorFilter = ColorFilter.tint(colorScheme.onBackground),
                    modifier = Modifier
                        .height(24.dp)
                        .padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Composable
fun painterResourceFromString(resourceName: String): Painter {
    val context = LocalContext.current
    val resources: Resources = context.resources
    val resourceId = resources.getIdentifier(resourceName, "drawable", context.packageName)

    return painterResource(id = resourceId)
}

@Composable
fun stringResourceFromString(resourceName: String): String {
    val context = LocalContext.current
    val resources: Resources = context.resources
    val resourceId = resources.getIdentifier(resourceName, "string", context.packageName)

    return stringResource(id = resourceId)
}

@Preview(showBackground = true)
@Composable
fun SparkPreviewLight() {
    SparkTheme {
        SparkPanel(
            currentThemeMode = ThemeMode.LIGHT,
            onThemeModeClick = {},
            dayThresholdHour = 20,
            onHourDecrease = {},
            onHourIncrease = {},
            totalHours = 2,
            totalMinutes = 5,
            sparkChoices = listOf<String>("desert_1", "desert_2"),
            onChoiceClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SparkPreviewDark() {
    SparkTheme(isSystemInDarkTheme = true) {
        SparkPanel(
            currentThemeMode = ThemeMode.DARK,
            onThemeModeClick = {},
            dayThresholdHour = 20,
            onHourDecrease = {},
            onHourIncrease = {},
            totalHours = 2,
            totalMinutes = 5,
            sparkChoices = listOf<String>("desert_1", "desert_2"),
            onChoiceClick = {}
        )
    }
}