package com.mallich.musicplayer.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mallich.musicplayer.MusicRepository
import com.mallich.musicplayer.R
import com.mallich.musicplayer.databinding.AlbumsRowBinding
import com.mallich.musicplayer.databinding.SongsRowBinding
import com.mallich.musicplayer.models.SongDataModel
import com.mallich.musicplayer.ui.MusicPlayerActivity
import kotlinx.android.synthetic.main.songs_row.view.*
import java.lang.NumberFormatException

class SingleAlbumAdapter(val context: Context, private val list: MutableList<SongDataModel>) : RecyclerView.Adapter<SingleAlbumAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SingleAlbumAdapter.ViewHolder {
        val binding: SongsRowBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.songs_row, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SingleAlbumAdapter.ViewHolder, position: Int) {
        val songDataModel = list[position]

        try {
            songDataModel.duration = MusicRepository.getTimeString(songDataModel.duration.toInt())
        } catch (e: NumberFormatException){}
        holder.albumsRowBinding.data = songDataModel

        holder.albumsRowBinding.root.setOnClickListener {
            val intent= Intent(context, MusicPlayerActivity::class.java)
            intent.putExtra(MusicRepository.SONG_POSITION, position)
            intent.putExtra(MusicRepository.TYPE, MusicRepository.SINGLE_ALBUM)
            intent.putExtra(MusicRepository.ALBUM, songDataModel.album)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: SongsRowBinding) : RecyclerView.ViewHolder(itemView.root) {
        val albumsRowBinding: SongsRowBinding = itemView
    }

}