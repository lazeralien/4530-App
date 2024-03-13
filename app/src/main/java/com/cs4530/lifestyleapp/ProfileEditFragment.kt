package com.cs4530.lifestyleapp

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider
import java.io.File
import java.io.FileOutputStream
import java.lang.ClassCastException
import android.Manifest
import android.content.pm.PackageManager
import android.location.*
import android.location.Geocoder.GeocodeListener
import android.text.TextUtils
import androidx.fragment.app.activityViewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_BALANCED_POWER_ACCURACY
import java.io.IOException
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.*

class ProfileEditFragment(): Fragment(), View.OnClickListener, AdapterView.OnItemSelectedListener {
    // Variables to hold values of UI elements
    private var firstNameValue: String? = null
    private var lastNameValue: String? = null
    private var ageValue: String? = null
    private var cityValue: String? = null
    private var countryValue: String? = null
    private var heightFeetValue: String? = null
    private var heightInchesValue: String? = null
    private var weightValue: String? = null
    private var sexValue: String? = null
    private var activityLevelValue: String? = null
    private var bmrValue: String? = null

    private var currentUserId: Long? = null

    // Variables for UI elements
    private var firstNameTextEdit: EditText? = null
    private var lastNameTextEdit: EditText? = null
    private var ageSlider: Slider? = null
    private var cityTextEdit: EditText? = null
    private var countrySpinner: Spinner? = null
    private var heightFeetSpinner: Spinner? = null
    private var heightInchesSpinner: Spinner? = null
    private var weightSlider: Slider? = null
    private var sexSpinner: Spinner? = null
    private var activityLevelSpinner: Spinner? = null
    private var latLongText: TextView? = null

    private var mButtonCamera: Button? = null
    private var mButtonSubmit: Button? = null
    private var mButtonLocation: Button? = null
    private var mIvPic: ImageView? = null

