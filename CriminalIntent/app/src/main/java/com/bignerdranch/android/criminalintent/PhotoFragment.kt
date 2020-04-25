package com.bignerdranch.android.criminalintent

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import java.io.File

private const val ARG_PHOTO_FILE = "photo_uri"

class PhotoFragment : DialogFragment() {

    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private lateinit var fullPicture: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoFile = arguments?.getSerializable(ARG_PHOTO_FILE) as File
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dialog, container, false)

        fullPicture = view.findViewById(R.id.full_image) as ImageView

        return view
    }

    override fun onStart() {
        super.onStart()
        updatePhotoViewer()
    }

    private fun updatePhotoViewer() {
        if(photoFile.exists()) {
            val bmp = getScaledBitmap(photoFile.path, requireActivity())
            fullPicture.setImageBitmap(bmp)
        } else {
            fullPicture.setImageDrawable(null)
        }
    }

    companion object {
        fun newInstance(photoFile: File): PhotoFragment {
            val args = Bundle().apply {
                putSerializable(ARG_PHOTO_FILE, photoFile)
            }
            return PhotoFragment().apply {
                arguments = args
            }
        }
    }
}