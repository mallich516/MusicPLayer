package com.mallich.musicplayer.adapters

import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mallich.musicplayer.repositories.MusicRepository
import com.mallich.musicplayer.interfaces.AllMusicInterface
import com.mallich.musicplayer.R
import com.mallich.musicplayer.databinding.SongsRowBinding
import com.mallich.musicplayer.models.SongDataModel

class SongsAdapter(
    private val context: Context,
    private val songsList: MutableList<SongDataModel>,
    private val allMusicInterface: AllMusicInterface
) : RecyclerView.Adapter<SongsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowBinding: SongsRowBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.songs_row,
            parent,
            false
        )
        return ViewHolder(rowBinding)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val songDataModel: SongDataModel = songsList[position]

        try {
            songDataModel.duration = MusicRepository.getTimeString(songDataModel.duration.toInt())
        } catch (e: NumberFormatException) {}

        holder.songsRowBinding.data = songDataModel
        holder.songsRowBinding.root.setOnClickListener {
            allMusicInterface.sendSelectedSongToPlay(context, position)
        }

        holder.songsRowBinding.songsRowOptionsBtn.setOnClickListener {
            allMusicInterface.optionClicked(context, songDataModel, holder.songsRowBinding.songsRowOptionsBtn)
        }
    }

    override fun getItemCount(): Int {
        return songsList.size
    }

    class ViewHolder(itemView: SongsRowBinding) : RecyclerView.ViewHolder(itemView.root) {
        val songsRowBinding: SongsRowBinding = itemView
    }

}