package com.kanulp.mymap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.util.*

class MainActivity : FragmentActivity(), OnMapReadyCallback {
    var search: EditText? = null
    var latLng: LatLng? = null
    var title: String? = null
    var currentLocation: Location? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mMap: GoogleMap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fetchCurrentLocation()
        search = findViewById(R.id.edit_text_search)
        Places.initialize(applicationContext, "AIzaSyCO_JouaskDFG1z9RzscTM4Nggzfk6cPPc")
        search?.setFocusable(false)
        search?.setOnClickListener(View.OnClickListener {
            val fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(this@MainActivity)
            startActivityForResult(intent, 100)
        })
    }

    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE)
            return
        }
        val task = fusedLocationProviderClient!!.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                Toast.makeText(this@MainActivity, currentLocation!!.latitude.toString() + "", Toast.LENGTH_SHORT).show()
                val mapFragment = supportFragmentManager
                        .findFragmentById(R.id.map) as SupportMapFragment?
                mapFragment!!.getMapAsync(this@MainActivity)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            val place = Autocomplete.getPlaceFromIntent(data!!)
            search!!.setText(place.address)
            mMap!!.clear()
            latLng = place.latLng
            title = place.name
            val snippet = """
                Address: ${place.address}
                Phone number: ${place.phoneNumber}
                rating${place.rating}
                """.trimIndent()
            mMap!!.addMarker(MarkerOptions().position(latLng!!).title(title).snippet(snippet))
            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
            mMap!!.setInfoWindowAdapter(PlacesAdapter(applicationContext))
        } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            val status = Autocomplete.getStatusFromIntent(data!!)
            Toast.makeText(applicationContext, status.statusMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        latLng = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
        mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        mMap!!.addMarker(MarkerOptions().position(latLng!!).title("Current Location"))
        mMap!!.uiSettings.isZoomControlsEnabled = true
        mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap!!.setOnMapClickListener { destination ->
            mMap!!.clear()
            val options = MarkerOptions()
            options.position(destination)
            options.title("Lat=" + destination.latitude + ", Long=" + destination.longitude)
            val marker = mMap!!.addMarker(options)
            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(destination, 16f))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 101
    }
}