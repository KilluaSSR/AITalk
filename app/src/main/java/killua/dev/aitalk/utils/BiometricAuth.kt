package killua.dev.aitalk.utils

import androidx.navigation.NavController

class BiometricAuth {
    private fun getBiometricHelper(): BiometricHelper? {
        return BiometricManagerSingleton.getBiometricHelper()
    }

    suspend fun authenticate(): Boolean {
        val biometricHelper = getBiometricHelper()
        if (biometricHelper == null) {
            return false
        }

        return if (biometricHelper.canAuthenticate()) {
            biometricHelper.authenticate()
        } else {
            false
        }
    }

    suspend fun authenticateAndNavigate(
        navController: NavController,
        route: String,
    ) {
        if (authenticate()) {
            navController.navigate(route)
        }
    }
}