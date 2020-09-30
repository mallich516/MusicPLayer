package com.mallich.musicplayer.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.mallich.musicplayer.R
import com.mallich.musicplayer.repositories.MusicRepository
import com.mallich.musicplayer.ui.SplashScreenActivity

class MusicService : Service() {


    private lateinit var notificationLayout: RemoteViews
    private val CHANNELID = "Music"
    private val NOTIFICATIONID = 1

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val title = intent?.getStringExtra("name")
        val album = intent?.getStringExtra("album")

        println("POSITION : $title $album ${MusicRepository.songPosition} ${MusicRepository.playList.size}")
        createNotificationChannel(this, title!!, album!!)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForeground(NOTIFICATIONID, notificationBuilder(this, title, album))

        return START_STICKY
    }

    private fun createNotificationChannel(context: Context, title: String, album: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel =
                NotificationChannel(CHANNELID, title, importance).apply {
                    description = album
                }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun notificationBuilder(
        context: Context,
        title: String,
        album: String
    ): Notification {
        val intent = Intent(context, SplashScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        notificationLayout =
            RemoteViews(context.packageName, R.layout.notification_layout)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        notificationLayout.setImageViewUri(
            R.id.notification_image,
            Uri.parse(MusicRepository.playList[MusicRepository.songPosition].albumArt)
        )
        notificationLayout.setTextViewText(R.id.notification_song_title, title)
        notificationLayout.setTextViewText(R.id.notification_song_album, album)

        notificationLayout.setImageViewResource(R.id.notification_prevBtn, R.drawable.prev_icon)

        if (MusicRepository.mediaPlayer!!.isPlaying) {
            notificationLayout.setImageViewResource(
                R.id.notification_playBtn,
                R.drawable.pause_icon
            )
        } else {
            notificationLayout.setImageViewResource(
                R.id.notification_playBtn,
                R.drawable.play_icon
            )
        }

        notificationLayout.setImageViewResource(R.id.notification_nextBtn, R.drawable.next_icon)
        val prevIntent = Intent(context, MusicReceiver::class.java)
        prevIntent.action = MusicRepository.PREV_BTN
        notificationLayout.setOnClickPendingIntent(
            R.id.notification_prevBtn,
            PendingIntent.getBroadcast(
                context,
                0,
                prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        val playIntent = Intent(context, MusicReceiver::class.java)
        playIntent.action = MusicRepository.PLAY_BTN
        notificationLayout.setOnClickPendingIntent(
            R.id.notification_playBtn,
            PendingIntent.getBroadcast(
                context,
                0,
                playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        val nextIntent = Intent(context, MusicReceiver::class.java)
        nextIntent.action = MusicRepository.NEXT_PLAY
        notificationLayout.setOnClickPendingIntent(
            R.id.notification_nextBtn,
            PendingIntent.getBroadcast(
                context,
                0,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

        val notification = NotificationCompat.Builder(context, CHANNELID)
            .setSmallIcon(R.drawable.music_logo)
            .setContentTitle(album)
            .setAutoCancel(true)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentText(title)
            .setContentIntent(pendingIntent)
            .setCustomContentView(notificationLayout).build()
        notification.flags = Notification.FLAG_ONGOING_EVENT and Notification.FLAG_NO_CLEAR

        return notification
    }
}
