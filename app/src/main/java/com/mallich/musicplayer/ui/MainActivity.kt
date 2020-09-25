package com.mallich.musicplayer.ui

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.mallich.musicplayer.*
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.data.MusicViewModel
import com.mallich.musicplayer.fragments.AlbumsFragment
import com.mallich.musicplayer.fragments.ArtistsFragment
import com.mallich.musicplayer.fragments.SongsFragment
import com.mallich.musicplayer.interfaces.AllMusicInterface
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), AllMusicInterface {

    companion object {
        @RequiresApi(Build.VERSION_CODES.R)
        fun getPlayList(application: Application) {
            if (MusicPlayerActivity.albumType == MusicRepository.SINGLE_ALBUM) {
                MusicPlayerActivity.playList.clear()
                MusicPlayerActivity.playList.addAll(MusicRepository.getSingleAlbum(application, MusicPlayerActivity.album))
            } else {
                MusicPlayerActivity.playList.clear()
                MusicPlayerActivity.playList.addAll(MusicRepository.getAllSongs(application))
            }
        }

        var ACTIVE: Boolean = false
        lateinit var viewPagerAdapter: MainViewPager
        lateinit var imageView: ImageView
        lateinit var titleTextView: TextView
        lateinit var artist: TextView
        lateinit var playBtn: ImageView
        lateinit var linearLayout: LinearLayout
        lateinit var progressBar: ProgressBar
    }

    private lateinit var musicViewModel: MusicViewModel

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        musicViewModel = ViewModelProviders.of(this).get(MusicViewModel::class.java)

        checkStoragePermission()

        playBtn.setOnClickListener {
            if (musicViewModel.songStatus.value == MusicRepository.SONG_PLAY) {
                musicViewModel.updateSongStatus(MusicRepository.SONG_PAUSE)
            } else {
                musicViewModel.updateSongStatus(MusicRepository.SONG_PLAY)
            }
            updateMainActivityPlayer()

            // Start Notification Service
            val intentService = Intent(this, MusicService::class.java)
            intentService.putExtra("name", MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].name)
            intentService.putExtra("album", MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].album)
            startForegroundService(intentService)

        }

        linearLayout.setOnClickListener {
            openMusicPlayer()
        }
    }

    private fun initViews() {
        imageView = findViewById(R.id.current_song_image)
        titleTextView = findViewById(R.id.current_song_title)
        artist = findViewById(R.id.current_song_artist)
        playBtn = findViewById(R.id.current_song_playBtn)
        linearLayout = findViewById(R.id.current_song_layout)
        progressBar = findViewById(R.id.current_song_progress)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun updateMainActivityPlayer() {
        if (MusicPlayerActivity.mediaPlayer != null) {
            continuePlaying()
        } else {
            startPlayingFromFirst()
        }
        updateMainSeekBarAndPlayBtn()
    }

    private fun openMusicPlayer() {
        MusicPlayerActivity.PLAYER_ON = true
        startActivity(Intent(this, MusicPlayerActivity::class.java))
    }

    fun updateSongDetailsInDataStore() {
        musicViewModel.updateCurrentSong(
            MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].name,
            MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].album,
            MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].artist,
            MusicPlayerActivity.songPosition,
            MusicPlayerActivity.albumType,
            MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].albumArt
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun startPlayingFromFirst() {
        MusicPlayerActivity.playList.clear()
        if (MusicPlayerActivity.albumType == MusicRepository.ALL_SONGS) {
            MusicPlayerActivity.playList.addAll(MusicRepository.getAllSongs(application))
        } else {
            MusicPlayerActivity.playList.addAll(
                MusicRepository.getSingleAlbum(
                    application,
                    MusicPlayerActivity.album
                )
            )
        }

        MusicPlayerActivity.mediaPlayer = MediaPlayer.create(
            this,
            Uri.parse(MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].fileData)
        )
        MusicPlayerActivity.mediaPlayer!!.start()
        MusicRepository.SONG_STATUS = MusicRepository.SONG_PLAY
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun continuePlaying() {
        if (MusicPlayerActivity.mediaPlayer != null) {
            if (MusicRepository.SONG_STATUS == MusicRepository.SONG_PLAY) {
                MusicPlayerActivity.mediaPlayer?.pause()
                MusicRepository.SONG_STATUS = MusicRepository.SONG_PAUSE
            } else {
                MusicPlayerActivity.mediaPlayer?.start()
                MusicRepository.SONG_STATUS = MusicRepository.SONG_PLAY
            }
        }
    }

    private fun updateMainSeekBarAndPlayBtn() {
        updatePlayBtnImage()

        Handler().postDelayed(object : Runnable {
            override fun run() {
                try {
                    progressBar.max = MusicPlayerActivity.mediaPlayer!!.duration / 1000
                    progressBar.progress =
                        MusicPlayerActivity.mediaPlayer!!.currentPosition / 1000
                } catch (e: Exception) {
                    progressBar.progress = 0
                }
                Handler().postDelayed(this, 1000)
            }
        }, 0)
    }

    private fun updatePlayBtnImage() {
        if (MusicRepository.SONG_STATUS == MusicRepository.SONG_PLAY) {
            playBtn.setImageResource(R.drawable.pause_icon)
        } else {
            playBtn.setImageResource(R.drawable.play_icon)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkStoragePermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            startApplication()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1
            )
        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun startApplication() {
        val viewPagerData = mutableListOf<String>()
        viewPagerData.add("Songs")
        viewPagerData.add("Albums")
        viewPagerData.add("Artists")


        viewPagerAdapter = MainViewPager(supportFragmentManager, viewPagerData)
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)

        if (MusicPlayerActivity.mediaPlayer == null) {
            musicViewModel.updateSongStatus(MusicRepository.SONG_PAUSE)
        }

        musicViewModel.currentSongDetails.observe(this, { songDetails ->
            titleTextView.text = songDetails[0]
            MusicPlayerActivity.album = songDetails[1]
            artist.text = songDetails[2]
            MusicPlayerActivity.songPosition = songDetails[3].toInt()
            MusicPlayerActivity.albumType = songDetails[4]
            Glide.with(this).load(songDetails[5]).error(R.drawable.music_logo).into(imageView)
        })

        musicViewModel.songStatus.observe(this, { songStatus ->
            MusicRepository.SONG_STATUS = songStatus
            updateMainSeekBarAndPlayBtn()
        })
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startApplication()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    class MainViewPager(fragmentManager: FragmentManager, private val viewPagerData: MutableList<String>) :
        FragmentStatePagerAdapter(fragmentManager) {

        override fun getCount(): Int {
            return viewPagerData.size
        }

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> {
                    return SongsFragment()
                }
                1 -> {
                    return AlbumsFragment()
                }
//                2 -> {
//                    return ArtistsFragment()
//                }
            }
            return SongsFragment()
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "Songs"
                1 -> return "Albums"
//                2 -> return "Artists"
            }
            return super.getPageTitle(position)
        }
    }

    override fun playMusic(context: Context, position: Int) {
        MusicPlayerActivity.songPosition = position
        MusicPlayerActivity.albumType = MusicRepository.ALL_SONGS
        MusicPlayerActivity.SELECTED_SONG = true
        MusicReceiver.updateSongInDataStore(context)
        context.startActivity(Intent(context, MusicPlayerActivity::class.java))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

    override fun onResume() {
        super.onResume()
        ACTIVE = true
    }

    override fun onPause() {
        super.onPause()
        ACTIVE = false
    }

}