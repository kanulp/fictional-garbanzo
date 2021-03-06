package com.kanulp.mymap

import android.content.Context
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import android.widget.TextView
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.model.Marker
import java.lang.Exception

class PlacesAdapter(var mContext: Context) : InfoWindowAdapter {
    private val mWindow: View

    private fun rendowWindowText(marker: Marker, view: View) {
        val title = marker.title
        val tvTitle = view.findViewById<View>(R.id.title) as TextView
        if (title != "") {
            tvTitle.text = title
        }
        val snippet = marker.snippet
        val data = view.findViewById<View>(R.id.data) as TextView
        try {
            if (snippet != "") {
                data.text = snippet
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(mContext, "No place information available.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getInfoWindow(marker: Marker): View {
        rendowWindowText(marker, mWindow)
        return mWindow
    }

    override fun getInfoContents(marker: Marker): View {
        rendowWindowText(marker, mWindow)
        return mWindow
    }

    init {
        mWindow = LayoutInflater.from(mContext).inflate(R.layout.item_places, null)
    }
}