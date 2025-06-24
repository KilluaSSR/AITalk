package killua.dev.aitalk.utils

import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import killua.dev.aitalk.R
import java.lang.ref.WeakReference
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BiometricHelper(private val activity: FragmentActivity) {
    private val executor = ContextCompat.getMainExecutor(activity)
    private var biometricPrompt: BiometricPrompt = BiometricPrompt(activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
            }

            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence
            ) {
                super.onAuthenticationError(errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        })
    private var promptInfo: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(activity.getString(R.string.protected_content_title))
        .setSubtitle(activity.getString(R.string.protected_content_desc))
        .setNegativeButtonText(activity.getString(R.string.cancel))
        .build()

    suspend fun authenticate(): Boolean = suspendCoroutine { continuation->
        var isHandled = false

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    if (!isHandled) {
                        isHandled = true
                        continuation.resume(true)
                    }
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    if (!isHandled) {
                        isHandled = true
                        continuation.resume(false)
                    }
                }

                override fun onAuthenticationFailed() {
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.protected_content_title))
            .setSubtitle(activity.getString(R.string.protected_content_desc))
            .setNegativeButtonText(activity.getString(R.string.cancel))
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun canAuthenticate(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Log.d("BiometricHelper", "No biometric features available on this device")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d("BiometricHelper", "Biometric features are currently unavailable")
                false
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Log.d("BiometricHelper", "No biometric credentials enrolled")
                false
            }
            else -> false
        }
    }
}

object BiometricManagerSingleton {
    private var fragmentActivity: WeakReference<FragmentActivity>? = null

    fun init(activity: FragmentActivity) {
        fragmentActivity = WeakReference(activity)
    }

    fun getBiometricHelper(): BiometricHelper? {
        return fragmentActivity?.get()?.let { BiometricHelper(it) }
    }
}