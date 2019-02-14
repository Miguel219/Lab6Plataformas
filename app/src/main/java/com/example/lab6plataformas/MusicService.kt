package com.example.lab6plataformas

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import com.example.lab6plataformas.MusicService.MusicBinder
import android.os.IBinder
import android.content.ContentUris
import android.util.Log
import android.view.MenuItem
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.support.annotation.RequiresApi
import android.content.Context.NOTIFICATION_SERVICE
import android.support.v4.content.ContextCompat.getSystemService




class MusicService() : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener, Parcelable {

    var player: MediaPlayer = MediaPlayer()
    lateinit var songs: ArrayList<Song>
    var songPosn: Int = 0
    private val musicBind = MusicBinder()
    var NOTIFY_ID = 1
    var songTitle = ""
    var songArtist = ""

    override fun onCreate() {
        initMusicPlayer()
    }

    fun initMusicPlayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK)
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player.setOnPreparedListener(this)
        player.setOnCompletionListener(this)
        player.setOnErrorListener(this)
    }

    fun setList(theSongs: java.util.ArrayList<Song>?) {
        songs = theSongs!!
    }

    inner class MusicBinder : Binder() {
        internal val service: MusicService
            get() = this@MusicService
    }

    override fun onBind(intent: Intent): Binder? {
        return musicBind
    }

    override fun onUnbind(intent: Intent): Boolean {
        player.stop()
        player.release()
        return false
    }

    fun playSong() {
        player.reset()
        val playSong = songs[songPosn]
        songTitle=playSong.title;
        songArtist=playSong.artist
        //get id
        val currSong = playSong.id
        //set uri
        val trackUri = ContentUris.withAppendedId(
            android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            currSong
        )
        try {
            player.setDataSource(applicationContext, trackUri)
        } catch (e: Exception) {
            Log.e("MUSIC SERVICE", "Error setting data source", e)
        }
        player.prepareAsync()
    }

    fun setSong(songIndex: Int) {
        songPosn = songIndex
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mp!!.start()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mp!!.reset()
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCompletion(mp: MediaPlayer?) {
        mp!!.reset();
        go()
        playNext();
    }

    fun getPosn(): Int {
        return player.currentPosition
    }

    fun getDur(): Int {
        return player.duration
    }

    fun isPng(): Boolean {
        return player.isPlaying
    }

    fun pausePlayer() {
        player.pause()
    }

    fun seek(posn: Int) {
        player.seekTo(posn)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun go() {
        player.start()
        val notIntent = Intent(this, MainActivity::class.java)
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendInt = PendingIntent.getActivity(
            this, 0,
            notIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = Notification.Builder(this)
        builder.setContentIntent(pendInt)
        builder.setSmallIcon(R.drawable.ic_play_arrow_black_24dp)
        builder.setTicker(songTitle)
        builder.setOngoing(true)
        builder.setContentTitle(songTitle)
        builder.setContentText(songArtist)
        //Se crea un canal
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "sdl_notification_channel"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val notificationChannel = NotificationChannel(channelId, "SmartDeviceLink", importance)
        notificationChannel.enableLights(false)
        notificationChannel.enableVibration(false)
        notificationManager.createNotificationChannel(notificationChannel)
        builder.setChannelId(channelId)
        val not = builder.build()
        startForeground(NOTIFY_ID,not)
    }

    //Acciones de los botones
    //Prev
    fun playPrev(){
        songPosn = songPosn-2
        if(songPosn==0) {
            songPosn = songs.size - 1;
        }
            playSong();
    }
    //Next
    fun playNext(){
        songPosn++
        if(songPosn==songs.size) {
            songPosn=0
        }
        playSong();
    }

    override fun onDestroy() {
        stopForeground(true)
    }

    constructor(parcel: Parcel) : this() {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MusicService> {
        override fun createFromParcel(parcel: Parcel): MusicService {
            return MusicService(parcel)
        }

        override fun newArray(size: Int): Array<MusicService?> {
            return arrayOfNulls(size)
        }
    }
}