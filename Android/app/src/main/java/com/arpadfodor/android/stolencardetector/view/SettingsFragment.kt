package com.arpadfodor.android.stolencardetector.view

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.arpadfodor.android.stolencardetector.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

}