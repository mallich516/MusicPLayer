package com.mallich.musicplayer.repositories

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.mallich.musicplayer.services.MusicService
import com.mallich.musicplayer.data.MusicDataStore
import com.mallich.musicplayer.models.SongDataModel
import com.mallich.musicplayer.viewmodels.MusicViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MusicRepository {

    companion object {

        var currentSongTitle: String = ""
        var SINGLE_ALBUM_IS_ACTIVE: Boolean = false
        var OPEN_FROM_HOME_AS_SINGLE_ALBUM: Boolean = false
        var OPEN_FROM_HOME_AS_ALL_SONGS: Boolean = false

        const val ALBUM: String = "Album"
        const val ALL_SONGS: String = "AllSongs"
        const val ALBUM_ART: String = "albumArt"
        const val SINGLE_ALBUM: String = "SingleAlbum"
        const val LETS_START_MUSIC: String = "Let's Start Music"
        const val PLAY_NOW: String = "Play Now"

        // For MusicService and MusicReceiver
        const val PREV_BTN = "prevBtn"
        const val PLAY_BTN = "playBtn"
        const val NEXT_PLAY = "nextBtn"

        const val SONG_PLAY: String = "1"
        const val SONG_PAUSE: String = "0"
        var SONG_STATUS: String = "0"
        var PLAYER_ON: Boolean = false
        var SELECTED_SONG: Boolean = false

        var songPosition = 0
        var songTitle: String = ""
        var artist: String = ""
        var album: String = ""
        var albumArt: String = ""
        var albumType: String = ALL_SONGS
        var playList: MutableList<SongDataModel> = mutableListOf()

        var mediaPlayer: MediaPlayer? = null

        // Reusable Methods
        @SuppressLint("Recycle")
        fun getAllSongs(context: Context): MutableList<SongDataModel> {

            val list: MutableList<SongDataModel> = mutableListOf()

            // getting the external storage media store audio uri
            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            // non-zero if audio file is a music file
            val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"

            // sorting the music
            val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

            val cursor: Cursor? = context.contentResolver!!.query(
                uri,
                null,
                selection,
                null,
                sortOrder
            )

            if (cursor != null && cursor.moveToFirst()) {
                val id: Int = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val title: Int = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val album: Int = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)
                val artist: Int = cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST)
                val data: Int = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val type: Int = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
                val albumId: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val duration: Int = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                do {
                    val audioId = cursor.getLong(id)
                    val audioTitle = cursor.getString(title)
                    val audioAlbum = cursor.getString(album)
                    val audioArtist = cursor.getString(artist)
                    val audioData = cursor.getString(data)
                    val audioType = cursor.getString(type)
                    val audioAlbumId = cursor.getLong(albumId)
                    val duration1: String = cursor.getString(duration)

                    val artWorkUri = Uri.parse("content://media/external/audio/albumart")
                    val albumArtUri = ContentUris.withAppendedId(artWorkUri, audioAlbumId)

                    if (audioType == "audio/mpeg") {
                        list.add(
                            SongDataModel(
                                audioId,
                                audioTitle,
                                audioAlbum,
                                audioArtist,
                                "audioPath",
                                audioData,
                                albumArtUri.toString(),
                                duration1
                            )
                        )
                    }
                } while (cursor.moveToNext())
            }
            return list
        }

        fun getAllAlbums(context: Context): MutableList<SongDataModel> {

            // mutable list initialization
            val list: MutableList<SongDataModel> = mutableListOf()

            // uri for music files
            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            // album selection
            val selection = MediaStore.Audio.AlbumColumns.ALBUM + "!='<unknown>'"

            // sorting order for albums
            val sortOrder = MediaStore.Audio.AlbumColumns.ALBUM + " ASC"

            val cursor: Cursor? = context.contentResolver!!.query(
                uri,
                null,
                selection,
                null,
                sortOrder
            )

            if (cursor != null && cursor.moveToFirst()) {
                val album: Int = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)
                val albumId: Int = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID)
                val albumArtist: Int = cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST)

                do {
                    val albumTitle = cursor.getString(album)
                    val albumID = cursor.getLong(albumId)
                    val albumArtist1 = cursor.getString(albumArtist)

                    val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                    val albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumID)

                    val model = SongDataModel(
                        albumID,
                        "",
                        albumTitle,
                        albumArtist1,
                        "",
                        "",
                        albumArtUri.toString(),
                        ""
                    )
                    list.add(model)
                } while (cursor.moveToNext())
            }
            return list.distinctBy { songDataModel -> songDataModel.album } as MutableList<SongDataModel>
        }

        @SuppressLint("Recycle")
        fun getAllArtists(context: Context): MutableList<SongDataModel> {

            val list = mutableListOf<SongDataModel>()

            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            val selection = MediaStore.Audio.ArtistColumns.ARTIST + "!='<unknown>'"

            val sortOrder = MediaStore.Audio.ArtistColumns.ARTIST + " ASC"

            val cursor: Cursor? =
                context.contentResolver!!.query(uri, null, selection, null, sortOrder)

            if (cursor != null && cursor.moveToFirst()) {
                val name = cursor.getColumnIndex(MediaStore.Audio.ArtistColumns.ARTIST)
                val artId = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                do {
                    val artistName = cursor.getString(name)
                    val albumArtID = cursor.getLong(artId)

                    val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                    val albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumArtID)

                    val songDataModel = SongDataModel(
                        albumArtID,
                        "",
                        artistName,
                        "",
                        "",
                        "",
                        albumArtUri.toString(),
                        ""
                    )
                    list.add(songDataModel)
                } while (cursor.moveToNext())
            }
            return list
        }

        fun getSelectedArtistAlbums(context: Context, artist: String): MutableList<SongDataModel> {

            val list = mutableListOf<SongDataModel>()

            val uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI

            val selection = MediaStore.Audio.ArtistColumns.ARTIST + "='$artist'"

            val sortOrder = MediaStore.Audio.Albums.ALBUM + " ASC"

            val cursor: Cursor? = context.contentResolver!!.query(
                uri,
                null,
                selection,
                null,
                sortOrder
            )

            if (cursor != null && cursor.moveToFirst()) {
                val title: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val artist: Int = cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST)
                val duration: Int = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val albumId: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val albumInt: Int = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)
                val fileData: Int = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                do {
                    val titleString = cursor.getString(title)
                    val artistString = cursor.getString(artist)
//                    val durationString = cursor.getString(duration)
//                    val albumArtId = cursor.getLong(albumId)
//                    val fileDataString = cursor.getString(fileData)
                    val albumString = cursor.getString(albumInt)

//                    val artWorkUri = Uri.parse("content://media/external/audio/albumart")
//                    val albumArt = ContentUris.withAppendedId(artWorkUri, albumArtId)

                    val songDataModel = SongDataModel(
                        0,
                        titleString,
                        albumString,
                        artistString,
                        "",
                        "",
                        "",
                        ""
                    )
                    list.add(songDataModel)
                } while (cursor.moveToNext())
            }

            return list
        }

        @SuppressLint("Recycle")
        fun getSingleAlbum(context: Context, album: String): MutableList<SongDataModel> {

            val list: MutableList<SongDataModel> = mutableListOf()

            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            val selection = MediaStore.Audio.Albums.ALBUM + "='$album'"

            val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

            val cursor: Cursor? = context.contentResolver!!.query(
                uri,
                null,
                selection,
                null,
                null
            )

            if (cursor != null && cursor.moveToFirst()) {
                val id: Int = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val title: Int = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artist: Int = cursor.getColumnIndex(MediaStore.Audio.Albums.ARTIST)
                val duration: Int = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val albumId: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val albumInt: Int = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM)
                val fileData: Int = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                do {
                    val audioId = cursor.getLong(id)
                    val titleString = cursor.getString(title)
                    val artistString = cursor.getString(artist)
                    val durationString = cursor.getString(duration)
                    val albumArtId = cursor.getLong(albumId)
                    val fileDataString = cursor.getString(fileData)
                    val albumString = cursor.getString(albumInt)

                    val artWorkUri = Uri.parse("content://media/external/audio/albumart")
                    val albumArt = ContentUris.withAppendedId(artWorkUri, albumArtId)

                    val songDataModel = SongDataModel(
                        audioId,
                        titleString,
                        albumString,
                        artistString,
                        "",
                        fileDataString,
                        albumArt.toString(),
                        durationString
                    )
                    list.add(songDataModel)
                } while (cursor.moveToNext())
            }
            return list;
        }

        private fun updateSongInDataStore(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val musicDataStore = MusicDataStore(context)
                musicDataStore.updateCurrentSongDetails(
                    playList[songPosition].name,
                    playList[songPosition].album,
                    playList[songPosition].artist,
                    songPosition,
                    albumType,
                    playList[songPosition].albumArt
                )
            }
        }

        fun getSelectedPlayList(context: Context) {
            if (albumType == SINGLE_ALBUM) {
                playList.clear()
                playList.addAll(getSingleAlbum(context, album))
                println("PLAYLIST SIZE $albumType ${playList.size}")
            } else {
                playList.clear()
                playList.addAll(getAllSongs(context))
                println("PLAYLIST SIZE $albumType ${playList.size}")
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun buildNotification(context: Context) {
            // Start Notification Service
            val intentService = Intent(context, MusicService::class.java)
            intentService.putExtra(
                "name",
                playList[songPosition].name
            )
            intentService.putExtra(
                "album",
                playList[songPosition].album
            )
            context.startForegroundService(intentService)
        }

        fun incrementSongPosition() {
            songPosition += 1
            if (songPosition >= playList.size) {
                songPosition = 0
            }
            println("SONG POSITION : $songPosition")
        }

        fun decrementSongPosition() {
            songPosition -= 1
            if (songPosition < 0) {
                songPosition = playList.size - 1
            }
            println("SONG POSITION : $songPosition")
        }

        fun checkIfMediaPlayerIsNull(context: Context) {
            if (mediaPlayer == null) {
                getSelectedPlayList(context)
                playMusic(context)
            }
        }

        fun playMusic(context: Context) {
            val selectedSong = Uri.parse(playList[songPosition].fileData)
            if (mediaPlayer != null) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            }
            mediaPlayer = MediaPlayer.create(context, selectedSong)
            if (SONG_STATUS == SONG_PLAY) {
                mediaPlayer?.start()
            } else {
                mediaPlayer?.start()
                mediaPlayer?.pause()
            }
        }

        fun sendSongToMusicPlayerToPlay(context: Context, position: Int, playListType: String) {
            songPosition = position
            albumType = playListType
            SELECTED_SONG = true
            updateSongInDataStore(context)
//            context.startActivity(Intent(context, MusicPlayerActivity::class.java))
        }

        fun getTimeString(milliseconds: Int): String {
            val hours = (milliseconds / (1000 * 60 * 60))
            val minutes = ((milliseconds % (1000 * 60 * 60)) / (1000 * 60))
            val seconds = (((milliseconds % (1000 * 60 * 60)) % (1000 * 60)) / 1000)

            var secondsString = ""
            secondsString = if (seconds < 10) {
                String.format("%02d", seconds)
            } else {
                String.format("%2d", seconds)
            }

            if (hours == 0) {
                String.format("%2d", hours) + ":" + String.format(
                    "%2d",
                    minutes
                ) + ":" + secondsString
            }
            return String.format("%2d", minutes) + ":" + secondsString
        }

        fun updateSongInDataStore(musicViewModel: MusicViewModel) {
            musicViewModel.updateCurrentSong(
                playList[songPosition].name,
                playList[songPosition].album,
                playList[songPosition].artist,
                songPosition,
                albumType,
                playList[songPosition].albumArt
            )
        }
    }
}