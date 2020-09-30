package com.mallich.musicplayer.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mallich.musicplayer.repositories.MusicRepository
import com.mallich.musicplayer.R
import com.mallich.musicplayer.databinding.SongsRowBinding
import com.mallich.musicplayer.interfaces.AllMusicInterface
import com.mallich.musicplayer.models.SongDataModel
import java.lang.NumberFormatException

class SingleAlbumAdapter(
    val context: Context,
    private val list: MutableList<SongDataModel>,
    private val allMusicInterface: AllMusicInterface
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

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val songDataModel = list[position]

        try {
            songDataModel.duration = MusicRepository.getTimeString(songDataModel.duration.toInt())
        } catch (e: NumberFormatException) {}

        holder.albumsRowBinding.data = songDataModel
        holder.albumsRowBinding.root.setOnClickListener {
            allMusicInterface.sendSelectedSongToPlay(context, position)
        }
        holder.albumsRowBinding.songsRowOptionsBtn.setOnClickListener {
            allMusicInterface.optionClicked(context, songDataModel, holder.albumsRowBinding.songsRowOptionsBtn)
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: SongsRowBinding) : RecyclerView.ViewHolder(itemView.root) {
        val albumsRowBinding: SongsRowBinding = itemView
    }

}