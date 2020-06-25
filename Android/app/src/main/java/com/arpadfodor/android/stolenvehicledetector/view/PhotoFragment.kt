package com.arpadfodor.android.stolenvehicledetector.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.arpadfodor.android.stolenvehicledetector.R
import com.bumptech.glide.Glide
import java.io.File

/**
 * Fragment used for each individual page showing a photo inside of [GalleryFragment]
 */
class PhotoFragment internal constructor() : Fragment() {

    companion object {

        private const val FILE_NAME_KEY = "file_name"

        fun create(image: File) = PhotoFragment()
            .apply {
            arguments = Bundle().apply {
                putString(FILE_NAME_KEY, image.absolutePath)
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = ImageView(context)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments ?: return
        val resource = args.getString(FILE_NAME_KEY)?.let{ File(it) } ?: R.drawable.ic_photo
        Glide.with(view).load(resource).into(view as ImageView)
    }

}
