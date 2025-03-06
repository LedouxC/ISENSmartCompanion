package fr.isen.giusiano.isensmartcompanion

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.isen.giusiano.isensmartcompanion.models.Event
import fr.isen.giusiano.isensmartcompanion.services.NotificationReceiver

class EventDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val event = intent.getParcelableExtra<Event>("event")

        setContent {
            if (event != null) {
                val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                EventDetailScreen(event, sharedPreferences)
            } else {
                Text(stringResource(id = R.string.event_not_found), modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
fun EventDetailScreen(event: Event, prefs: SharedPreferences) {
    var isNotified by remember { mutableStateOf(prefs.getBoolean(event.id, false)) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = event.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.black)
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        DetailCard(label = stringResource(id = R.string.date_label), value = event.date)
        DetailCard(label = stringResource(id = R.string.location_label), value = event.location)
        DetailCard(label = stringResource(id = R.string.category_label), value = event.category)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = event.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(16.dp)
                .background(colorResource(id = R.color.background), RoundedCornerShape(8.dp))
                .padding(16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isNotified) stringResource(id = R.string.notify_on) else stringResource(id = R.string.notify_off),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = isNotified,
                onCheckedChange = {
                    isNotified = it
                    prefs.edit().putBoolean(event.id, isNotified).apply()

                    if (isNotified) {
                        scheduleNotification(context, event)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = colorResource(id = R.color.red),
                    checkedBorderColor = colorResource(id = R.color.red),
                    uncheckedIconColor = colorResource(id = R.color.grey)
                )
            )
        }
    }
}

@Composable
fun DetailCard(label: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = label,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
               fontWeight = FontWeight.Normal
            )
        }
    }
}


fun scheduleNotification(context: Context, event: Event) {
    val intent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("event_title", event.title)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        event.id.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val triggerTime = SystemClock.elapsedRealtime() + 10 * 1000

    alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
}
