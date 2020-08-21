package com.arpadfodor.stolenvehicledetector.android.app.view

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.arpadfodor.stolenvehicledetector.android.app.ApplicationRoot
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.model.AuthenticationService
import com.arpadfodor.stolenvehicledetector.android.app.model.ai.VehicleRecognizerService
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.GeneralRepository
import com.arpadfodor.stolenvehicledetector.android.app.model.repository.UserReportRepository
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppSnackBarBuilder
import com.google.android.material.snackbar.Snackbar
import java.util.*

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private var settingsKeepScreenAlive = ""
    private var settingsMaximumRecognitions = ""
    private var settingsMinimumPredictionCertainty = ""
    private var settingsShowReceptiveField = ""
    private var settingsAutoSync = ""
    private var settingsLastSyncedDbVehicles = ""
    private var settingsLastSyncedDbReports = ""

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onResume() {

        super.onResume()

        settingsKeepScreenAlive = getString(R.string.SETTINGS_KEEP_SCREEN_ALIVE)
        settingsMaximumRecognitions = getString(R.string.SETTINGS_NUM_RECOGNITIONS)
        settingsMinimumPredictionCertainty = getString(R.string.SETTINGS_MINIMUM_PREDICTION_CERTAINTY)
        settingsShowReceptiveField = getString(R.string.SETTINGS_SHOW_RECEPTIVE_FIELD)
        settingsAutoSync = getString(R.string.SETTINGS_AUTO_SYNC)
        settingsLastSyncedDbVehicles = getString(R.string.LAST_SYNCED_DB_VEHICLES)
        settingsLastSyncedDbReports = getString(R.string.LAST_SYNCED_DB_REPORTS)

        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this.requireContext())
        
        updatePreferenceUpdatedTimestamp(preferences)

        val syncButton: Preference? = findPreference(getString(R.string.SETTINGS_SYNC_NOW))
        val deleteUserReportsButton: Preference? = findPreference(getString(R.string.SETTINGS_DELETE_USER_REPORTS))

        syncButton?.onPreferenceClickListener = Preference.OnPreferenceClickListener {

            GeneralRepository.updateAll{ isVehiclesSuccess, isReportsSuccess ->

                    if(isVehiclesSuccess){
                        val currentTime = Calendar.getInstance().time.toString()
                        preferences.edit().putString(getString(R.string.LAST_SYNCED_DB_VEHICLES), currentTime)
                            .apply()
                    }
                    VehicleRecognizerService.initialize()

                    if(isReportsSuccess){
                        val currentTime = Calendar.getInstance().time.toString()
                        preferences.edit().putString(getString(R.string.LAST_SYNCED_DB_REPORTS), currentTime)
                            .apply()
                    }

                    dbUpdateResultSnackBar(isVehiclesSuccess && isReportsSuccess)

            }

            true

        }

        deleteUserReportsButton?.onPreferenceClickListener = Preference.OnPreferenceClickListener {

            val user = AuthenticationService.userName

            UserReportRepository.deleteAllFromUser(user){ isSuccess ->
                dbDeleteResultSnackBar(isSuccess)
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
        sharedPreferences?.let {
            changeSettings(sharedPreferences){
                updatePreferenceUpdatedTimestamp(sharedPreferences)
            }
        }
    }

    private fun changeSettings(sharedPreferences: SharedPreferences?, callback: (Boolean) -> Unit) {

        sharedPreferences?: return

        with (sharedPreferences.edit()) {

            remove(settingsKeepScreenAlive)
            val keepScreenOn = sharedPreferences.getBoolean(settingsKeepScreenAlive,
                resources.getBoolean(R.bool.settings_keep_screen_alive_default)
            )
            putBoolean(settingsKeepScreenAlive, keepScreenOn)
            ApplicationRoot.keepScreenAlive = keepScreenOn

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

            remove(settingsLastSyncedDbVehicles)
            val updatedDbVehiclesTimestamp = sharedPreferences.getString(settingsLastSyncedDbVehicles,
                resources.getString(R.string.settings_last_synced_default)).toString()
            putString(settingsLastSyncedDbVehicles, updatedDbVehiclesTimestamp)

            remove(settingsLastSyncedDbReports)
            val updatedDbReportsTimestamp = sharedPreferences.getString(settingsLastSyncedDbReports,
                resources.getString(R.string.settings_last_synced_default)).toString()
            putString(settingsLastSyncedDbReports, updatedDbReportsTimestamp)

            apply()
        }

        callback(true)

    }

    private fun updatePreferenceUpdatedTimestamp(sharedPreferences: SharedPreferences){

        val dbVehiclesTimestamp = sharedPreferences.getString(settingsLastSyncedDbVehicles,
            resources.getString(R.string.settings_last_synced_default)).toString()
        preferenceManager.findPreference<Preference>(getString(R.string.LAST_SYNCED_DB_VEHICLES))?.
        summary = dbVehiclesTimestamp

        val dbReportsTimestamp = sharedPreferences.getString(settingsLastSyncedDbReports,
            resources.getString(R.string.settings_last_synced_default)).toString()
        preferenceManager.findPreference<Preference>(getString(R.string.LAST_SYNCED_DB_REPORTS))?.
        summary = dbReportsTimestamp

    }

    private fun dbUpdateResultSnackBar(isSuccess: Boolean){

        if(isSuccess){
            AppSnackBarBuilder.buildSuccessSnackBar(requireContext().applicationContext, this.requireView(),
                getString(R.string.updated_database), Snackbar.LENGTH_SHORT).show()
        }
        else{
            AppSnackBarBuilder.buildAlertSnackBar(requireContext().applicationContext, this.requireView(),
                getString(R.string.updating_failed), Snackbar.LENGTH_SHORT).show()
        }

    }

    private fun dbDeleteResultSnackBar(isSuccess: Boolean){

        if(isSuccess){
            AppSnackBarBuilder.buildInfoSnackBar(requireContext().applicationContext, this.requireView(),
                getString(R.string.deleted_database), Snackbar.LENGTH_SHORT).show()
        }
        else{
            AppSnackBarBuilder.buildAlertSnackBar(requireContext().applicationContext, this.requireView(),
                getString(R.string.deleting_failed), Snackbar.LENGTH_SHORT).show()
        }

    }

}