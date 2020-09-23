package com.mallich.musicplayer.fragments

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mallich.musicplayer.MusicRepository
import com.mallich.musicplayer.R
import com.mallich.musicplayer.adapters.SongsAdapter
import com.mallich.musicplayer.interfaces.MusicInterface
import com.mallich.musicplayer.models.SongDataModel
import com.mallich.musicplayer.ui.MainActivity
import com.mallich.musicplayer.ui.MusicPlayerActivity

class SongsFragment : Fragment(), MusicInterface {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_songs, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.songsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        recyclerView.adapter = SongsAdapter(MainActivity(), context!!, MusicRepository.getAllSongs(requireActivity().application))

        return view
    }

    override fun setMusic(context: Context, position: Int) {
        val intent = Intent(context, MusicPlayerActivity::class.java)
        intent.putExtra("songId", position)
        context.startActivity(intent)
    }

}

