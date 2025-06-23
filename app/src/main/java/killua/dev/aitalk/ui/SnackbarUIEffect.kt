package killua.dev.aitalk.ui

import androidx.compose.material3.SnackbarDuration
import killua.dev.aitalk.ui.viewmodels.base.UIEffect

sealed interface SnackbarUIEffect : UIEffect {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val withDismissAction: Boolean = false,
        val duration: SnackbarDuration = if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite,
        val onActionPerformed: (suspend () -> Unit)? = null,
        val onDismissed: (suspend () -> Unit)? = null,
    ) : SnackbarUIEffect

    data object DismissSnackbar : SnackbarUIEffect
}