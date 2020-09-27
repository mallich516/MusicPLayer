package com.mallich.musicplayer.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.widget.*
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
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var imageView: ImageView
        lateinit var titleTextView: TextView
        lateinit var artist: TextView
        lateinit var playBtn: ImageView
        lateinit var progressBar: ProgressBar
    }

    private lateinit var viewPagerAdapter: MainViewPager
    private lateinit var linearLayout: LinearLayout
    private lateinit var musicViewModel: MusicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        musicViewModel = ViewModelProviders.of(this).get(MusicViewModel::class.java)
        checkStoragePermission()
    }

    private fun initViews() {
        imageView = findViewById(R.id.current_song_image)
        titleTextView = findViewById(R.id.current_song_title)
        artist = findViewById(R.id.current_song_artist)
        playBtn = findViewById(R.id.current_song_playBtn)
        linearLayout = findViewById(R.id.current_song_layout)
        progressBar = findViewById(R.id.current_song_progress)
    }

    private fun openMusicPlayer() {
        startActivity(Intent(this, MusicPlayerActivity::class.java))
    }

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

    private fun startApplication() {
        initViews()

        val viewPagerData = mutableListOf<String>()
        viewPagerData.add("Songs")
        viewPagerData.add("Albums")
        viewPagerData.add("Artists")

        viewPagerAdapter = MainViewPager(supportFragmentManager, viewPagerData)
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)

        if (MusicPlayerActivity.mediaPlayer == null) {
            MusicRepository.SONG_STATUS = MusicRepository.SONG_PLAY
            musicViewModel.updateSongStatus()
        }

        liveDataObservers()

        clickEvents()

    }

    private fun liveDataObservers() {
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
    }

    private fun updatePlayBtnImage() {
        if (MusicRepository.SONG_STATUS == MusicRepository.SONG_PAUSE) {
            playBtn.setImageResource(R.drawable.play_icon)
        } else {
            playBtn.setImageResource(R.drawable.pause_icon)
        }
    }

    private fun updateTitleAndImage() {
        titleTextView.text = MusicRepository.songTitle
        artist.text = MusicRepository.artist
        Glide.with(this)
            .load(MusicRepository.albumArt)
            .error(R.drawable.music_logo)
            .into(imageView)
    }

    private fun updateSeekBar() {
        Handler().postDelayed(object : Runnable {
            override fun run() {
                try {
                    progressBar.max =
                        MusicPlayerActivity.mediaPlayer!!.duration / 1000
                    progressBar.progress =
                        MusicPlayerActivity.mediaPlayer!!.currentPosition / 1000
                } catch (e: Exception) {
                    progressBar.progress = 0
                }
                Handler().postDelayed(this, 1000)
            }
        }, 0)
    }

    private fun clickEvents() {
        playBtn.setOnClickListener {
            MusicRepository.checkIfMediaPlayerIsNull(this)
            musicViewModel.updateSongStatus()
        }
        linearLayout.setOnClickListener {
            openMusicPlayer()
        }
    }


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

    class MainViewPager(
        fragmentManager: FragmentManager,
        private val viewPagerData: MutableList<String>
    ) :
        FragmentStatePagerAdapter(fragmentManager) {

        override fun getCount(): Int {
            return viewPagerData.size - 1
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

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }

}