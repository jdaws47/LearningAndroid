package com.bignerdranch.android.criminalintent

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import java.util.*

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val REQUEST_DATE = 0
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_TIME = 1
private const val DATE_FORMAT = "EEE, MMM, dd"
private const val REQUEST_CONTACT = 2
private const val PERMISSIONS_REQUEST_READ_CONTACTS = 3


class CrimeFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {

    private lateinit var crime: Crime

    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var requiresPoliceCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
    private lateinit var confrontButton: Button
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        Log.d(TAG, "args bundle crime ID: $crimeId")
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        timeButton = view.findViewById(R.id.crime_time) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        requiresPoliceCheckBox = view.findViewById(R.id.crime_requires_police) as CheckBox
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
        confrontButton = view.findViewById(R.id.crime_confront) as Button

        /*dateButton.apply {
            text = crime.date.toString()
            isEnabled = false
        }*/


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                crime?.let {
                    this.crime = crime
                    updateUI()
                }
            })
    }

    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // This space intentionally left blank
            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(sequence: Editable?) {
                // This one too
            }
        }
        titleField.addTextChangedListener(titleWatcher)

        solvedCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        requiresPoliceCheckBox.apply {
            setOnCheckedChangeListener { _, isChecked ->
                crime.requiresPolice = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)
            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject))
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
                startActivity(chooserIntent)
            }
        }

        suspectButton.apply {
            val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)

            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)
            if (resolvedActivity == null)
                isEnabled = false
        }

        confrontButton.setOnClickListener {
            // check for contact permissions
            if (ContextCompat.checkSelfPermission(this.context!!, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
                // request if they have not been given yet
                requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS),
                    PERMISSIONS_REQUEST_READ_CONTACTS)
            } else {
                // already have permissions, so proceed with confrontation
                confrontSuspect()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "request permissions have a result")
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_CONTACTS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    confrontSuspect()
                } else {
                    // do nothing, disabling confront button would prevent user
                    // from ever granting permission without relaunching the app
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    override fun onTimeSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    private fun updateUI() {
        titleField.setText(crime.title)

        dateButton.text = android.text.format.DateFormat.format("EEEE, MMM dd, yyyy", crime.date)
        timeButton.text = android.text.format.DateFormat.format("h:mm a", crime.date)

        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }

        requiresPoliceCheckBox.apply {
            isChecked = crime.requiresPolice
            jumpDrawablesToCurrentState()
        }

        if(crime.suspect.isNotBlank()) {
            suspectButton.text = crime.suspect
        }

        confrontButton.isEnabled = crime.suspect.isNotBlank() /*&&
                (ContextCompat.checkSelfPermission(this.context!!,
                    Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)*/
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when {
            resultCode != Activity.RESULT_OK -> return

            requestCode == REQUEST_CONTACT && data != null -> {
                val contactUri: Uri = data.data ?: return
                //specify which fields you want your query to return values for
                val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                //perform your query - the contractUri is like a where clause here
                val cursor = requireActivity().contentResolver
                    .query(contactUri, queryFields, null, null, null)
                cursor?.use{
                    //verify cursor contains at least one result
                    if(it.count == 0)
                        return

                    // Pull out the first column of the first row of data -
                    // that is your suspect's name
                    it.moveToFirst()
                    val suspect = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    crime.suspect = suspect
                    crimeDetailViewModel.saveCrime(crime)
                    suspectButton.text = suspect
                }
            }
        }
    }

    private fun getCrimeReport(): String {
        var solvedString = if(crime.isSolved) getString(R.string.crime_report_solved)
                            else getString(R.string.crime_report_unsolved)
        val dateString = android.text.format.DateFormat.format(DATE_FORMAT, crime.date)
        var suspect = if(crime.suspect.isBlank()) getString(R.string.crime_report_no_suspect)
                        else getString(R.string.crime_report_suspect, crime.suspect)

        return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
    }

    private fun confrontSuspect() {
        // query for the contact id of the row based on the suspect's name
        val uri: Uri = ContactsContract.Contacts.CONTENT_URI
        val queryFields = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.HAS_PHONE_NUMBER)
        val searchQuery = "${ContactsContract.Contacts.DISPLAY_NAME}=\"${crime.suspect}\""
        val cursor = requireActivity().contentResolver
            .query(uri, queryFields, searchQuery, null, null)

        cursor?.use{
            it.moveToFirst()
            //verify cursor contains at least one result that has a phone number
            if(it.count != 0 && it.getInt(it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) == 1) {
                it.moveToFirst()
                val id = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))

                // query for phone number using the contact id we just got
                val phoneUri:Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                val queryFields = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val searchQuery = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID}=\"${Uri.encode(id)}\""
                val cursor = requireActivity().contentResolver
                    .query(phoneUri, queryFields, searchQuery, null, null)

                cursor?.use {
                    if(it.count != 0) {
                        it.moveToFirst()
                        val phone_number =
                            it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                        // pass the number to the default call app to dial
                        // calling app waits waits for user action to make call
                        val callIntent =
                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone_number))
                        startActivity(callIntent)
                    } else {
                        Toast.makeText(context, "No phone number could be found", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "No phone number could be found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }

}