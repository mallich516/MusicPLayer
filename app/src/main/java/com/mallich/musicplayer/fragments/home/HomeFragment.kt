package com.mallich.musicplayer.fragments.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.mallich.musicplayer.R
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.data.MusicViewModel
import com.mallich.musicplayer.databinding.FragmentHomeBinding
import com.mallich.musicplayer.fragments.home.tabs.AlbumsFragment
import com.mallich.musicplayer.fragments.home.tabs.SongsFragment
import java.lang.Exception

class HomeFragment : Fragment() {

    private lateinit var viewModel: MusicViewModel
    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.bind(view)
        viewModel = ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
            .create(MusicViewModel::class.java)
        checkStoragePermission()
        return binding.root
    }

    private fun liveDataObservers() {
        viewModel.currentSongDetails.observe(requireActivity(), { songDetails ->
            MusicRepository.songTitle = songDetails[0]
            MusicRepository.album = songDetails[1]
            MusicRepository.artist = songDetails[2]
            MusicRepository.songPosition = songDetails[3].toInt()
            MusicRepository.albumType = songDetails[4]
            MusicRepository.albumArt = songDetails[5]
            updateTitleAndImage()
            updateSeekBar()
        })

        viewModel.songStatus.observe(requireActivity(), { songStatus ->
            MusicRepository.SONG_STATUS = songStatus
            updatePlayBtnImage()
            updateSeekBar()
        })
    }

    private fun updatePlayBtnImage() {
        if (MusicRepository.SONG_STATUS == MusicRepository.SONG_PAUSE) {
            binding.homeCurrentSongPlayBtn.setImageResource(R.drawable.play_icon)
        } else {
            binding.homeCurrentSongPlayBtn.setImageResource(R.drawable.pause_icon)
        }
    }

    private fun updateTitleAndImage() {
        binding.homeCurrentSongTitle.text = MusicRepository.songTitle
        binding.homeCurrentSongArtist.text = MusicRepository.artist
        Glide.with(this)
            .load(MusicRepository.albumArt)
            .error(R.drawable.music_logo)
            .into(binding.homeCurrentSongImage)
    }

    private fun updateSeekBar() {
        Handler().postDelayed(object : Runnable {
            override fun run() {
                try {
                    binding.homeCurrentSongProgress.max =
                        MusicRepository.mediaPlayer!!.duration / 1000
                    binding.homeCurrentSongProgress.progress =
                        MusicRepository.mediaPlayer!!.currentPosition / 1000
                } catch (e: Exception) {
                    binding.homeCurrentSongProgress.progress = 0
                }
                Handler().postDelayed(this, 1000)
            }
        }, 0)
    }

    private fun clickEvents() {
        binding.homeCurrentSongPlayBtn.setOnClickListener {
            MusicRepository.checkIfMediaPlayerIsNull(requireContext())
            viewModel.updateSongStatus()
        }
        binding.homeCurrentSongLayout.setOnClickListener {
            MusicRepository.OPEN_FROM_HOME_AS_SINGLE_ALBUM = true
            findNavController().navigate(R.id.action_homeFragment_to_playerFragment)
        }
    }

    private fun startApplication() {

        val titlesList = mutableListOf<String>()
        titlesList.add("Songs")
        titlesList.add("Albums")

        val fragmentsList = mutableListOf<Fragment>()
        fragmentsList.add(SongsFragment())
        fragmentsList.add(AlbumsFragment())

        binding.homeViewPager2.adapter =
            HomeViewPager2Adapter(requireActivity(), fragmentsList, titlesList)
        TabLayoutMediator(binding.homeTabLayout, binding.homeViewPager2) { tab, position ->
            tab.text = titlesList[position]
        }.attach()

        if (MusicRepository.mediaPlayer == null) {
            MusicRepository.SONG_STATUS = MusicRepository.SONG_PLAY
            viewModel.updateSongStatus()
        }

        liveDataObservers()

        clickEvents()

    }

    private fun checkStoragePermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            startApplication()
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startApplication()
//                Toast.makeText(requireContext(), "Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}