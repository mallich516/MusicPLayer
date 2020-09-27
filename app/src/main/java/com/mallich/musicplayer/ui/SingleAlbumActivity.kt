package com.mallich.musicplayer.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chinodev.androidneomorphframelayout.NeomorphFrameLayout
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.R
import com.mallich.musicplayer.adapters.SingleAlbumAdapter
import com.mallich.musicplayer.data.MusicViewModel
import com.mallich.musicplayer.interfaces.AllMusicInterface

class SingleAlbumActivity : AppCompatActivity() , AllMusicInterface {

    companion object {
        var FROM_ARTIST: Boolean = false
    }
    private lateinit var album: String
    private lateinit var albumArt: String
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var neomorphFrameLayout: NeomorphFrameLayout
    private lateinit var musicViewModel: MusicViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_album)

        imageView = findViewById(R.id.single_album_image)
        textView = findViewById(R.id.single_album_title)
        recyclerView = findViewById(R.id.single_album_recyclerView)
        neomorphFrameLayout = findViewById(R.id.single_album_backBtn)

        // Data From Intent
        album = intent.getStringExtra(MusicRepository.ALBUM)!!
        albumArt = intent.getStringExtra(MusicRepository.ALBUM_ART)!!

        musicViewModel = ViewModelProviders.of(this).get(MusicViewModel::class.java)

        textView.text = album
        Glide.with(imageView)
            .load(albumArt)
            .error(R.drawable.music_logo)
            .into(imageView)

        recyclerView.layoutManager = LinearLayoutManager(this)

        if (FROM_ARTIST) {
            recyclerView.adapter =
                SingleAlbumAdapter(
                    this,
                    MusicRepository.getSelectedArtistAlbums(applicationContext, album),
                    this
                )

        } else {
            recyclerView.adapter =
                SingleAlbumAdapter(
                    this,
                    MusicRepository.getSingleAlbum(application, album),
                    this
                )

        }

        neomorphFrameLayout.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun sendSelectedSongToPlay(context: Context, position: Int) {
        MusicRepository.checkIfMediaPlayerIsNull(context)
        MusicRepository.album = album
        MusicRepository.songPosition = position
        MusicRepository.albumType = MusicRepository.SINGLE_ALBUM
        MusicRepository.SELECTED_SONG = true
        MusicRepository.getSelectedPlayList(application)
        MusicRepository.updateSongInDataStore(musicViewModel)
        context.startActivity(Intent(context, MusicPlayerActivity::class.java))
    }

}