package com.mechanize

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class Notifications {
    companion object{
        private const val CHANNEL_ID = "notifications"
        private var notificationManager: NotificationManager? = null

        fun init(context: Context){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val channel = NotificationChannel(CHANNEL_ID, context.getString(R.string.notifications_channel_name), NotificationManager.IMPORTANCE_HIGH).apply{ description = context.getString(R.string.notifications_channel_description) }

                notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                notificationManager?.createNotificationChannel(channel)
            } else notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }

        fun create(context: Context, title: String, message: String){
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)

            builder.setContentTitle(title)
            builder.setSmallIcon(R.drawable.baseline_directions_car_24)
            builder.setLargeIcon(LogoLibrary.decoder())
            builder.priority = NotificationCompat.PRIORITY_HIGH

            val style = NotificationCompat.InboxStyle()

            style.addLine(message)
            builder.setStyle(style)

            val intent = Intent(context, context.javaClass)

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.setContentIntent(pendingIntent)

            val notification = builder.build()

            notification.flags = Notification.FLAG_AUTO_CANCEL

            notificationManager?.notify(R.drawable.baseline_directions_car_24, notification)
        }
    }
}
