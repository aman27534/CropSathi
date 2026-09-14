sed -i '/import android.Manifest/i \
import android.content.Context\n\
import androidx.datastore.core.DataStore\n\
import androidx.datastore.preferences.core.Preferences\n\
import androidx.datastore.preferences.core.doublePreferencesKey\n\
import androidx.datastore.preferences.core.edit\n\
import androidx.datastore.preferences.core.floatPreferencesKey\n\
import androidx.datastore.preferences.preferencesDataStore\n\
import kotlinx.coroutines.flow.map\n\
' app/src/main/java/com/example/MainActivity.kt
