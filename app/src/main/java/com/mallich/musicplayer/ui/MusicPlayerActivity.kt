package com.mallich.musicplayer.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.chinodev.androidneomorphframelayout.NeomorphFrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.makeramen.roundedimageview.RoundedImageView
import com.mallich.musicplayer.MusicReceiver
import com.mallich.musicplayer.MusicService
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.R
import com.mallich.musicplayer.data.MusicDataStore
import com.mallich.musicplayer.data.MusicViewModel
import com.mallich.musicplayer.models.SongDataModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class MusicPlayerActivity : AppCompatActivity() {

    companion object {

        var ACTIVE: Boolean = false
        lateinit var imageView: RoundedImageView
        lateinit var titleTextView: TextView
        lateinit var startTime: TextView
        lateinit var endTime: TextView
        lateinit var seekBar: SeekBar
        lateinit var prevBtn: ImageView
        lateinit var nextBtn: ImageView
        lateinit var playBtn: FloatingActionButton
        lateinit var volumeSeekBar: SeekBar
        lateinit var neomorphFrameLayout: NeomorphFrameLayout
        lateinit var viewPager: ViewPager

        var PLAYER_ON: Boolean = false
        var SELECTED_SONG: Boolean = false

        var mediaPlayer: MediaPlayer? = null
        val handler = Handler()

        var songPosition = -1
        var albumType: String = ""
        var album: String = ""
        var newSongPosition = -1
        var newAlbumType: String = ""
        var newAlbum: String? = ""
        var playList: MutableList<SongDataModel> = mutableListOf()

        lateinit var audioManager: AudioManager

        private lateinit var musicViewModel: MusicViewModel

        @RequiresApi(Build.VERSION_CODES.R)
        fun setSongDetails() {
            if (MusicRepository.SONG_STATUS == MusicRepository.SONG_PLAY) {
                playBtn.setImageResource(R.drawable.pause_icon)
            } else {
                playBtn.setImageResource(R.drawable.play_icon)
            }
            if (mediaPlayer != null) {
                initializeSeekBar()
            }
        }

        @RequiresApi(Build.VERSION_CODES.R)
        fun playNewSong(context: Context) {
            val selectedSong =
                Uri.parse(playList[songPosition].fileData)
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, selectedSong)
            mediaPlayer?.start()
            musicViewModel.updateSongStatus(MusicRepository.SONG_PLAY)
            updateSongDetailsInDataStore(context)
        }

        @RequiresApi(Build.VERSION_CODES.R)
        fun playNextSong(context: Context) {
            val selectedSong =
                Uri.parse(playList[songPosition].fileData)
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, selectedSong)
            println("PLAY BTN STATUS ${MusicRepository.SONG_STATUS}")
            if (MusicRepository.SONG_STATUS == MusicRepository.SONG_PLAY) {
                mediaPlayer?.start()
                if (ACTIVE) {
                    playBtn.setImageResource(R.drawable.pause_icon)
                }
            } else {
                mediaPlayer?.start()
                mediaPlayer?.pause()
                if (ACTIVE) {
                    playBtn.setImageResource(R.drawable.play_icon)
                }
            }

            // Start Notification Service
            val intentService = Intent(context, MusicService::class.java)
            intentService.putExtra("name", playList[songPosition].name)
            intentService.putExtra("album", playList[songPosition].album)
            context.startForegroundService(intentService)
        }

        private fun initializeSeekBar() {
            seekBar.max = mediaPlayer!!.duration
            endTime.text = MusicRepository.getTimeString(mediaPlayer!!.duration)
            handler.postDelayed(object : Runnable {
                override fun run() {
                    try {
                        seekBar.progress = mediaPlayer!!.currentPosition
                        val currentTime =
                            MusicRepository.getTimeString(mediaPlayer!!.currentPosition)
                        startTime.text = currentTime
                        handler.postDelayed(this, 1000)
                    } catch (e: Exception) {
                        seekBar.progress = 0
                    }
                }
            }, 0)
        }


        @RequiresApi(Build.VERSION_CODES.R)
        fun loadSongToPlay(application: Application) {
            getPlayList(application)

//        val viewPagerAdapter = PlayerViewPagerAdapter(
//            application,
//            supportFragmentManager,
//            MusicPlayerActivity(),
//            playList
//        )
//        viewPager.adapter = viewPagerAdapter

            if (mediaPlayer == null) {
                playNextSong(application)
            }
        }

        @RequiresApi(Build.VERSION_CODES.R)
        fun getPlayList(application: Application) {
            if (albumType == MusicRepository.SINGLE_ALBUM) {
                playList.clear()
                playList.addAll(MusicRepository.getSingleAlbum(application, album))
            } else {
                playList.clear()
                playList.addAll(MusicRepository.getAllSongs(application))
            }
        }

        @RequiresApi(Build.VERSION_CODES.R)
        fun updateMusicPlayerActivityPlayer(application: Application) {

            if (mediaPlayer == null) {
                loadSongToPlay(application)
            }

            if (!mediaPlayer!!.isPlaying && mediaPlayer != null) {
                mediaPlayer?.start()
                MusicRepository.SONG_STATUS = MusicRepository.SONG_PLAY
            } else {
                mediaPlayer?.pause()
                MusicRepository.SONG_STATUS = MusicRepository.SONG_PAUSE
            }

            if (musicViewModel.songStatus.value == MusicRepository.SONG_PLAY) {
                musicViewModel.updateSongStatus(MusicRepository.SONG_PAUSE)
            } else {
                musicViewModel.updateSongStatus(MusicRepository.SONG_PLAY)
            }
        }

        fun updateSongDetailsInDataStore(context: Context) {
            if (MainActivity.ACTIVE) {
                MusicReceiver.updateSongInDataStore(context)
            } else if (ACTIVE) {
                musicViewModel.updateCurrentSong(
                    playList[songPosition].name,
                    playList[songPosition].album,
                    playList[songPosition].artist,
                    songPosition,
                    albumType,
                    playList[songPosition].albumArt
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        initView()

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        volumeSeekBar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        volumeSeekBar.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        musicViewModel = ViewModelProviders.of(this).get(MusicViewModel::class.java)

        if (SELECTED_SONG) {
            getPlayList(application)
            MusicRepository.SONG_STATUS = MusicRepository.SONG_PLAY
            musicViewModel.updateSongStatus(MusicRepository.SONG_STATUS)
            playNextSong(this)
        }

        musicViewModel.currentSongDetails.observe(this, { songDetails ->
            titleTextView.text = songDetails[0]
            album = songDetails[1]
            songPosition = songDetails[3].toInt()
            albumType = songDetails[4]
            Glide.with(this).load(songDetails[5]).error(R.drawable.music_logo).into(imageView)
            setSongDetails()
        })

        musicViewModel.songStatus.observe(this, { songStatus ->
            MusicRepository.SONG_STATUS = songStatus
            setSongDetails()
        })
        viewListeners()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun viewListeners() {

        playBtn.setOnClickListener {
            updateMusicPlayerActivityPlayer(application)
            val intentService = Intent(this, MusicService::class.java)
            intentService.putExtra("name", MusicPlayerActivity.playList[songPosition].name)
            intentService.putExtra("album", MusicPlayerActivity.playList[songPosition].album)
            startForegroundService(intentService)
        }

        prevBtn.setOnClickListener {
            songPosition -= 1
            if (songPosition < 0) {
                songPosition = playList.size - 1
                playNextSong(this)
            } else {
                getPlayList(application)
                playNextSong(this)
            }
            updateSongDetailsInDataStore(this)
        }

        nextBtn.setOnClickListener {
            songPosition += 1
            if (songPosition < playList.size) {
                playNextSong(this)
            } else {
                getPlayList(application)
                songPosition = 0
                playNextSong(this)
            }
            updateSongDetailsInDataStore(this)
        }

        neomorphFrameLayout.setOnClickListener {
            onBackPressed()
        }

        seekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, changed: Boolean) {
                    if (changed) {
                        mediaPlayer?.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }
            })

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, changed: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })

        mediaPlayer?.setOnCompletionListener { mp ->
            songPosition += 1
            if (songPosition < playList.size) {
                Toast.makeText(this, "Playing Next Song", Toast.LENGTH_SHORT).show()
                updateSongDetailsInDataStore(this)
                playNextSong(this@MusicPlayerActivity)
            } else {
                seekBar.progress = 0
                startTime.text = MusicRepository.getTimeString(0)
                endTime.text = MusicRepository.getTimeString(mp!!.duration)
            }
        }
    }

    private fun initView() {
        imageView = findViewById(R.id.player_img)
        titleTextView = findViewById(R.id.player_album)
        startTime = findViewById(R.id.player_startTime)
        endTime = findViewById(R.id.player_endTime)
        seekBar = findViewById(R.id.player_seekBar)
        prevBtn = findViewById(R.id.player_prevBtn)
        nextBtn = findViewById(R.id.player_nextBtn)
        playBtn = findViewById(R.id.player_playBtn)
        volumeSeekBar = findViewById(R.id.player__volume_seekBar)
        neomorphFrameLayout = findViewById(R.id.player_backBtn)
        viewPager = findViewById(R.id.player_viewpager)
    }

    override fun onPause() {
        super.onPause()
        SELECTED_SONG = false
        ACTIVE = false
    }

    override fun onResume() {
        super.onResume()
        ACTIVE = true
    }

    override fun onBackPressed() {
        if (albumType == MusicRepository.SINGLE_ALBUM) {
            super.onBackPressed()
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

}