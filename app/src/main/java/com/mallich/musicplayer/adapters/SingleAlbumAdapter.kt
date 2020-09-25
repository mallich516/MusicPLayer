package com.mallich.musicplayer.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mallich.musicplayer.MusicReceiver
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.R
import com.mallich.musicplayer.databinding.SongsRowBinding
import com.mallich.musicplayer.models.SongDataModel
import com.mallich.musicplayer.ui.MusicPlayerActivity
import java.lang.NumberFormatException

class SingleAlbumAdapter(
    val context: Context,
    private val list: MutableList<SongDataModel>,
    private val album: String
) :
    RecyclerView.Adapter<SingleAlbumAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: SongsRowBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.songs_row,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val songDataModel = list[position]

        try {
            songDataModel.duration = MusicRepository.getTimeString(songDataModel.duration.toInt())
        } catch (e: NumberFormatException) {
        }
        holder.albumsRowBinding.data = songDataModel

        holder.albumsRowBinding.root.setOnClickListener {
            MusicPlayerActivity.songPosition = position
            MusicPlayerActivity.albumType = MusicRepository.SINGLE_ALBUM
            MusicPlayerActivity.album = album
            MusicPlayerActivity.SELECTED_SONG = true
            MusicReceiver.updateSongInDataStore(context)
            context.startActivity(Intent(context, MusicPlayerActivity::class.java))
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: SongsRowBinding) : RecyclerView.ViewHolder(itemView.root) {
        val albumsRowBinding: SongsRowBinding = itemView
    }

}