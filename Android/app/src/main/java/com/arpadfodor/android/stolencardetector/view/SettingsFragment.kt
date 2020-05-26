package com.arpadfodor.android.stolencardetector.view

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.arpadfodor.android.stolencardetector.R
import com.arpadfodor.android.stolencardetector.model.ai.StolenVehicleRecognizerService
import com.arpadfodor.android.stolencardetector.model.db.DatabaseService
import com.arpadfodor.android.stolencardetector.view.utils.AppSnackBarBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.*

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var settingsMaximumRecognitions = ""
    private var settingsMinimumPredictionCertainty = ""
    private var settingsShowReceptiveField = ""
    private var settingsAutoSync = ""
    private var settingsLastSynced = ""

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {

        super.onResume()

        settingsMaximumRecognitions = getString(R.string.SETTINGS_NUM_RECOGNITIONS)
        settingsMinimumPredictionCertainty = getString(R.string.SETTINGS_MINIMUM_PREDICTION_CERTAINTY)
        settingsShowReceptiveField = getString(R.string.SETTINGS_SHOW_RECEPTIVE_FIELD)
        settingsAutoSync = getString(R.string.SETTINGS_AUTO_SYNC)
        settingsLastSynced = getString(R.string.LAST_SYNCED_DB)
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this.requireContext())

        val syncButton: Preference? = findPreference(getString(R.string.SETTINGS_SYNC_NOW))

        syncButton?.onPreferenceClickListener = Preference.OnPreferenceClickListener {

            DatabaseService.updateFromApi{ isSuccess ->

                if(isSuccess){

                    val currentTime = Calendar.getInstance().time.toString()
                    preferences.edit().putString(getString(R.string.LAST_SYNCED_DB), currentTime)
                        .apply()

                    AppSnackBarBuilder.buildSuccessSnackBar(resources, this.requireView(),
                        getString(R.string.updated_database), Snackbar.LENGTH_SHORT).show()

                }
                else{
                    AppSnackBarBuilder.buildAlertSnackBar(resources, this.requireView(),
                        getString(R.string.updating_failed), Snackbar.LENGTH_SHORT).show()
                }

                StolenVehicleRecognizerService.initialize()

            }
            true

        }

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
                resources.getInteger(R.integer.settings_num_recognitions_default)
            ))

            remove(settingsMinimumPredictionCertainty)
            putInt(settingsMinimumPredictionCertainty, sharedPreferences.getInt(settingsMinimumPredictionCertainty,
                resources.getInteger(R.integer.settings_minimum_prediction_certainty_default)
            ))

            remove(settingsShowReceptiveField)
            putBoolean(settingsShowReceptiveField, sharedPreferences.getBoolean(settingsShowReceptiveField,
                resources.getBoolean(R.bool.settings_receptive_field_default)
            ))

            remove(settingsAutoSync)
            putBoolean(settingsAutoSync, sharedPreferences.getBoolean(settingsAutoSync,
                resources.getBoolean(R.bool.settings_auto_sync_default)
            ))

            remove(settingsLastSynced)
            putString(settingsLastSynced, sharedPreferences.getString(settingsLastSynced,
                resources.getString(R.string.settings_last_synced_default)
            ))

            apply()

        }

    }

}