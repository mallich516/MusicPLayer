package com.mallich.musicplayer.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.mallich.musicplayer.R
import com.mallich.musicplayer.adapters.SingleAlbumAdapter
import com.mallich.musicplayer.repositories.MusicRepository
import com.mallich.musicplayer.repositories.MusicRepository.Companion.album
import com.mallich.musicplayer.repositories.MusicRepository.Companion.albumArt
import com.mallich.musicplayer.viewmodels.MusicViewModel
import com.mallich.musicplayer.databinding.FragmentSingleAlbumBinding
import com.mallich.musicplayer.interfaces.AllMusicInterface
import com.mallich.musicplayer.models.SongDataModel

class SingleAlbumFragment : Fragment(), AllMusicInterface {

    private lateinit var viewModel: MusicViewModel
    private lateinit var binding: FragmentSingleAlbumBinding
    private lateinit var adapter: SingleAlbumAdapter
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
        adapter = SingleAlbumAdapter(
            requireContext(),
            MusicRepository.getSingleAlbum(requireActivity().application, album),
            this
        )
        binding.singleAlbumRecyclerView.adapter = adapter

        binding.singleAlbumBackBtn.setOnClickListener {
            findNavController().popBackStack()
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
        if (MusicRepository.songTitle != MusicRepository.playList[position].name) {
            MusicRepository.updateSongInDataStore(viewModel)
        }
        findNavController().navigate(R.id.action_singleAlbumFragment_to_playerFragment)
    }

    override fun optionClicked(context: Context, songData: SongDataModel, view: View) {
        openOptionMenu(context, songData, view)
    }

    private fun openOptionMenu(context: Context, songData: SongDataModel, view: View) {
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.song_option_menu, popupMenu.menu)
        popupMenu.menu.removeItem(R.id.song_option_GoToAlbum)
        popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.song_option_SetAsRingtone -> {
                    setRingtone(songData)
                }
                R.id.song_option_Delete -> {
                    deleteSong(songData)
                }
                R.id.song_option_GoToAlbum -> {
                    openAlbum(songData)
                }
                R.id.song_option_PlayNext -> {
                    Toast.makeText(context, menuItem.title, Toast.LENGTH_SHORT).show()
                }
            }
            true
        })
        popupMenu.show()
    }

    private fun openAlbum(songData: SongDataModel) {
        MusicRepository.album = songData.album
        MusicRepository.albumArt = songData.albumArt
        findNavController().navigate(R.id.action_homeFragment_to_singleAlbumFragment)
    }

    private fun deleteSong(songData: SongDataModel) {
        val contentResolver = requireActivity().contentResolver
        val newUri = Uri.parse("content://media/external/audio/media/" + songData.id)
        val deleted = contentResolver.delete(newUri, null, null)
        if (deleted > 0) {
            Toast.makeText(
                context,
                "${songData.name} Deleted",
                Toast.LENGTH_SHORT
            ).show()
            adapter.notifyDataSetChanged()
        } else {
            Toast.makeText(
                context,
                "Unable to delete ${songData.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setRingtone(songData: SongDataModel) {
        val newUri = Uri.parse("content://media/external/audio/media/" + songData.id)
        try {
            RingtoneManager.setActualDefaultRingtoneUri(
                requireActivity(),
                RingtoneManager.TYPE_RINGTONE,
                newUri
            )
            Toast.makeText(context, "Ringtone Set", Toast.LENGTH_SHORT).show()
        } catch (t: Throwable) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                checkSettingsPermission()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkSettingsPermission() {
        val settingsCanWrite = Settings.System.canWrite(context)
        if (!settingsCanWrite) {
            // If do not have write settings permission then open the Can modify system settings panel.
            val alertDialog: android.app.AlertDialog? =
                android.app.AlertDialog.Builder(requireContext()).create()
            alertDialog?.setMessage("You have to give access to MusicPlayer in settings")
            alertDialog?.setIcon(R.drawable.music_logo)
            alertDialog?.setButton(
                AlertDialog.BUTTON_POSITIVE,
                "Ok",
                DialogInterface.OnClickListener { dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.dismiss()
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    startActivity(intent)
                })
            alertDialog?.show()
        }
//        else {
//            // If has permission then show an alert dialog with message.
////            val alertDialog: android.app.AlertDialog? = android.app.AlertDialog.Builder(requireContext()).create()
//////            alertDialog?.setMessage("You have system write settings permission now.")
////            alertDialog?.setMessage("Now you can set ringtone.")
////            alertDialog?.show()
//        }
    }


}