package com.mallich.musicplayer.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.ReportFragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.mallich.musicplayer.R
import com.mallich.musicplayer.data.MusicRepository
import com.mallich.musicplayer.data.MusicViewModel
import com.mallich.musicplayer.databinding.FragmentPlayerBinding
import java.lang.Exception

class PlayerFragment : Fragment() {

    private lateinit var binding: FragmentPlayerBinding
    private lateinit var audioManager: AudioManager
    private lateinit var viewModel: MusicViewModel

    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_player, container, false)
        binding = FragmentPlayerBinding.bind(view)

        audioManager = activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        binding.musicPlayerVolumeSeekBar.max =
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        binding.musicPlayerVolumeSeekBar.progress =
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        viewModel = ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
            .create(MusicViewModel::class.java)

        ifSongSelected()

        liveDataObservers()

        clickListeners()

//        val callback = requireActivity().onBackPressedDispatcher.addCallback(requireActivity()) {
//            if (MusicRepository.albumType == MusicRepository.SINGLE_ALBUM) {
//                when {
//                    MusicRepository.SINGLE_ALBUM_IS_ACTIVE -> {
//                        MusicRepository.SINGLE_ALBUM_IS_ACTIVE = false
//                        findNavController().navigate(R.id.action_playerFragment_to_singleAlbumFragment)
//                    }
//                    MusicRepository.OPEN_FROM_HOME_AS_SINGLE_ALBUM -> {
//                        MusicRepository.OPEN_FROM_HOME_AS_SINGLE_ALBUM = false
//                        findNavController().navigate(R.id.action_playerFragment_to_homeFragment)
//                    }
//                    else -> {
//                        findNavController().navigate(R.id.action_singleAlbumFragment_to_homeFragment)
//                    }
//                }
//            } else if (MusicRepository.albumType == MusicRepository.ALL_SONGS) {
//                when {
//                    MusicRepository.OPEN_FROM_HOME_AS_ALL_SONGS -> {
//                        findNavController().navigate(R.id.action_playerFragment_to_homeFragment)
//                    }
//                }
//            }
//        }

        return binding.root
    }

    private fun clickListeners() {
        binding.musicPlayerPlayBtn.setOnClickListener {
            MusicRepository.checkIfMediaPlayerIsNull(requireActivity().application)
            viewModel.updateSongStatus()
        }

        binding.musicPlayerPrevBtn.setOnClickListener {
            MusicRepository.checkIfMediaPlayerIsNull(requireActivity().application)
            MusicRepository.decrementSongPosition()
            MusicRepository.updateSongInDataStore(viewModel)
            MusicRepository.playMusic(requireContext())
        }

        binding.musicPlayerNextBtn.setOnClickListener {
            MusicRepository.checkIfMediaPlayerIsNull(requireActivity().application)
            MusicRepository.incrementSongPosition()
            MusicRepository.updateSongInDataStore(viewModel)
            MusicRepository.playMusic(requireContext())
        }

        binding.musicPlayerBackBtn.setOnClickListener {
            findNavController().navigate(R.id.action_playerFragment_to_homeFragment)
        }

        binding.musicPlayerSongSeekBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, changed: Boolean) {
                    if (changed) {
                        MusicRepository.mediaPlayer?.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }
            })

        binding.musicPlayerVolumeSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, changed: Boolean) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

        })

        MusicRepository.mediaPlayer?.setOnCompletionListener { mp ->
            MusicRepository.songPosition += 1
            if (MusicRepository.songPosition < MusicRepository.playList.size) {
                MusicRepository.updateSongInDataStore(viewModel)
                MusicRepository.playMusic(requireActivity().application)
            } else {
                binding.musicPlayerSongSeekBar.progress = 0
                binding.musicPlayerStartTime.text = MusicRepository.getTimeString(0)
                binding.musicPlayerEndTime.text = MusicRepository.getTimeString(mp!!.duration)
            }
        }
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
        if (MusicRepository.SONG_STATUS == MusicRepository.SONG_PLAY) {
            binding.musicPlayerPlayBtn.setImageResource(R.drawable.pause_icon)
        } else {
            binding.musicPlayerPlayBtn.setImageResource(R.drawable.play_icon)
        }
    }

    private fun updateSeekBar() {
        Handler().postDelayed(object : Runnable {
            override fun run() {
                try {
                    binding.musicPlayerSongSeekBar.progress =
                        MusicRepository.mediaPlayer!!.currentPosition
                    val currentTime =
                        MusicRepository.getTimeString(MusicRepository.mediaPlayer!!.currentPosition)
                    binding.musicPlayerSongSeekBar.max = MusicRepository.mediaPlayer!!.duration
                    binding.musicPlayerEndTime.text =
                        MusicRepository.getTimeString(MusicRepository.mediaPlayer!!.duration)
                    binding.musicPlayerStartTime.text = currentTime
                    Handler().postDelayed(this, 1000)
                } catch (e: Exception) {
                    binding.musicPlayerSongSeekBar.progress = 0
                }
            }
        }, 0)
    }

    private fun updateTitleAndImage() {
        binding.musicPlayerTitle.text = MusicRepository.songTitle
        binding.musicPlayerAlbum.text = MusicRepository.album
        Glide.with(this)
            .load(MusicRepository.albumArt)
            .error(R.drawable.music_logo).into(
                binding.musicPlayerImage
            )
    }

    private fun ifSongSelected() {
        if (MusicRepository.SELECTED_SONG) {
            MusicRepository.SONG_STATUS = MusicRepository.SONG_PAUSE
            viewModel.updateSongStatus()
            MusicRepository.playMusic(requireActivity().application)
        }
    }
}