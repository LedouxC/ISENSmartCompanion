package fr.isen.clement.isensmartcompanion.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.clement.isensmartcompanion.R
import com.google.ai.client.generativeai.GenerativeModel
import fr.isen.clement.isensmartcompanion.database.AppDatabase
import fr.isen.clement.isensmartcompanion.models.Message
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color


@Composable
fun MainScreen(innerPadding: PaddingValues, db: AppDatabase) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = "AIzaSyCmm3cXA2TiLZagooAjGLE9_YwhNwTH0aw"
        )
    }

    var question by remember { mutableStateOf(TextFieldValue("")) }
    val messageDao = remember { db.messageDao() }
    var messages by remember { mutableStateOf<List<Message>>(listOf()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Image(
            painter = painterResource(id = R.drawable.isen_logo),
            contentDescription = context.getString(R.string.isen_logo),
            modifier = Modifier
                .size(180.dp)
                .align(Alignment.CenterHorizontally)
        )

        Text(
            text = "Welcome to ISEN Smart Companion!",
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp)
                .align(Alignment.CenterHorizontally)
        )


        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF5F5F5))
                .padding(16.dp),
        ) {
            items(messages) { msg ->
                ChatBubble(message = msg)
            }
        }

        MessageInputField(
            question = question,
            onTextChange = { question = it },
            onSendMessage = {
                if (question.text.isNotBlank()) {
                    val userMessage = Message(text = question.text, isUser = true)
                    messages = messages + userMessage

                    val conversationContext = messages.takeLast(5).joinToString("\n") { message ->
                        if (message.isUser) "User: ${message.text}" else "AI: ${message.text}"
                    }

                    val prompt = """
                        Current context:
                        $conversationContext
                        
                        New user input:
                        ${question.text}
                        
                        Continue the conversation (you are AI).
                    """.trimIndent()

                    question = TextFieldValue("")

                    coroutineScope.launch {
                        messageDao.insert(userMessage)
                        try {


                            val result = generativeModel.generateContent(prompt)
                            val aiMessage = Message(text = result.text?.trimEnd() ?: "", isUser = false)
                            messages = messages + aiMessage
                            messageDao.insert(aiMessage)
                        } catch (e: Exception) {
                            val errorMessage = Message(text = "Error : ${e.message}", isUser = false)
                            messages = messages + errorMessage
                            messageDao.insert(errorMessage)
                        }
                    }
                }
                Toast.makeText(context, "Sent!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun ChatBubble(message: Message) {
    val backgroundColor = if (message.isUser) Color(0xFF007AFF) else Color(0xFFD1D1D6)
    val textColor = if (message.isUser) Color.White else Color.Black

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Text(
            text = formatDate(message.timestamp),
            fontSize = 12.sp,
            color = colorResource(R.color.grey),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(backgroundColor)
                .padding(16.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 16.sp,
                color = textColor
            )
        }
    }
}

@Composable
fun MessageInputField(
    question: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
    onSendMessage: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colorResource(R.color.white))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = question,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (question.text.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.ask_question),
                            color = colorResource(R.color.grey),
                            fontSize = 18.sp
                        )
                    }
                    innerTextField()
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSendMessage,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(colorResource(R.color.red))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_forward),
                    contentDescription = "Send",
                    modifier = Modifier.size(30.dp),
                    tint = colorResource(R.color.white)
                )
            }
        }
    }
}


