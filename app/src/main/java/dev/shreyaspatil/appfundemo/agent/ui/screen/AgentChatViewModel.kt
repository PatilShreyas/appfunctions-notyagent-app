package dev.shreyaspatil.appfundemo.agent.ui.screen

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.shreyaspatil.appfundemo.agent.NotyAgentExecutor
import dev.shreyaspatil.appfundemo.agent.NotyNote
import dev.shreyaspatil.appfundemo.agent.NotyRequest
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

class AgentChatViewModel(
    private val executor: NotyAgentExecutor
) : ViewModel() {

    // Holds the chat history
    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    init {
        _messages.add(
            ChatMessage(
                "Hi! I am your Noty Agent. Type 'show me my notes' to test the AppFunctions.",
                false
            )
        )
    }

    private var lastlyAddedNote: NotyNote? = null

    fun onSendMessage(messageText: String) {
        if (messageText.isBlank()) return

        viewModelScope.launch {
            _messages.add(ChatMessage(messageText, isUser = true))
            processMessage(messageText.lowercase().trim())
        }
    }

    private suspend fun processMessage(lowerCaseText: String) {
        when {
            lowerCaseText == "show me my notes" -> {
                // Call the AppFunction
                try {
                    // Compile-time safety!
                    val notes: List<NotyNote> = executor.execute(NotyRequest.ListNotes)

                    val message =
                        "These are your notes from NotyKT:\n" + notes.joinToString("\n-----------\n") { "-${it.title}: ${it.content}" }
                    _messages.add(ChatMessage(message, isUser = false))
                } catch (e: Exception) {
                    _messages.add(ChatMessage("Error: ${e.message}", isUser = false))
                }
            }

            lowerCaseText.startsWith("add a note about info of appfunctions") -> {
                // Call the AppFunction
                try {
                    // Compile-time safety!
                    val note = executor.execute<NotyNote>(
                        NotyRequest.CreateNote(
                            title = "Android AppFunctions",
                            content = """
                                    AppFunctions allow your Android app to share specific pieces of functionality that the system and various AI agents and assistants can discover and invoke. By defining these functions, you enable your app to provide services, data, and actions to the Android OS, allowing users to complete tasks through AI agents and system-level interactions.
                                    
                                    AppFunctions serve as the mobile equivalent of tools within the Model Context Protocol (MCP). While MCP traditionally standardizes how agents connect to server-side tools, AppFunctions provide the same mechanism for Android apps. This enables you to expose your app's capabilities as orchestratable "tools" that authorized apps (callers) can discover and execute to fulfill user intents. Callers must have the EXECUTE_APP_FUNCTIONS permission to discover and execute AppFunctions, and can include agents, apps, and AI assistants like Gemini.
                                    
                                    AppFunctions work with devices running Android 16 or higher.
                                """.trimIndent()
                        )
                    )
                    lastlyAddedNote = note
                    _messages.add(ChatMessage("Added!", isUser = false))
                } catch (e: Exception) {
                    _messages.add(ChatMessage("Error: ${e.message}", isUser = false))
                }
            }

            lowerCaseText.startsWith("make it short") -> {
                if (lastlyAddedNote != null) {
                    try {
                        // Compile-time safety!
                        val note = executor.execute<NotyNote>(
                            NotyRequest.EditNote(
                                noteId = lastlyAddedNote!!.id,
                                title = lastlyAddedNote!!.title,
                                content = lastlyAddedNote!!.content
                                    .split("\n")
                                    .dropLast(1)
                                    .joinToString("\n")
                            )
                        )
                        lastlyAddedNote = note
                        _messages.add(ChatMessage("Done", isUser = false))
                    } catch (e: Exception) {
                        _messages.add(ChatMessage("Error: ${e.message}", isUser = false))
                    }
                } else {
                    _messages.add(ChatMessage("Which note?", isUser = false))
                }
            }

            else -> {
                _messages.add(
                    ChatMessage(
                        "I'm a simple demo agent. I only understand 'show me my notes' right now.",
                        isUser = false
                    )
                )
            }
        }
    }
}