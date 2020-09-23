package com.mallich.musicplayer.ui

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.chinodev.androidneomorphframelayout.NeomorphFrameLayout
import com.mallich.musicplayer.MusicRepository
import com.mallich.musicplayer.R
import com.mallich.musicplayer.adapters.SingleAlbumAdapter

class SingleAlbumActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var neomorphFrameLayout: NeomorphFrameLayout

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_album)

        imageView = findViewById(R.id.single_album_image)
        textView = findViewById(R.id.single_album_title)
        recyclerView = findViewById(R.id.single_album_recyclerView)
        neomorphFrameLayout = findViewById(R.id.single_album_backBtn)

        val album = intent.getStringExtra(MusicRepository.ALBUM)
        val albumArt = intent.getStringExtra(MusicRepository.ALBUM_ART)

        textView.text = album
        Glide.with(imageView)
            .load(albumArt)
            .error(R.drawable.music_logo)
            .into(imageView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter =
            SingleAlbumAdapter(this, MusicRepository.getSingleAlbum(application, album.toString()))

        neomorphFrameLayout.setOnClickListener {
            onBackPressed()
        }
    }

//    override fun onBackPressed() {
//        startActivity(Intent(this, MainActivity::class.java))
//    }

}