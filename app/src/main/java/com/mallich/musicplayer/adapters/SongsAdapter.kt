package com.mallich.musicplayer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mallich.musicplayer.MusicRepository
import com.mallich.musicplayer.interfaces.MusicInterface
import com.mallich.musicplayer.R
import com.mallich.musicplayer.databinding.SongsRowBinding
import com.mallich.musicplayer.models.SongDataModel
import com.mallich.musicplayer.ui.MainActivity
import kotlinx.android.synthetic.main.songs_row.view.*

class SongsAdapter(
    private val musicInterface: MusicInterface,
    private val context: Context,
    private val songsList: MutableList<SongDataModel>
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val songDataModel: SongDataModel = songsList[position]

        try {
            songDataModel.duration = MusicRepository.getTimeString(songDataModel.duration.toInt())
        } catch (e: NumberFormatException) {}
        holder.songsRowBinding.data = songDataModel

        holder.songsRowBinding.root.setOnClickListener {
            musicInterface.setMusic(context, position)
        }
    }

    override fun getItemCount(): Int {
        return songsList.size
    }

    class ViewHolder(itemView: SongsRowBinding) : RecyclerView.ViewHolder(itemView.root) {
        val songsRowBinding: SongsRowBinding = itemView
    }

}