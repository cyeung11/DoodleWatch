package com.jkjk.doodlewatch

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

/**
 *Created by chrisyeung on 28/5/2019.
 */

class PermissionManager(private val act: Activity,
                        private val listener: PermissionListener
) {

    fun obtainPermission(permission: String): Boolean {
        val requestCode = when (permission) {
            Manifest.permission.WRITE_EXTERNAL_STORAGE -> FILE_PERMISSION_REQUEST_CODE
            else -> 0
        }

        if (ContextCompat.checkSelfPermission(act, permission) != PackageManager.PERMISSION_GRANTED) {
            if (act.shouldShowRequestPermissionRationale(permission)) {
                // Permission has been reject
                showPermissionDialog(permission, requestCode, true)
            } else {
                // Permission has not been asked / Don't ask again has been checked
                act.requestPermissions(arrayOf(permission), requestCode)
            }
            return false
        } else return true // Lollipop
    }

    fun onActivityResult(requestCode: Int): Boolean {
        when (requestCode) {
            FILE_PERMISSION_REQUEST_CODE -> {
                if (obtainPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    listener.onPermissionGrant(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                return true
            }
        }
        return false
    }

    fun onPermissionResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray): Boolean {
        when (requestCode) {
            FILE_PERMISSION_REQUEST_CODE -> {
                val targetIndex = permissions.indexOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (targetIndex >= 0 && grantResults[targetIndex] == PackageManager.PERMISSION_GRANTED) {
                    listener.onPermissionGrant(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    showPermissionDialog(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            requestCode,
                            act.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    )
                }
                return true
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showPermissionDialog(permission: String, requestCode: Int, getPermissionAfterConfirm: Boolean) {
        AlertDialog.Builder(act)
            .setPositiveButton(R.string.grant) { _, _ ->
                    if (getPermissionAfterConfirm) {

                        act.requestPermissions(arrayOf(permission), requestCode)
                    } else {
                        val permissionPageIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", act.packageName, null))

                        act.startActivityForResult(permissionPageIntent, requestCode)

                    }
                }
            .setNegativeButton(R.string.cancel) { _, _ ->
                listener.onPermissionReject(permission)
            }
            .setMessage(act.getString(R.string.file_permission_msg))
            .show()
    }


    companion object {
        const val FILE_PERMISSION_REQUEST_CODE = 11
    }

    interface PermissionListener {
        fun onPermissionGrant(permission: String)
        fun onPermissionReject(permission: String)
    }
}