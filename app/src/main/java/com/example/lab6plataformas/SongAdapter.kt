package com.example.lab6plataformas

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

class SongAdapter(c:Context, theSongs:ArrayList<Song>): BaseAdapter() {

    var songList: ArrayList<Song>? = theSongs
    var songInflater: LayoutInflater = LayoutInflater.from(c)

    override fun getItem(position: Int): Any {
        return songList!!.get(position)
    }

    override fun getItemId(position: Int): Long {
        val currSong = songList!!.get(position)
        return currSong.id
    }

    override fun getCount(): Int {
        return songList!!.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        //map to song layout
        val songLay = songInflater.inflate(R.layout.song, parent, false) as LinearLayout
        //get title and artist views
        val songView = songLay.findViewById<View>(R.id.song_title) as TextView
        val artistView = songLay.findViewById<View>(R.id.song_artist) as TextView
        //get song using position
        val currSong = songList!!.get(position)
        //get title and artist strings
        songView.setText(currSong.title)
        artistView.setText(currSong.artist)
        //set position as tag
        songLay.tag = position
        return songLay
    }
}