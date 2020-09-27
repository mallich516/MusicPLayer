package com.mallich.musicplayer.fragments.home.tabs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.R
import com.mallich.musicplayer.adapters.SongsAdapter
import com.mallich.musicplayer.data.MusicViewModel
import com.mallich.musicplayer.interfaces.AllMusicInterface

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
        recyclerView.adapter = SongsAdapter(requireContext(), MusicRepository.getAllSongs(requireActivity().application), this)

        return view
    }

    override fun sendSelectedSongToPlay(context: Context, position: Int) {
        MusicRepository.checkIfMediaPlayerIsNull(context)
        MusicRepository.songPosition = position
        MusicRepository.albumType = MusicRepository.ALL_SONGS
        MusicRepository.SELECTED_SONG = true
        MusicRepository.getSelectedPlayList(requireActivity().application)
        MusicRepository.updateSongInDataStore(musicViewModel)
//        context.startActivity(Intent(context, MusicPlayerActivity::class.java))
        findNavController().navigate(R.id.action_homeFragment_to_playerFragment)
    }

}

