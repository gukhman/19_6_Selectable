package com.example.a19_6_selectable

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestApp()
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun TestApp() {
    //сохраняем состояние экрана
    var currentScreen by rememberSaveable { mutableStateOf(Screen.MainMenu) }
    //сохраняем ответы пользователя
    var answers by rememberSaveable { mutableStateOf(mutableMapOf<Int, Set<Int>>()) }

    //Проходим по всем 5 экранам из enum class Screen
    when (currentScreen) {
        Screen.MainMenu -> MainMenuScreen { currentScreen = Screen.Question1 }

        Screen.Question1 -> QuestionScreen(
            question = "Какие два события произошли в 1492 году?",
            options = listOf(
                Option.TextOption("Открытие Америки Христофором Колумбом"),
                Option.TextOption("Завершение Реконкисты в Испании"),
                Option.TextOption("Начало Французской революции")
            ),
            onNext = { selectedAnswers ->
                answers[1] = selectedAnswers
                currentScreen = Screen.Question2
            }
        )

        Screen.Question2 -> QuestionScreen(
            question = "Какие из перечисленных стран входили в антигитлеровскую коалицию во время Второй мировой войны?",
            options = listOf(
                Option.TextOption("СССР"),
                Option.TextOption("Германия"),
                Option.TextOption("США")
            ),
            onNext = { selectedAnswers ->
                answers[2] = selectedAnswers
                currentScreen = Screen.Question3
            }
        )

        Screen.Question3 -> QuestionScreen(
            question = "Выберите изображение Петра 1",
            options = listOf(
                Option.ImageOption(R.drawable.alex_makedon),
                Option.ImageOption(R.drawable.petr1),
                Option.ImageOption(R.drawable.rasputin)
            ),
            onNext = { selectedAnswers ->
                answers[3] = selectedAnswers
                currentScreen = Screen.Results
            },
            isSingleChoice = true // Только один выбор
        )

        Screen.Results -> ResultsScreen(
            answers = answers,
            correctAnswers = mapOf(1 to setOf(0, 1), 2 to setOf(0, 2), 3 to setOf(1)),
            onRestart = {
                answers.clear()
                currentScreen = Screen.MainMenu
            }
        )
    }
}

@Composable
fun MainMenuScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Тест по истории", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onStart) {
            Text("Начать")
        }
    }
}

@Composable
fun QuestionScreen(
    question: String,
    options: List<Option>,
    onNext: (Set<Int>) -> Unit,
    isSingleChoice: Boolean = false
) {
    var selectedAnswers by rememberSaveable { mutableStateOf(setOf<Int>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(question, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        options.forEachIndexed { index, option ->
            OptionItem(
                option = option,
                isSelected = index in selectedAnswers,
                isSingleChoice = isSingleChoice,
                onSelectionChange = { isSelected ->
                    selectedAnswers =
                        if (isSingleChoice) setOf(index)
                        else if (isSelected) selectedAnswers + index
                        else selectedAnswers - index
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onNext(selectedAnswers) },
            enabled = selectedAnswers.isNotEmpty()
        ) {
            Text("Ответить")
        }
    }
}

@Composable
fun OptionItem(
    option: Option,
    isSelected: Boolean,
    isSingleChoice: Boolean,
    onSelectionChange: (Boolean) -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.background

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(8.dp)
            .toggleable(
                value = isSelected,
                onValueChange = onSelectionChange
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSingleChoice) {
            RadioButton(selected = isSelected, onClick = null)
        } else {
            Checkbox(checked = isSelected, onCheckedChange = null)
        }
        Spacer(modifier = Modifier.width(8.dp))
        when (option) {
            is Option.TextOption -> Text(option.text, style = MaterialTheme.typography.bodyLarge)
            is Option.ImageOption -> Image(
                painter = painterResource(option.imageResId),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
        }
    }
}

@Composable
fun ResultsScreen(
    answers: Map<Int, Set<Int>>,
    correctAnswers: Map<Int, Set<Int>>,
    onRestart: () -> Unit
) {
    val score = correctAnswers.count { (question, correctSet) ->
        correctSet == (answers[question] ?: emptySet<Int>())
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Ваш результат: $score из ${correctAnswers.size}",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRestart) {
            Text("В главное меню")
        }
    }
}


open class Option {
    data class TextOption(val text: String) : Option()
    data class ImageOption(val imageResId: Int) : Option()
}

enum class Screen {
    MainMenu, Question1, Question2, Question3, Results
}
