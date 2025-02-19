package com.fmd.mapsprojesi

import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.fmd.mapsprojesi.databinding.ActivityMapsBinding
import android.Manifest
import android.content.SharedPreferences
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager:LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher:ActivityResultLauncher<String>

    var takipBoolean:Boolean?=null
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher()
        sharedPreferences=getSharedPreferences("com.fmd.mapsprojesi",MODE_PRIVATE)
        takipBoolean=false
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)

        locationManager=this.getSystemService(LOCATION_SERVICE) as LocationManager
        locationListener=object :LocationListener{
            override fun onLocationChanged(location: Location) {
                takipBoolean=sharedPreferences.getBoolean("takipBoolean",false)
                if(!takipBoolean!!){
                    mMap.clear()
                    val kullaniciKonumu=LatLng(location.latitude,location.longitude)
                    mMap.addMarker(MarkerOptions().position(kullaniciKonumu).title("Konumunuz"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kullaniciKonumu,10f))
                    sharedPreferences.edit().putBoolean("takipBoolean",true).apply()
                }

            }
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
                Snackbar.make(binding.root,"Konumunuzu Almamız İçin İzin Gerekli",Snackbar.LENGTH_INDEFINITE).setAction(
                    "Onayla"
                ){
                    permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()
            }else{
                permissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,4,4f,locationListener)
            val sonBilinenKonum=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if(sonBilinenKonum!=null){
                val sonBilinenLatLng=LatLng(sonBilinenKonum.latitude,sonBilinenKonum.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sonBilinenLatLng,14f))

            }
        }

    }
    private fun registerLauncher(){
        permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){result->
            if(result){
                if(ContextCompat.checkSelfPermission(this@MapsActivity,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,4,4f,locationListener)
                    val sonBilinenKonum=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if(sonBilinenKonum!=null){
                        val sonBilinenLatLng=LatLng(sonBilinenKonum.latitude,sonBilinenKonum.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sonBilinenLatLng,14f))

                    }
                }
            }
            else{
                Toast.makeText(this@MapsActivity,"İzne İhtiyacımız Var!",Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onMapLongClick(p0: LatLng) {
        mMap.clear()
    }
}