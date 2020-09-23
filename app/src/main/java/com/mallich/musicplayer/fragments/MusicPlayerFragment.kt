package com.mallich.musicplayer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.mallich.musicplayer.R

class MusicPlayerFragment : Fragment() {

    private lateinit var imageView: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_music_player, container, false)

        imageView = view.findViewById(R.id.player_viewpager_row_image)

        val image = arguments?.getString("image")
        Glide.with(imageView).load(image).error(R.drawable.music_logo).into(imageView)
        val album = arguments?.getString("album")
        val position = arguments?.getInt("position")
        return view
    }
}