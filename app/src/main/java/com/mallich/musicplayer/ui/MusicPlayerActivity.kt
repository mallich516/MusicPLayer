package com.mallich.musicplayer.ui

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.*
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.chinodev.androidneomorphframelayout.NeomorphFrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.makeramen.roundedimageview.RoundedImageView
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.R
import com.mallich.musicplayer.data.MusicViewModel
import com.mallich.musicplayer.interfaces.AllMusicInterface
import java.lang.Exception

class MusicPlayerActivity : AppCompatActivity() {

    companion object {
        lateinit var imageView: RoundedImageView
        lateinit var titleTextView: TextView
        lateinit var startTime: TextView
        lateinit var endTime: TextView
        lateinit var seekBar: SeekBar
        lateinit var prevBtn: ImageView
        lateinit var nextBtn: ImageView
        lateinit var playBtn: FloatingActionButton
        var mediaPlayer: MediaPlayer? = null
    }

    private lateinit var audioManager: AudioManager
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var neomorphFrameLayout: NeomorphFrameLayout
    private lateinit var viewPager: ViewPager
    private lateinit var musicViewModel: MusicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        initView()

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        volumeSeekBar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        volumeSeekBar.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        musicViewModel = ViewModelProviders.of(this).get(MusicViewModel::class.java)

        ifSongSelected()

        musicViewModel.currentSongDetails.observe(this, { songDetails ->
            MusicRepository.songTitle = songDetails[0]
            MusicRepository.album = songDetails[1]
            MusicRepository.artist = songDetails[2]
            MusicRepository.songPosition = songDetails[3].toInt()
            MusicRepository.albumType = songDetails[4]
            MusicRepository.albumArt = songDetails[5]
            updateTitleAndImage()
            updateSeekBar()
        })

        musicViewModel.songStatus.observe(this, { songStatus ->
            MusicRepository.SONG_STATUS = songStatus
            updatePlayBtnImage()
            updateSeekBar()
        })

        clickListeners()

    }

    private fun updateTitleAndImage() {
        titleTextView.text = MusicRepository.songTitle
        Glide.with(this)
            .load(MusicRepository.albumArt)
            .error(R.drawable.music_logo).into(
                imageView
            )
    }

    private fun updatePlayBtnImage() {
        if (MusicRepository.SONG_STATUS == MusicRepository.SONG_PLAY) {
            playBtn.setImageResource(R.drawable.pause_icon)
        } else {
            playBtn.setImageResource(R.drawable.play_icon)
        }
    }

    private fun updateSeekBar() {
        Handler().postDelayed(object : Runnable {
            override fun run() {
                try {
                    seekBar.progress =
                        mediaPlayer!!.currentPosition
                    val currentTime =
                        MusicRepository.getTimeString(mediaPlayer!!.currentPosition)
                    seekBar.max = mediaPlayer!!.duration
                    endTime.text =
                        MusicRepository.getTimeString(mediaPlayer!!.duration)
                    startTime.text = currentTime
                    Handler().postDelayed(this, 1000)
                } catch (e: Exception) {
                    seekBar.progress = 0
                }
            }
        }, 0)
    }


    private fun ifSongSelected() {
        if (MusicRepository.SELECTED_SONG) {
            MusicRepository.SONG_STATUS = MusicRepository.SONG_PAUSE
            musicViewModel.updateSongStatus()
            MusicRepository.playMusic(application)
        }
    }

    private fun clickListeners() {
        playBtn.setOnClickListener {
            MusicRepository.checkIfMediaPlayerIsNull(application)
            musicViewModel.updateSongStatus()
        }

        prevBtn.setOnClickListener {
            MusicRepository.checkIfMediaPlayerIsNull(application)
            MusicRepository.decrementSongPosition()
            MusicRepository.updateSongInDataStore(musicViewModel)
            MusicRepository.playMusic(this)
        }

        nextBtn.setOnClickListener {
            MusicRepository.checkIfMediaPlayerIsNull(application)
            MusicRepository.incrementSongPosition()
            MusicRepository.updateSongInDataStore(musicViewModel)
            MusicRepository.playMusic(this)
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
            MusicRepository.songPosition += 1
            if (MusicRepository.songPosition < MusicRepository.playList.size) {
                MusicRepository.updateSongInDataStore(musicViewModel)
                MusicRepository.playMusic(application)
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
        MusicRepository.SELECTED_SONG = false
    }

    override fun onBackPressed() {
        if (MusicRepository.albumType == MusicRepository.SINGLE_ALBUM) {
            val intent= Intent(this, SingleAlbumActivity::class.java)
            intent.putExtra(MusicRepository.ALBUM, MusicRepository.album)
            intent.putExtra(MusicRepository.ALBUM_ART, MusicRepository.albumArt)
            startActivity(intent)
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}