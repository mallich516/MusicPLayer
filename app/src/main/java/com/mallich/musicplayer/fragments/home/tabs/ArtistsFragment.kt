package com.mallich.musicplayer.fragments.home.tabs

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.mallich.musicplayer.R
import com.mallich.musicplayer.viewmodels.MusicViewModel
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

//        SingleAlbumActivity.FROM_ARTIST = true
//        binding.artistRecyclerView.adapter =
//            AlbumAdapter(requireContext(), MusicRepository.getAllArtists(requireActivity().application))

        return view
    }

}