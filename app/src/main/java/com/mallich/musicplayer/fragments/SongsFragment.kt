package com.mallich.musicplayer.fragments

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.R
import com.mallich.musicplayer.adapters.SongsAdapter
import com.mallich.musicplayer.interfaces.AllMusicInterface
import com.mallich.musicplayer.ui.MainActivity
import com.mallich.musicplayer.ui.MusicPlayerActivity

class SongsFragment : Fragment(), AllMusicInterface {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_songs, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.songsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        MainActivity.getPlayList(requireActivity().application)

        recyclerView.adapter = SongsAdapter(MainActivity(), context!!, MusicRepository.getAllSongs(requireActivity().application))

        return view
    }

    override fun playMusic(context: Context, position: Int) {
        val intent = Intent(context, MusicPlayerActivity::class.java)
        intent.putExtra("songId", position)
        context.startActivity(intent)
    }

}

