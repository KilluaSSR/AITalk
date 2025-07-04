package killua.dev.aitalk.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "DataStore"
)
fun Context.readStoreString(key: Preferences.Key<String>, defValue: String) = dataStore.data.map { preferences -> preferences[key] ?: defValue }
fun Context.readStoreBoolean(key: Preferences.Key<Boolean>, defValue: Boolean) = dataStore.data.map { preferences -> preferences[key] ?: defValue }
fun Context.readStoreInt(key: Preferences.Key<Int>, defValue: Int) = dataStore.data.map { preferences -> preferences[key] ?: defValue }
fun Context.readStoreLong(key: Preferences.Key<Long>, defValue: Long) = dataStore.data.map { preferences -> preferences[key] ?: defValue }

fun Context.readStoreDouble(key: Preferences.Key<Double>, defValue: Double) =
    dataStore.data.map { preferences -> preferences[key] ?: defValue }

suspend fun Context.saveStoreString(key: Preferences.Key<String>, value: String) = dataStore.edit { settings -> settings[key] = value }
suspend fun Context.saveStoreBoolean(key: Preferences.Key<Boolean>, value: Boolean) = dataStore.edit { settings -> settings[key] = value }
suspend fun Context.saveStoreInt(key: Preferences.Key<Int>, value: Int) = dataStore.edit { settings -> settings[key] = value }
suspend fun Context.saveStoreLong(key: Preferences.Key<Long>, value: Long) = dataStore.edit { settings -> settings[key] = value }
suspend fun Context.saveStoreDouble(key: Preferences.Key<Double>, value: Double) =
    dataStore.edit { settings -> settings[key] = value }