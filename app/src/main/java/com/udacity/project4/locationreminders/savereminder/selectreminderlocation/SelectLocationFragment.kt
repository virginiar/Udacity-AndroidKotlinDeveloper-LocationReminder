package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    private lateinit var map : GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedMarker : Marker? = null
    private lateinit var contxt: Context

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
        private val TAG = SelectLocationFragment::class.java.simpleName
        private const val DEFAULT_ZOOM = 15f
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        // DONE: Add the map setup implementation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(contxt)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // DONE: Call this function after the user confirms on the selected location
        onLocationSelected()

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contxt = context
    }

    private fun onLocationSelected() {
        // When the user confirms on the selected location,
        // send back the selected location details to the view model
        // and navigate back to the previous fragment to save the reminder and add the geofence
        binding.saveButton.setOnClickListener {
            if (selectedMarker != null) {
                Log.i(TAG, selectedMarker!!.title)
                _viewModel.latitude.value = selectedMarker!!.position.latitude
                _viewModel.longitude.value = selectedMarker!!.position.longitude
                _viewModel.reminderSelectedLocationStr.value = selectedMarker!!.title
                _viewModel.navigationCommand.postValue(NavigationCommand.Back)
            } else {
                _viewModel.showErrorMessage.value = getString(R.string.select_poi)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // DONE: Add style to the map
        setMapStyle(map)

        // DONE: Zoom to the user location after taking his permission
        enableMyLocation()
        zoomLocation()

        // DONE: Put a marker to location that the user selected
        setPoiClick(map)
        setLongClick(map)
    }

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
            zoomLocation()
        }
        else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        Log.i(TAG, "Request Result")
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                _viewModel.showErrorMessage.postValue(getString(R.string.permission_denied_explanation))
            }
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun zoomLocation() {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lastLocation = LatLng(location.latitude, location.longitude)
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            lastLocation,
                            DEFAULT_ZOOM
                        )
                    )
                }
                else {
                    Log.i(TAG, "Using default location")
                    zoomLocation()
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun setLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { location ->
            map.clear()
            selectedMarker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .title(getString(R.string.poi))
            )
            selectedMarker!!.showInfoWindow()
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            map.clear()
            selectedMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            selectedMarker!!.showInfoWindow()
        }
    }
}



