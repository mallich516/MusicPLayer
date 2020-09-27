package com.mallich.musicplayer.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mallich.musicplayer.R
import com.mallich.musicplayer.adapters.SingleAlbumAdapter
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.data.MusicRepository.Companion.album
import com.mallich.musicplayer.data.MusicRepository.Companion.albumArt
import com.mallich.musicplayer.data.MusicViewModel
import com.mallich.musicplayer.databinding.FragmentSingleAlbumBinding
import com.mallich.musicplayer.interfaces.AllMusicInterface

class SingleAlbumFragment : Fragment(), AllMusicInterface {

    private lateinit var viewModel: MusicViewModel
    private lateinit var binding: FragmentSingleAlbumBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_single_album, container, false)
        binding = FragmentSingleAlbumBinding.bind(view)

        viewModel = ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
            .create(MusicViewModel::class.java)

        binding.singleAlbumTitle.text = album
        Glide.with(requireContext())
            .load(albumArt)
            .error(R.drawable.music_logo)
            .into(binding.singleAlbumImage)

        binding.singleAlbumRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.singleAlbumRecyclerView.adapter =
            SingleAlbumAdapter(
                requireContext(),
                MusicRepository.getSingleAlbum(requireActivity().application, album),
                this
            )

        binding.singleAlbumBackBtn.setOnClickListener {
            findNavController().navigate(R.id.action_singleAlbumFragment_to_homeFragment)
        }

        return binding.root
    }

    override fun sendSelectedSongToPlay(context: Context, position: Int) {
        MusicRepository.checkIfMediaPlayerIsNull(context)
        MusicRepository.songPosition = position
        MusicRepository.album = binding.singleAlbumTitle.text.toString().trim()
        MusicRepository.albumType = MusicRepository.SINGLE_ALBUM
        MusicRepository.SELECTED_SONG = true
        MusicRepository.getSelectedPlayList(requireActivity().application)
        MusicRepository.updateSongInDataStore(viewModel)
        MusicRepository.SINGLE_ALBUM_IS_ACTIVE = true
        findNavController().navigate(R.id.action_singleAlbumFragment_to_playerFragment)
    }

}