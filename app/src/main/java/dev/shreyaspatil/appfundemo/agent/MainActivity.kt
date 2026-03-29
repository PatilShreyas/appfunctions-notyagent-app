package dev.shreyaspatil.appfundemo.agent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.shreyaspatil.appfundemo.agent.ui.screen.AgentChatScreen
import dev.shreyaspatil.appfundemo.agent.ui.screen.AgentChatViewModel
import dev.shreyaspatil.appfundemo.agent.ui.theme.AppFunctionsDemoAgentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val executor = NotyAgentExecutor(this)
        val viewModel = AgentChatViewModel(executor)

        enableEdgeToEdge()
        setContent {
            AppFunctionsDemoAgentTheme {
                // Initialize your components
                AgentChatScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AppFunctionsDemoAgentTheme {
        Greeting("Android")
    }
}