package com.arpadfodor.android.stolencardetector.view

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.arpadfodor.android.stolencardetector.R

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var settingsMaximumRecognitions = ""
    private var settingsMinimumPredictionCertainty = ""
    private var settingsShowReceptiveField = ""

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {
        super.onResume()
        settingsMaximumRecognitions = getString(R.string.SETTINGS_MAXIMUM_RECOGNITIONS)
        settingsMinimumPredictionCertainty = getString(R.string.SETTINGS_MINIMUM_PREDICTION_CERTAINTY)
        settingsShowReceptiveField = getString(R.string.SETTINGS_SHOW_RECEPTIVE_FIELD)
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    /**
     * Called when a shared preference is changed, added, or removed.
     * This may be called even if a preference is set to its existing value.
     * This callback will be run on main thread.
     * Note: This callback will not be triggered when preferences are cleared via[Editor.clear].
     *
     * @param sharedPreferences The [SharedPreferences] that received the change
     * @param key The key of the preference that was changed, added, or removed
     **/
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        changeSettings(sharedPreferences)
    }

    private fun changeSettings(sharedPreferences: SharedPreferences?) {

        sharedPreferences?: return

        with (sharedPreferences.edit()) {

            remove(settingsMaximumRecognitions)
            putInt(settingsMaximumRecognitions, sharedPreferences.getInt(settingsMaximumRecognitions,
                resources.getInteger(R.integer.settings_maximum_recognitions_default)
            ))

            remove(settingsMinimumPredictionCertainty)
            putInt(settingsMinimumPredictionCertainty, sharedPreferences.getInt(settingsMinimumPredictionCertainty,
                resources.getInteger(R.integer.settings_minimum_prediction_certainty_default)
            ))

            remove(settingsShowReceptiveField)
            putBoolean(settingsShowReceptiveField, sharedPreferences.getBoolean(settingsShowReceptiveField,
                resources.getBoolean(R.bool.settings_receptive_field_default)
            ))

            apply()

        }

    }

}