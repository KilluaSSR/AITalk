package killua.dev.aitalk.ui.components

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

@Composable
fun BaseTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = OutlinedTextFieldDefaults.shape,
    singleLine: Boolean = true
) {

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        trailingIcon = trailingIcon,
        label = label,
        enabled = enabled,
        shape = shape,
        modifier = modifier,
        singleLine = singleLine,
    )
}