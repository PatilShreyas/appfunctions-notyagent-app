package dev.shreyaspatil.appfundemo.agent

import android.app.appfunctions.AppFunctionException
import android.app.appfunctions.AppFunctionManager
import android.app.appfunctions.ExecuteAppFunctionRequest
import android.app.appfunctions.ExecuteAppFunctionResponse
import android.app.appsearch.GenericDocument
import android.content.Context
import android.os.CancellationSignal
import android.os.OutcomeReceiver
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// In Agent App
data class NotyNote(
    val id: String,
    val title: String,
    val content: String
)

/**
 * Extension to safely map the system's GenericDocument back to our Note model.
 */
fun GenericDocument.toNotyNote(): NotyNote {
    return NotyNote(
        id = getPropertyString("id") ?: "",
        title = getPropertyString("title") ?: "",
        // Remember: your Tool app used 'note' as the variable name
        content = getPropertyString("content") ?: ""
    )
}

sealed class NotyRequest(val functionName: String) {
    object ListNotes : NotyRequest("listNotes")

    data class CreateNote(
        val title: String,
        val content: String
    ) : NotyRequest("createNote")

    data class EditNote(
        val noteId: String,
        val title: String?,
        val content: String?
    ) : NotyRequest("editNote")

    data class DeleteNote(
        val noteId: String
    ) : NotyRequest("deleteNote")
}

class NotyAgentExecutor(context: Context) {
    private val appFunctionManager = context.getSystemService(AppFunctionManager::class.java)
    private val mainExecutor = context.mainExecutor

    private val targetPackageName = "dev.shreyaspatil.noty.composeapp"
    private val functionPrefix = "dev.shreyaspatil.noty.appfunctions.NotyAppFunctions"
    private val RESULT_KEY = "androidAppfunctionsReturnValue"

    /**
     * The single entry point for all typed requests.
     */
    suspend fun <T> execute(request: NotyRequest): T {
        val functionId = "$functionPrefix#${request.functionName}"

        val builder = ExecuteAppFunctionRequest.Builder(targetPackageName, functionId)

        // Type-safe parameter building based on the Request model
        val params = when (request) {
            is NotyRequest.CreateNote -> {
                GenericDocument.Builder<GenericDocument.Builder<*>>("app", "req", "CreateNote")
                    .setPropertyString("title", request.title)
                    .setPropertyString("content", request.content)
                    .build()
            }

            is NotyRequest.EditNote -> {
                GenericDocument.Builder<GenericDocument.Builder<*>>("app", "req", "EditNote")
                    .setPropertyString("noteId", request.noteId)
                    .setPropertyString("title", request.title)
                    .setPropertyString("content", request.content)
                    .build()
            }
            // ListNotes has no parameters, so we don't call setParameters
            else -> null
        }

        params?.let { builder.setParameters(it) }

        val executeAppFunctionRequest = builder.build()
        Log.d("NotyAgentExecutor", "Sending Request: $executeAppFunctionRequest")

        val response = runRawExecution(executeAppFunctionRequest)

        Log.d("NotyAgentExecutor", "Received Response: $response")

        // Type-safe response parsing
        return when (request) {
            is NotyRequest.ListNotes -> {
                val docs = response.getPropertyDocumentArray(RESULT_KEY)
                docs?.map { it.toNotyNote() } ?: emptyList<NotyNote>()
            }

            is NotyRequest.CreateNote -> {
                response.getPropertyDocumentArray(RESULT_KEY)?.first()?.toNotyNote()
            }

            is NotyRequest.EditNote -> {
                response.getPropertyDocumentArray(RESULT_KEY)?.first()?.toNotyNote()
            }

            else -> throw UnsupportedOperationException("Not implemented")
        } as T
    }

    private suspend fun runRawExecution(
        request: ExecuteAppFunctionRequest,
    ) = suspendCancellableCoroutine<GenericDocument> { cont ->
        val cancellationSignal = CancellationSignal()

        appFunctionManager.executeAppFunction(
            request,
            mainExecutor,
            cancellationSignal,
            object : OutcomeReceiver<ExecuteAppFunctionResponse, AppFunctionException> {

                override fun onResult(response: ExecuteAppFunctionResponse) {
                    cont.resume(response.resultDocument)
                }

                override fun onError(error: AppFunctionException) {
                    Log.e("NotyAgent", "Execution failed: ${error.message}")
                    cont.resumeWithException(error)
                }
            }
        )

        cont.invokeOnCancellation {
            cancellationSignal.cancel()
        }
    }
}