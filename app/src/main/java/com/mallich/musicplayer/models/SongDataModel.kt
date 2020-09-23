package com.mallich.musicplayer.models

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.mallich.musicplayer.R

data class SongDataModel(
    val id: Long,
    val name: String,
    val album: String,
    val artist: String,
    val filePath: String,
    val fileData: String,
    val albumArt: String,
    var duration: String
) {
    companion object {
        @BindingAdapter("android:setImage")
        @JvmStatic
        fun setImage(imageView: ImageView, imageUrl: String?): Unit {
            Glide.with(imageView)
                .load(imageUrl)
                .error(R.drawable.music_logo)
                .into(imageView)
        }
    }
}