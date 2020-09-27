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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.R
import com.mallich.musicplayer.adapters.SongsAdapter
import com.mallich.musicplayer.data.MusicViewModel
import com.mallich.musicplayer.interfaces.AllMusicInterface
import com.mallich.musicplayer.ui.MainActivity
import com.mallich.musicplayer.ui.MusicPlayerActivity

class SongsFragment : Fragment(), AllMusicInterface {

    private lateinit var musicViewModel: MusicViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_songs, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.songsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        musicViewModel = ViewModelProvider.AndroidViewModelFactory(requireActivity().application).create(MusicViewModel::class.java)
        recyclerView.adapter = SongsAdapter(context!!, MusicRepository.getAllSongs(requireActivity().application), this)

        return view
    }

    override fun sendSelectedSongToPlay(context: Context, position: Int) {
        MusicRepository.checkIfMediaPlayerIsNull(context)
        MusicRepository.songPosition = position
        MusicRepository.albumType = MusicRepository.ALL_SONGS
        MusicRepository.SELECTED_SONG = true
        MusicRepository.getSelectedPlayList(requireActivity().application)
        MusicRepository.updateSongInDataStore(musicViewModel)
        context.startActivity(Intent(context, MusicPlayerActivity::class.java))
    }

}