    private var countryOptions : Array<String> = arrayOf("United States", "Canada", "Ethiopia", "Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Antigua and Barbuda", "Argentina", "Armenia", "Australia", "Austria", "Azerbaijan", "The Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana", "Brazil", "Brunei", "Bulgaria", "Burkina Faso", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Central African Republic", "Chad", "Chile", "China", "Colombia", "Comoros", "Congo, Republic of the", "Congo, Democratic Republic of the", "Costa Rica", "Cote d'Ivoire", "Croatia", "Cuba", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "East Timor (Timor-Leste)", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Fiji", "Finland", "France", "Gabon", "The Gambia", "Georgia", "Germany", "Ghana", "Greece", "Grenada", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Honduras", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea, North", "Korea, South", "Kosovo", "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein", "Lithuania", "Luxembourg", "Macedonia", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Mauritania", "Mauritius", "Mexico", "Micronesia, Federated States of", "Moldova", "Monaco", "Mongolia", "Montenegro", "Morocco", "Mozambique", "Myanmar (Burma)", "Namibia", "Nauru", "Nepal", "Netherlands", "New Zealand", "Nicaragua", "Niger", "Nigeria", "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Poland", "Portugal", "Qatar", "Romania", "Russia", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Sudan", "Spain", "Sri Lanka", "Sudan", "Suriname", "Swaziland", "Sweden", "Switzerland", "Syria", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Togo", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "Uruguay", "Uzbekistan", "Vanuatu", "Vatican City (Holy See)", "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe")
    private var heightFeetOptions: Array<String> = arrayOf("1", "2", "3", "4", "5", "6", "7", "8")
    private var heightInchesOptions: Array<String> = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11")
    private var sexOptions: Array<String> = arrayOf("Prefer not to say", "Female", "Male")
    private var activityLevelOptions: Array<String> =
        arrayOf("Sedentary", "Lightly active", "Moderately active", "Active", "Very active")
    private var activityFactors: Map<String, Double> = mapOf("Sedentary" to 1.2, "Lightly active" to 1.375, "Moderately active" to 1.55, "Active" to 1.725, "Very active" to 1.9)

    private var countryAdapter: ArrayAdapter<String?>? = null
    private var feetAdapter: ArrayAdapter<String?>? = null
    private var inchesAdapter: ArrayAdapter<String?>? = null
    private var sexAdapter: ArrayAdapter<String?>? = null
    private var activityAdapter: ArrayAdapter<String?>? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var requestPermissionLauncher : ActivityResultLauncher<String>

    var dataPasser: ProfileEditDataPassingInterface? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private var gcListener : GCListener = GCListener()

    private val model: MainViewModel by activityViewModels()

    interface ProfileEditDataPassingInterface {
        fun passProfileData()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        dataPasser = try {
            context as ProfileEditDataPassingInterface
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement ProfileDisplayFragment.DataPassingInterface")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_edit, container, false)

        // Get the form fields
        firstNameTextEdit = view.findViewById(R.id.firstNameInput)
        lastNameTextEdit = view.findViewById(R.id.lastNameInput)
        ageSlider = view.findViewById(R.id.ageInput)
        cityTextEdit = view.findViewById(R.id.cityInput)
        countrySpinner = view.findViewById(R.id.countryInput)
        heightFeetSpinner = view.findViewById(R.id.heightFeetInput)
        heightInchesSpinner = view.findViewById(R.id.heightInchesInput)
        weightSlider = view.findViewById(R.id.weightInput)
        sexSpinner = view.findViewById(R.id.sexInput)
        activityLevelSpinner = view.findViewById(R.id.activityLevelInput)

        //Get the buttons
        mButtonCamera = view.findViewById(R.id.uploadPhotoButton)
        mButtonSubmit = view.findViewById(R.id.submitButton)
        mButtonLocation = view.findViewById(R.id.locationButton)

        //Say that this class itself contains the listener
        mButtonCamera!!.setOnClickListener(this)
        mButtonSubmit!!.setOnClickListener(this)
        mButtonLocation!!.setOnClickListener(this)

        // Define remaining values
        countryAdapter = setSpinnerData(R.id.countryInput, countryOptions, view)
        feetAdapter = setSpinnerData(R.id.heightFeetInput, heightFeetOptions, view)
        inchesAdapter = setSpinnerData(R.id.heightInchesInput, heightInchesOptions, view)
        sexAdapter = setSpinnerData(R.id.sexInput, sexOptions, view)
        activityAdapter = setSpinnerData(R.id.activityLevelInput, activityLevelOptions, view)

        mIvPic = view.findViewById(R.id.iv_pic)

        // Set the observer
        model.dataUser.observe(requireActivity(), liveDataObserver)

        /** Retrieve all the field values for data persistence on rotate **/
        if (savedInstanceState != null) {
            firstNameTextEdit!!.setText(savedInstanceState.getString("FIRST_NAME"))
            lastNameTextEdit!!.setText(savedInstanceState.getString("LAST_NAME"))
            ageSlider!!.value = savedInstanceState.getString("AGE")!!.toFloat()
            cityTextEdit!!.setText(savedInstanceState.getString("CITY"))
            val countryPosition =
                countryAdapter!!.getPosition(savedInstanceState.getString("COUNTRY"))
            countrySpinner!!.setSelection(countryPosition)
            val feetPosition =
                feetAdapter!!.getPosition(savedInstanceState.getString("HEIGHT_FEET"))
            heightFeetSpinner!!.setSelection(feetPosition)
            val inchesPosition =
                inchesAdapter!!.getPosition(savedInstanceState.getString("HEIGHT_INCHES"))
            heightInchesSpinner!!.setSelection(inchesPosition)
            weightSlider!!.value = savedInstanceState.getString("WEIGHT")!!.toFloat()
            val sexPosition = sexAdapter!!.getPosition(savedInstanceState.getString("SEX"))
            sexSpinner!!.setSelection(sexPosition)
            val activityPosition =
                activityAdapter!!.getPosition(savedInstanceState.getString("ACTIVITY_LEVEL"))
            activityLevelSpinner!!.setSelection(activityPosition)
        }

        /** The next few lines retrieve the photo from cache and redraw them **/
        var bits: Bitmap? = getBitmapFromCache()
        if (bits != null) {
            mIvPic = view.findViewById<View>(R.id.iv_pic) as ImageView
            mIvPic!!.setImageBitmap(bits)
        }
        else { //Default view is a person silhouette
            mIvPic = view.findViewById<View>(R.id.iv_pic) as ImageView
            mIvPic!!.setImageResource(R.drawable.baseline_person_24)
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted : Boolean ->
            if (isGranted) {
                Toast.makeText(requireContext(), "Location Permission Granted", Toast.LENGTH_SHORT).show()

                fusedLocationClient.getCurrentLocation(PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnSuccessListener { location : Location? ->
                        // Got last known location. In some rare situations this can be null.
                        //                        val latLongText = findViewById<TextView>(R.id.latLong)
                        //                        latLongText.text = "Latitude: ${location?.latitude} Longitude: ${location?.longitude}"
                        if (location != null) {
                            autofillLocation(location.latitude, location.longitude)
                        }
                        else
                        {
                            Toast.makeText(requireContext(), "There was a problem filling your location information automatically", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Location Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private val liveDataObserver: Observer<UserTable> =
        Observer { userData ->
            if (userData != null) {
                currentUserId = userData.id
                firstNameTextEdit!!.setText(userData.firstName)
                lastNameTextEdit!!.setText(userData.lastName)
                ageSlider!!.value = userData.age!!.toFloat()
                cityTextEdit!!.setText(userData.city)
                val countryPosition =
                    countryAdapter!!.getPosition(userData.country)
                countrySpinner!!.setSelection(countryPosition)
                val feetPosition =
                    feetAdapter!!.getPosition((userData.height!!/12).toString())
                heightFeetSpinner!!.setSelection(feetPosition)
                val inchesPosition =
                    inchesAdapter!!.getPosition((userData.height!! % 12).toString())
                heightInchesSpinner!!.setSelection(inchesPosition)
                weightSlider!!.value = userData.weight!!.toFloat()
                val sexPosition = sexAdapter!!.getPosition(userData.sex)
                sexSpinner!!.setSelection(sexPosition)
                val activityPosition =
                    activityAdapter!!.getPosition(userData.activityLevel)
                activityLevelSpinner!!.setSelection(activityPosition)

            }
        }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun autofillLocation(locationLat: Double, locationLon: Double) {
        val gcd = Geocoder(requireContext(), Locale.getDefault())
        try {
            gcd.getFromLocation(
                locationLat, locationLon,
                10,
                gcListener
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        super.onSaveInstanceState(outState)

        firstNameValue = firstNameTextEdit!!.text.toString()
        lastNameValue = lastNameTextEdit!!.text.toString()
        val age = ageSlider!!.value.toString()
        cityValue = cityTextEdit!!.text.toString()
        countryValue = countrySpinner!!.selectedItem.toString()
        val heightFeet = heightFeetSpinner!!.selectedItem.toString()
        val heightInches = heightInchesSpinner!!.selectedItem.toString()
        val weight = weightSlider!!.value.toString()
        sexValue = sexSpinner!!.selectedItem.toString()
        activityLevelValue = activityLevelSpinner!!.selectedItem.toString()

        outState.putString("FIRST_NAME", firstNameValue)
        outState.putString("LAST_NAME", lastNameValue)
        outState.putString("AGE", age)
        outState.putString("COUNTRY", countryValue)
        outState.putString("CITY", cityValue)
        outState.putString("HEIGHT_FEET", heightFeet)
        outState.putString("HEIGHT_INCHES", heightInches)
        outState.putString("WEIGHT", weight)
        outState.putString("SEX", sexValue)
        outState.putString("ACTIVITY_LEVEL", activityLevelValue)
    }

    private fun setSpinnerData(
        spinnerId: Int,
        spinnerOptions: Array<String>,
        view: View
    ): ArrayAdapter<String?> {
        val targetSpinner = view.findViewById<Spinner>(spinnerId)
        targetSpinner.onItemSelectedListener =
            this // onItemSelectedListener tells you which item was clicked
        val targetSpinnerAdapter: ArrayAdapter<String?> = ArrayAdapter<String?>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            spinnerOptions
        )
        targetSpinnerAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )
        targetSpinner.adapter = targetSpinnerAdapter

        return targetSpinnerAdapter
    }


    /**
     * This function/method is near identical to the class example.
     * This enables the intent for the camera and image retrieval.
     */
    private val cameraActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                //val extras = result.data!!.extras
                //val thumbnailImage = extras!!["data"] as Bitmap?

                if (Build.VERSION.SDK_INT >= 33) {
                    val thumbnailImage =
                        result.data!!.getParcelableExtra("data", Bitmap::class.java)
                    mIvPic!!.setImageBitmap(thumbnailImage)
                    if (thumbnailImage != null) {
                        /** This saves the thumbnail to cache **/
                        saveBitmapToCache(thumbnailImage)
                    }
                } else {
                    val thumbnailImage = result.data!!.getParcelableExtra<Bitmap>("data")
                    mIvPic!!.setImageBitmap(thumbnailImage)

                    if (thumbnailImage != null) {
                        /** This saves the thumbnail to cache **/
                        saveBitmapToCache(thumbnailImage)
                    }
                }
            }
        }

    /**
     * This method/function is hardcoded to save the photo to the cache.
     * This is used in [cameraActivity]
     */
    private fun saveBitmapToCache(bitmap: Bitmap) {
        val fileName = "photo.png"
        val file = File(requireActivity().cacheDir, fileName)
        val outputStream = FileOutputStream(file)

        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            outputStream.close()
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

    /* BMR calculation:
    * For men: 66.47 + (6.24 × weight in pounds) + (12.7 × height in inches) − (6.75 × age in years).
    * For women: BMR = 655.51 + (4.35 × weight in pounds) + (4.7 × height in inches) - (4.7 × age in years) */
    private fun calculateBMR(feet: String?, inches:String?, weight: String?, sex:String?, age:String?, activity:String?): Int {
        if (feet.isNullOrBlank() || inches.isNullOrBlank() || weight.isNullOrBlank() || sex.isNullOrBlank()
            || age.isNullOrBlank() || activity.isNullOrBlank()) {
            return 0

        }
        val weightInt = weight.toInt()
        val ageInt = age.toInt()
        val heightInInches = feet.toInt() * 12 + inches.toInt()

        val activityFactor = if (activityFactors.containsKey(activity) && activityFactors[activity]!! > 0) activityFactors!![activity] else 1.1

        if (sex == "Female") {
            return ((655.0 + (4.35 * weightInt) + (4.7 * heightInInches) - (4.7 * ageInt)) * activityFactor!!).toInt()
        }
        if (sex == "Male") {
            return ((66.0 + (6.23 * weightInt) + (12.7 * heightInInches) - (6.8 * ageInt))* activityFactor!!).toInt()
        }
        return 0
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onClick(view: View) {
        when (view?.id) { //Added ? due to warning message. Consider better checks.
            R.id.locationButton -> {
                if (
                    ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // location permission has already been granted
                    fusedLocationClient.getCurrentLocation(PRIORITY_BALANCED_POWER_ACCURACY, null)
                        .addOnSuccessListener { location : Location? ->
                            // Got last known location. In some rare situations this can be null.
//                            val latLongText = findViewById<TextView>(R.id.latLong)
//                            latLongText.text = "Latitude: ${location?.latitude} Longitude: ${location?.longitude}"
                            if (location != null) {
                                autofillLocation(location.latitude, location.longitude)
                            }
                        }
                }
                else {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    Toast.makeText(requireContext(), "Asking permission", Toast.LENGTH_SHORT).show();
                    requestPermissionLauncher.launch(
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                }
            }
            R.id.uploadPhotoButton -> {
                //The button press should open a camera
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                try {
                    cameraActivity.launch(cameraIntent)
                } catch (ex: ActivityNotFoundException) {
                    val text = "Error: $ex"
                    val duration = Toast.LENGTH_SHORT

                    val toast = Toast.makeText(requireContext(), text, duration)
                    toast.show()
                }
            }
            R.id.submitButton -> {
                if(TextUtils.isEmpty(heightFeetSpinner!!.selectedItem.toString()) ||
                    TextUtils.isEmpty(heightInchesSpinner!!.selectedItem.toString())) {
                    Toast.makeText(requireContext(), "Height cannot be empty", Toast.LENGTH_SHORT).show();
                    return
                }

                firstNameValue = firstNameTextEdit!!.text.toString()
                lastNameValue = lastNameTextEdit!!.text.toString()
                val ageValueInt = ageSlider!!.value.toInt()
                ageValue = ageValueInt.toString()
                cityValue = cityTextEdit!!.text.toString()
                countryValue = countrySpinner!!.selectedItem.toString()
                heightFeetValue = heightFeetSpinner!!.selectedItem.toString()
                heightInchesValue = heightInchesSpinner!!.selectedItem.toString()
                val weightValueInt = weightSlider!!.value.toInt()
                weightValue = weightValueInt.toString()
                sexValue = sexSpinner!!.selectedItem.toString()
                activityLevelValue = activityLevelSpinner!!.selectedItem.toString()

                val bmrIntValue = calculateBMR(heightFeetValue, heightInchesValue, weightValue, sexValue, ageValue, activityLevelValue)
                bmrValue = if (bmrIntValue!! > 0) bmrIntValue.toString() else "BMR"

                // Save user data to main repository
                val userData = UserTable(
                    id = 0, // db should only ever have one user
                    firstName = firstNameValue,
                    lastName = lastNameValue,
                    age = ageValueInt,
                    city = cityValue,
                    country = countryValue,
                    height = heightFeetValue!!.toInt() * 12 + heightInchesValue!!.toInt(),
                    weight = weightValueInt,
                    sex = sexValue,
                    activityLevel = activityLevelValue,
                    bmr = bmrIntValue
                )

                // launch coroutine to insert user into db
                lifecycleScope.launch {
                    model.setUserData(userData)

                    // Start the profile display frag
                    dataPasser!!.passProfileData()
                }

            }

        }
    }

    override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View?, position: Int,
        id: Long
    ) {
        // access selected country using countries[position]
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    inner class GCListener : GeocodeListener {

        override fun onGeocode(addresses: List<Address>) {
            // do something with the location
            var cityName: String = "Not found"

            // get a few cities, just pick the first one that has a non-null city name
            if (addresses != null) {
                for (adrs in addresses) {
                    if (adrs != null) {
                        val city: String = adrs.locality
                        if (adrs.locality != null && adrs.locality != ""
                            && adrs.countryName != null && adrs.countryName != "") {
                            cityTextEdit?.setText(adrs.locality)
//                            countryAdapter?.getPosition(adrs.countryName)
//                                ?.let { countrySpinner?.setSelection(it) }
                            return
                        }
                    }
                }
            }
        }

        override fun onError(errorMessage: String?) {
            requireActivity().runOnUiThread {
                Toast.makeText(requireContext(), "There was a problem filling your location information automatically", Toast.LENGTH_SHORT).show()
            }
        }
    }

}