package com.mallich.musicplayer

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.mallich.musicplayer.data.MusicViewModel
import com.mallich.musicplayer.ui.MainActivity
import com.mallich.musicplayer.ui.MusicPlayerActivity

class MusicService : Service() {

    companion object {
        private lateinit var notificationLayout: RemoteViews
        private const val NOTIFICATION_CHANNEL_ID = "Music"
        private const val NOTIFICATION_ID = 1
        const val PREV_BTN = "prevBtn"
        const val PLAY_BTN = "playBtn"
        const val NEXT_PLAY = "nextBtn"

        private var SONG_POSITION: Int = 0
        private var ALBUM_TYPE: String = ""
        private var ALBUM: String = ""

        fun createNotificationChannel(context: Context, title: String, album: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel =
                    NotificationChannel(NOTIFICATION_CHANNEL_ID, title, importance).apply {
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

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            SONG_POSITION = MusicPlayerActivity.songPosition
            ALBUM_TYPE = MusicPlayerActivity.albumType
            ALBUM = MusicPlayerActivity.album

            notificationLayout =
                RemoteViews(context.packageName, R.layout.notification_layout)
            val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            notificationLayout.setImageViewUri(
                R.id.notification_image,
                Uri.parse(MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].albumArt)
            )
            notificationLayout.setTextViewText(R.id.notification_song_title, title)
            notificationLayout.setTextViewText(R.id.notification_song_album, album)

            notificationLayout.setImageViewResource(R.id.notification_prevBtn, R.drawable.prev_icon)

            if (MusicPlayerActivity.mediaPlayer!!.isPlaying) {
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
            prevIntent.action = PREV_BTN
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
            playIntent.action = PLAY_BTN
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
            nextIntent.action = NEXT_PLAY
            notificationLayout.setOnClickPendingIntent(
                R.id.notification_nextBtn,
                PendingIntent.getBroadcast(
                    context,
                    0,
                    nextIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )

            val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.music_logo)
                .setContentTitle(album)
                .setAutoCancel(true)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentText(title)
                .setContentIntent(pendingIntent)
                .setCustomContentView(notificationLayout).build()

            notification.flags = Notification.FLAG_ONGOING_EVENT

            return notification
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val title = intent?.getStringExtra("name")
        val album = intent?.getStringExtra("album")

        println("POSITION : $title $album ${MusicPlayerActivity.songPosition} ${MusicPlayerActivity.playList.size}")
        createNotificationChannel(this, title!!, album!!)
        startForeground(NOTIFICATION_ID, notificationBuilder(this, title, album))
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
