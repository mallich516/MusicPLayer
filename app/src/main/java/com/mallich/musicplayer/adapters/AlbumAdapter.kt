package com.mallich.musicplayer.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.R
import com.mallich.musicplayer.databinding.AlbumsRowBinding
import com.mallich.musicplayer.models.SongDataModel
import com.mallich.musicplayer.ui.SingleAlbumActivity

class AlbumAdapter(val context: Context, private val list: MutableList<SongDataModel>) :
    RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: AlbumsRowBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.albums_row,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val songDataModel = list[position]
        holder.albumsRowBinding.data = songDataModel
        holder.albumsRowBinding.root.setOnClickListener {
            openSelectedAlbum(songDataModel)
        }
    }

    private fun openSelectedAlbum(songDataModel: SongDataModel) {
        val intent= Intent(context, SingleAlbumActivity::class.java)
        intent.putExtra(MusicRepository.ALBUM, songDataModel.album)
        intent.putExtra(MusicRepository.ALBUM_ART, songDataModel.albumArt)
        context.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: AlbumsRowBinding) : RecyclerView.ViewHolder(itemView.root) {
        val albumsRowBinding: AlbumsRowBinding = itemView
    }

}