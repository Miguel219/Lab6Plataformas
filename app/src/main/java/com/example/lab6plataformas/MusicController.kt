package com.example.lab6plataformas

import android.content.Context
import android.widget.MediaController

class MusicController(context: Context?) : MediaController(context) {
    //se sobre escribe el metodo para que siempre se muestre el controlador
    override fun show(timeout: Int) {
        super.show(0)
    }
}