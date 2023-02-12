package permutil

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import base.CoreActivity

object PermissionManager {

    private lateinit var currentPermissionMessage: String
    private lateinit var currentPermissionTitle: String
    private lateinit var currentPermissionCallback: () -> Unit
    private var currentPermissionDeniedCallback: (() -> Unit)? = null
    private val permRequestCodes: HashMap<String, Int> = HashMap()
    private var requestCodeInts = 0

    fun permissionGate(
        context: CoreActivity,
        manifestPermissionId: String,
        granted: () -> Unit,
        deniedCallback: (() -> Unit)? = null,
        title: String = "Needs permission",
        message: String = "App needs your permission, tap OK to retry request"
    ) {

        //Save the state for callback
        currentPermissionCallback = granted
        currentPermissionDeniedCallback = deniedCallback
        currentPermissionTitle = title
        currentPermissionMessage = message

        checkPermission(manifestPermissionId, context)
    }

    private fun checkPermission(
        manifestPermissionId: String,
        context: AppCompatActivity
    ) {
        if (!permRequestCodes.containsKey(manifestPermissionId)) {
            permRequestCodes[manifestPermissionId] = requestCodeInts++
        }
        PermissionUtil.checkPermission(
            context,
            manifestPermissionId,
            object : PermissionUtil.PermissionAskListener {
                override fun onNeedPermission() {
                    ActivityCompat.requestPermissions(
                        context,
                        arrayOf(manifestPermissionId),
                        permRequestCodes[manifestPermissionId]!!
                    )
                }

                override fun showRationale() {
                    //show a dialog explaining permission and then request permission
                    getConfirmation(
                        message = currentPermissionMessage,
                        positiveBtn = "Ok",
                        title = currentPermissionTitle,
                        onConfirm = {
                            ActivityCompat.requestPermissions(
                                context,
                                arrayOf(manifestPermissionId),
                                permRequestCodes[manifestPermissionId]!!
                            )
                        },
                        onCancel = {
                            currentPermissionDeniedCallback?.invoke()
                        })
                }

                override fun onPermissionDisabled() {
                    currentPermissionDeniedCallback?.invoke()
                    //Take to settings
                    Toast.makeText(context, "Permission Disabled", Toast.LENGTH_SHORT).show()
                    getConfirmation(
                        title = "Permission Disabled",
                        message = "App needs your permission",
                        positiveBtn = "Go to settings",
                        onConfirm = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri: Uri = Uri.fromParts("package", context.packageName, null)
                            intent.data = uri
                            context.startActivity(intent)
                        })
                }

                override fun onPermissionGranted() {
                    currentPermissionCallback.invoke()
                }
            })
    }

    private fun getConfirmation(
        message: String,
        positiveBtn: String,
        title: String,
        onConfirm: () -> Unit,
        onCancel: (() -> Unit)? = null
    ) {

    }

    fun processResult(
        requestCode: Int,
        activity: AppCompatActivity
    ) {
        if (permRequestCodes.containsValue(requestCode)) {

            //Restore from state
            checkPermission(
                permRequestCodes.filterValues { it == requestCode }.keys.first(),
                activity
            )
        }
    }
}