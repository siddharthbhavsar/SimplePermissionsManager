package permutil

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import util.PreferencesUtil

//Grabbed this from online
//https://stackoverflow.com/questions/32347532/android-m-permissions-confused-on-the-usage-of-shouldshowrequestpermissionrati
object PermissionUtil {
    /*
    * Check if version is marshmallow and above.
    * Used in deciding to ask runtime permission
    * */
    private fun shouldAskPermission(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    private fun shouldAskPermission(context: Context, permission: String): Boolean {
        if (shouldAskPermission()) {
            val permissionResult = ActivityCompat.checkSelfPermission(context, permission)
            if (permissionResult != PackageManager.PERMISSION_GRANTED) {
                return true
            }
        }
        return false
    }

    @SuppressLint("NewApi")
    fun checkPermission(
        context: AppCompatActivity,
        permission: String,
        listener: PermissionAskListener
    ) {
        /*
        * If permission is not granted
        * */
        if (shouldAskPermission(context, permission)) {
            /*
            * If permission denied previously
            * */
            if (context.shouldShowRequestPermissionRationale(permission)) {
                listener.showRationale()
            } else {
                /*
                * Permission denied or first time requested
                * */
                if (PreferencesUtil.isFirstTimeAskingPermission(context, permission)) {
                    PreferencesUtil.firstTimeAskingPermission(context, permission, false)
                    listener.onNeedPermission()
                } else {
                    /*
                    * Handle the feature without permission or ask user to manually allow permission
                    * */
                    listener.onPermissionDisabled()
                }
            }
        } else {
            listener.onPermissionGranted()
        }
    }

    fun isGranted(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /*
    * Callback on various cases on checking permission
    *
    * 1.  Below M, runtime permission not needed. In that case onPermissionGranted() would be called.
    *     If permission is already granted, onPermissionGranted() would be called.
    *
    * 2.  Above M, if the permission is being asked first time onNeedPermission() would be called.
    *
    * 3.  Above M, if the permission is previously asked but not granted, onPermissionPreviouslyDenied()
    *     would be called.
    *
    * 4.  Above M, if the permission is disabled by device policy or the user checked "Never ask again"
    *     check box on previous request permission, onPermissionDisabled() would be called.
    * */
    interface PermissionAskListener {
        /*
        * Callback to ask permission
        * */
        fun onNeedPermission()

        /*
        * Callback on permission denied
        * */
        fun showRationale()

        /*
        * Callback on permission "Never show again" checked and denied
        * */
        fun onPermissionDisabled()

        /*
        * Callback on permission granted
        * */
        fun onPermissionGranted()
    }
}