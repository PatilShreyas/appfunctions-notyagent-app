package dev.shreyaspatil.appfundemo.agent.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentChatScreen(viewModel: AgentChatViewModel) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("Agent Chat") })
        }
    ) { innerPadding ->
        var inputText by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(innerPadding)
        ) {
            // Chat History Stream
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.messages) { message ->
                    ChatBubble(message = message)
                }
            }

            // Input Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask the agent...") },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        viewModel.onSendMessage(inputText)
                        inputText = "" // Clear input after sending
                    },
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(50)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    val backgroundColor = if (isUser) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0)
    val textColor = if (isUser) Color.White else Color.Black

    // Align user messages to the right, agent messages to the left
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isUser) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Text(
            text = message.text,
            color = textColor,
            modifier = Modifier
                .background(backgroundColor, shape)
                .padding(12.dp)
                .widthIn(max = 280.dp) // Prevent bubbles from stretching all the way across
        )
    }
}