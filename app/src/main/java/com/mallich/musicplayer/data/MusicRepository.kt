package com.mallich.musicplayer.data

import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.mallich.musicplayer.models.SongDataModel

class MusicRepository {

    companion object {

        var SONG_STATUS: String = "0"
        const val SONG_PLAY: String = "1"
        const val SONG_PAUSE: String = "0"
        const val SONG_POSITION: String = "songId"
        const val TYPE: String = "Type"
        const val ALBUM: String = "Album"
        const val ALL_SONGS: String = "AllSongs"
        const val ALBUM_ART: String = "albumArt"
        const val SINGLE_ALBUM: String = "SingleAlbum"
        const val LETS_START_MUSIC: String = "Let's Start Music"
        const val PLAY_NOW: String = "Play Now"

        @RequiresApi(Build.VERSION_CODES.R)
        @SuppressLint("Recycle")
        fun getAllSongs(application: Application): MutableList<SongDataModel> {

            val list: MutableList<SongDataModel> = mutableListOf()

            // getting the external storage media store audio uri
            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            // non-zero if audio file is a music file
            val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"

            // sorting the music
            val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

            val cursor: Cursor? = application.contentResolver!!.query(
                uri,
                null,
                selection,
                null,
                sortOrder
            )

            if (cursor != null && cursor.moveToFirst()) {
                val id: Int = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                val title: Int = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val album: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val artist: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
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

        fun getAllAlbums(application: Application): MutableList<SongDataModel> {

            // mutable list initialization
            val list: MutableList<SongDataModel> = mutableListOf()

            // uri for music files
            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            // album selection
            val selection = MediaStore.Audio.AlbumColumns.ALBUM + "!='<unknown>'"

            // sorting order for albums
            val sortOrder = MediaStore.Audio.AlbumColumns.ALBUM + " ASC"

            val cursor: Cursor? = application.contentResolver!!.query(
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
        fun getAllArtists(application: Application): MutableList<SongDataModel> {

            val list = mutableListOf<SongDataModel>()

            val uri: Uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI

            val sortOrder = MediaStore.Audio.ArtistColumns.ARTIST + " ASC"

            val cursor: Cursor? =
                application.contentResolver!!.query(uri, null, null, null, sortOrder)

            if (cursor != null && cursor.moveToFirst()) {
                val name = cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST)
                val artId = cursor.getColumnIndex(MediaStore.Audio.Artists.Albums.ALBUM_ID)

                do {
                    val artistName = cursor.getString(name)
                    val albumArtID = cursor.getLong(artId)

                    val sArtworkUri = Uri.parse("content://media/external/audio/albumart")
                    val albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumArtID)

                    val songDataModel = SongDataModel(
                        albumArtID,
                        artistName,
                        "",
                        artistName,
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

        @SuppressLint("Recycle")
        @RequiresApi(Build.VERSION_CODES.R)
        fun getSingleAlbum(application: Application, album: String): MutableList<SongDataModel> {

            val list: MutableList<SongDataModel> = mutableListOf()

            val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            val selection = MediaStore.Audio.Media.ALBUM + "='$album'"

            val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

            val cursor: Cursor? = application.contentResolver!!.query(
                uri,
                null,
                selection,
                null,
                null
            )

            if (cursor != null && cursor.moveToFirst()) {
                val title: Int = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artist: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val duration: Int = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val albumId: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val albumInt: Int = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val fileData: Int = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)

                do {
                    val titleString = cursor.getString(title)
                    val artistString = cursor.getString(artist)
                    val durationString = cursor.getString(duration)
                    val albumArtId = cursor.getLong(albumId)
                    val fileDataString = cursor.getString(fileData)
                    val albumString = cursor.getString(albumInt)

                    val artWorkUri = Uri.parse("content://media/external/audio/albumart")
                    val albumArt = ContentUris.withAppendedId(artWorkUri, albumArtId)

                    val songDataModel = SongDataModel(
                        albumArtId,
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

    }
}