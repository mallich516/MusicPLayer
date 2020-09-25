package com.mallich.musicplayer.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.adapters.AlbumAdapter
import com.mallich.musicplayer.R

class AlbumsFragment : Fragment() {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_albums, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.albumRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 3)
        recyclerView.setHasFixedSize(true)

        recyclerView.adapter = AlbumAdapter(
            context!!, MusicRepository.getAllAlbums(requireActivity().application)
        )

        return view
    }


}