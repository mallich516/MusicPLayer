package com.mallich.musicplayer.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.bumptech.glide.Glide
import com.mallich.musicplayer.*
import com.mallich.musicplayer.fragments.AlbumsFragment
import com.mallich.musicplayer.fragments.SongsFragment
import com.mallich.musicplayer.interfaces.MusicInterface
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MusicInterface {

    companion object {
        public lateinit var viewPagerAdapter: MainViewPager
        public lateinit var imageView: ImageView
        public lateinit var titleTextView: TextView
        public lateinit var artist: TextView
        public lateinit var playBtn: ImageView
        public lateinit var linearLayout: LinearLayout
        public lateinit var progressBar: ProgressBar
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkStoragePermission()

        imageView = findViewById(R.id.current_song_image)
        titleTextView = findViewById(R.id.current_song_title)
        artist = findViewById(R.id.current_song_artist)
        playBtn = findViewById(R.id.current_song_playBtn)
        linearLayout = findViewById(R.id.current_song_layout)
        progressBar = findViewById(R.id.current_song_progress)

        playBtn.setOnClickListener {
            if (MusicPlayerActivity.mediaPlayer != null) {
                if (MusicPlayerActivity.mediaPlayer!!.isPlaying) {
                    MusicPlayerActivity.mediaPlayer?.pause()
                    playBtn.setImageResource(R.drawable.play_icon)
                } else {
                    MusicPlayerActivity.mediaPlayer?.start()
                    playBtn.setImageResource(R.drawable.pause_icon)
                }
                if (MusicPlayerActivity.PLAYER_ON) {
                    MusicPlayerActivity.setSongDetails(this, MusicPlayerActivity.songPosition)
                } else {
                    updateMainActivityPlayer()
                }
            } else {
                MusicPlayerActivity.playList.clear()
                MusicPlayerActivity.playList.addAll(MusicRepository.getAllSongs(application))
                MusicPlayerActivity.songPosition = 0

                MusicPlayerActivity.mediaPlayer = MediaPlayer.create(
                    this,
                    Uri.parse(MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].fileData)
                )
                MusicPlayerActivity.mediaPlayer!!.start()
                updateMainActivityPlayer()
            }
        }

        linearLayout.setOnClickListener {
            MusicPlayerActivity.PLAYER_ON = true
            val intent = Intent(this, MusicPlayerActivity::class.java)
            intent.putExtra(MusicRepository.SONG_POSITION, MusicPlayerActivity.songPosition)
            intent.putExtra(MusicRepository.TYPE, MusicPlayerActivity.albumType)
            intent.putExtra(MusicRepository.ALBUM, MusicPlayerActivity.album)
            startActivity(intent)
        }

    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        if (MusicPlayerActivity.playList.size != 0) {
            updateMainActivityPlayer()
        }
    }

    private fun updateMainActivityPlayer() {
        titleTextView.text = MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].name
        artist.text = MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].artist
        Glide.with(this)
            .load(MusicPlayerActivity.playList[MusicPlayerActivity.songPosition].albumArt)
            .error(R.drawable.music_logo)
            .into(imageView)


        if (MusicPlayerActivity.mediaPlayer!!.isPlaying) {
            playBtn.setImageResource(R.drawable.pause_icon)
        } else {
            playBtn.setImageResource(R.drawable.play_icon)
        }

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

    private fun checkStoragePermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            viewPagerAdapter = MainViewPager(supportFragmentManager)
            viewPager.adapter = viewPagerAdapter
            tabLayout.setupWithViewPager(viewPager)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            viewPagerAdapter = MainViewPager(supportFragmentManager)
            viewPager.adapter = viewPagerAdapter
            tabLayout.setupWithViewPager(viewPager)
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    class MainViewPager(fragmentManager: FragmentManager) :
        FragmentStatePagerAdapter(fragmentManager) {

        override fun getCount(): Int {
            return 2
        }

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> {
                    return SongsFragment()
                }
                1 -> {
                    return AlbumsFragment()
                }
            }
            return SongsFragment()
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "Songs"
                1 -> return "Albums"
            }
            return super.getPageTitle(position)
        }
    }

    override fun setMusic(context: Context, position: Int) {
        val intent = Intent(context, MusicPlayerActivity::class.java)
        intent.putExtra(MusicRepository.SONG_POSITION, position)
        intent.putExtra(MusicRepository.TYPE, MusicRepository.ALL_SONGS)
        intent.putExtra(MusicRepository.ALBUM, MusicRepository.ALL_SONGS)
        context.startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        moveTaskToBack(true)
    }
}