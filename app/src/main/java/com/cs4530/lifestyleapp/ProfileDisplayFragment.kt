package com.cs4530.lifestyleapp

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import java.io.File
import java.io.FileOutputStream
import java.lang.ClassCastException

class ProfileDisplayFragment: Fragment() {
    private var nameTextView: TextView? = null
    private var ageTextView : TextView? = null
    private var locationTextView : TextView? = null
    private var heightTextView: TextView? = null
    private var weightTextView: TextView? = null
    private var sexTextView: TextView? = null
    private var activityLevelTextView: TextView? = null
    private var bmrScore: TextView? = null
    private var mIvPic: ImageView? = null
    private var editButton: Button? = null

    private var locationString: String = ""

    private val mViewModel: MainViewModel by activityViewModels()

    interface ProfileDisplayNavigationInterface {
        fun navigateToEditPage()
    }

    var dataPasser: ProfileDisplayNavigationInterface? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPasser = try {
            context as ProfileDisplayNavigationInterface
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement ProfileDisplayFragment.DataPassingInterface")
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_display, container, false)

        // Get the UI elements
        nameTextView = view.findViewById(R.id.name_value)
        ageTextView = view.findViewById(R.id.age_value)
        locationTextView = view.findViewById(R.id.location_value)
        heightTextView = view.findViewById(R.id.height_value)
        weightTextView = view.findViewById(R.id.weight_value)
        sexTextView = view.findViewById(R.id.sex_value)
        activityLevelTextView = view.findViewById(R.id.activity_level_value)
        bmrScore = view.findViewById(R.id.bmr_score)
        editButton = view.findViewById<Button>(R.id.editButton)


        // Set the observer
        mViewModel.dataUser.observe(requireActivity(), liveDataObserver)

        // Initialize the location manager. This will get an update on the users location while onCreate is running.
        val locationManager: LocationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        // Initialize the location listener. This is designed to get an updated location once and stop.
        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationString = location.latitude.toString() + ", " + location.longitude.toString()
                //This ends the requestLocationUpdates. Though initial tests didn't show much degredation in peformance. Best practice to removeUpdates though.
                locationManager.removeUpdates(this)
            }
            override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                // Handle status changes
                var yes : String? = null
            }
            override fun onProviderEnabled(provider: String) {
                // Handle provider enabled
                var yes : String? = null
            }
            override fun onProviderDisabled(provider: String) {
                // Handle provider disabled
                var yes : String? = null
            }
        }

        /** The next block retrieve the photo from cache and redraw them **/
        var bits : Bitmap? = getBitmapFromCache()
        if(bits != null) {
            mIvPic = view.findViewById<View>(R.id.profile_pic) as ImageView
            mIvPic!!.setImageBitmap(bits)
        }
        else { //Default view is a person silhouette
            mIvPic = view.findViewById<View>(R.id.profile_pic) as ImageView
            mIvPic!!.setImageResource(R.drawable.baseline_person_24)
        }

        /** This block prevents hike button from being active if location is disabled. Otherwise implements **/
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(requireContext(), "No location permission. Hikes not accurate.", Toast.LENGTH_SHORT).show()
            val hikeButtonDisabled = view.findViewById<Button>(R.id.find_hike)
            hikeButtonDisabled.setOnClickListener {
                Toast.makeText(requireContext(), "Enable location permission to find hikes", Toast.LENGTH_SHORT).show()
                hikeButtonDisabled.isEnabled = false
            }
        }
        else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0f,
                locationListener
            ) //Suppressed MissingPermission for this

            /** The next block listens for find hike button and opens the map**/
            val hikeButton = view.findViewById<Button>(R.id.find_hike)
            hikeButton.setOnClickListener {
                val searchUri =
                    Uri.parse("geo:" + locationString + "?q=hikes near me") //Instead of getting location in search, utilized maps' automatic search from location
                //create map intent
                val mapIntent = Intent(Intent.ACTION_VIEW, searchUri)
                try {
                    startActivity(mapIntent)
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), "Map Not Available", Toast.LENGTH_SHORT).show()
                }
            }

            editButton?.setOnClickListener {
                dataPasser?.navigateToEditPage()
            }
        }

        return view
    }

    private val liveDataObserver: Observer<UserTable> =
        Observer { userData ->
            if (userData != null) {
                nameTextView!!.text = userData!!.firstName + " " + userData!!.lastName
                ageTextView!!.text = userData!!.age.toString()
                locationTextView!!.text = userData!!.city + ", " + userData!!.country
                heightTextView!!.text = (userData!!.height?.div(12)).toString() + "\' " + (userData!!.height?.rem(12)).toString() + "\""
                weightTextView!!.text = userData!!.weight.toString() + "lbs"
                sexTextView!!.text = userData!!.sex
                activityLevelTextView!!.text = userData!!.activityLevel
                bmrScore!!.text = userData!!.bmr.toString()
            }
        }

    /**
     * This method/function is hardcoded to retrieve the profile photo from the cache.
     * This is used in [onCreate]
     */
    private fun getBitmapFromCache(): Bitmap? {
        val fileName = "photo.png"
        val file = File(requireActivity().cacheDir, fileName)

        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

}