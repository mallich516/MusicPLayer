package com.mallich.musicplayer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewbinding.ViewBinding
import com.mallich.musicplayer.R
import com.mallich.musicplayer.adapters.AlbumAdapter
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.data.MusicViewModel
import com.mallich.musicplayer.databinding.FragmentArtistsBinding

class ArtistsFragment : Fragment() {

    lateinit var musicViewModel: MusicViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_artists, container, false)
        val binding = FragmentArtistsBinding.bind(view)

        binding.artistRecyclerView.layoutManager = GridLayoutManager(context, 3)


        binding.artistRecyclerView.adapter =
            AlbumAdapter(context!!, MusicRepository.getAllArtists(activity!!.application))

        return view
    }

}