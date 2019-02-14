package com.example.lab6plataformas

import android.app.Activity
import android.os.Bundle
import java.util.ArrayList
import java.util.Collections
import android.view.View
import android.widget.MediaController.MediaPlayerControl
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.content.ComponentName
import android.content.Context
import com.example.lab6plataformas.MusicService.MusicBinder
import android.os.IBinder
import android.content.ServiceConnection
import android.content.Context.BIND_AUTO_CREATE
import android.os.Build
import android.support.annotation.RequiresApi


class MainActivity() : Activity(), MediaPlayerControl {

    var songList: ArrayList<Song>? = ArrayList()
    lateinit var controller: MusicController
    var musicSrv: MusicService? = null
    var playIntent: Intent? = null
    var musicBound = false
    var paused = false
    var playbackPaused = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Llama a la funcion que busca las canciones
        getSongList()
        //Se ordenan las canciones alfabeticamente
        Collections.sort(songList) { a, b -> a.title.compareTo(b.title) }
        val songAdt = SongAdapter(this, songList!!)
        songListView.adapter = songAdt

        //Se inicializa el controlador de la musica
        setController()
    }

    override fun onStart() {
        super.onStart()
        if (playIntent == null) {
            playIntent = Intent(this, MusicService::class.java)
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE)
            startService(playIntent)
        }
    }

    override fun onDestroy() {
        stopService(playIntent)
        musicSrv = null
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun songPicked(v: View){
        musicSrv!!.setSong(Integer.parseInt(v.getTag().toString()));
        musicSrv!!.playSong();
        setController()
        playbackPaused=true;
        musicSrv!!.go()
        controller.show(0);
    }

    fun getSongList(){
        val musicResolver = contentResolver
        val musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor = musicResolver.query(musicUri, null, null, null, null)
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //trae los datos de la columna
            val titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE)
            val idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID)
            val artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST)
            //agrega las cancones a las listas
            do {
                val thisId = musicCursor.getLong(idColumn)
                val thisTitle = musicCursor.getString(titleColumn)
                val thisArtist = musicCursor.getString(artistColumn)
                songList?.add(Song(thisId, thisTitle, thisArtist))
            } while (musicCursor.moveToNext())
        }
    }

    //connect to the service
    private val musicConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder = service as MusicBinder
            //get service
            musicSrv = binder.service
            //pass list
            musicSrv!!.setList(songList)
            musicBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setController(){
        controller = MusicController(this)
        controller.setPrevNextListeners(
            { playNext() },
            { playPrev() })
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.songListView));
        controller.setEnabled(true);
    }

    //play next
    @RequiresApi(Build.VERSION_CODES.O)
    private fun playNext() {
        musicSrv!!.playNext();
        setController();
        playbackPaused=true;
        musicSrv!!.go()
        controller.show(0);
    }

    //play previous
    @RequiresApi(Build.VERSION_CODES.O)
    private fun playPrev() {
        musicSrv!!.playPrev();
        setController();
        playbackPaused=true;
        musicSrv!!.go()
        controller.show(0);
    }

    override fun isPlaying(): Boolean {
        if(musicSrv!=null && musicBound)
        return musicSrv!!.isPng();
        return false;
    }

    override fun canSeekForward(): Boolean {
        return true
    }

    override fun getDuration(): Int {
        if(musicSrv!=null && musicBound && musicSrv!!.isPng())
        return musicSrv!!.getDur();
        else return 0;
    }

    override fun pause() {
        playbackPaused=true
        musicSrv!!.pausePlayer()
    }

    override fun onPause() {
        super.onPause()
        paused = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()
        if (paused) {
            setController()
            paused = false
        }
    }

    override fun onStop() {
        controller.hide()
        super.onStop()
    }

    override fun getBufferPercentage(): Int {
        return 1
    }

    override fun seekTo(pos: Int) {
        musicSrv!!.seek(pos)
    }

    override fun getCurrentPosition(): Int {
        if(musicSrv!=null && musicBound && musicSrv!!.isPng())
        return musicSrv!!.getPosn();
        else return 0;
    }

    override fun canSeekBackward(): Boolean {
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun start() {
        musicSrv!!.go()
    }

    override fun getAudioSessionId(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canPause(): Boolean {
        return true
    }
}
