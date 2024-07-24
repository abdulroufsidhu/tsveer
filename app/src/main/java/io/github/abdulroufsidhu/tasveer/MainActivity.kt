package io.github.abdulroufsidhu.tasveer

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import io.github.abdulroufsidhu.tasveer.data.MainViewModel
import io.github.abdulroufsidhu.tasveer.operations.CacheCleaner
import io.github.abdulroufsidhu.tasveer.operations.ConnectionListener
import io.github.abdulroufsidhu.tasveer.ui.theme.TasveerTheme

class MainActivity : ComponentActivity() {
    private val vm by lazy { ViewModelProvider(this)[MainViewModel::class.java] }
    private val cm by lazy { ConnectionListener(this) }

    override fun onResume() {
        super.onResume()
        cm.startListening(
            onAvailable = { vm.fetch() },
            onUnavailable = {
                Toast.makeText(this, "internet unavailable", Toast.LENGTH_SHORT).show()
            },
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.getPreferences(Context.MODE_PRIVATE).apply {
            with(edit()) {
                val scheduled = getBoolean("CacheCleanerScheduled", false)
                if (!scheduled) {
                    CacheCleaner.schedule(this@MainActivity)
                    putBoolean("CacheCleanerScheduled", true)
                    apply()
                }
            }
        }

        vm.fetch()
        enableEdgeToEdge()
        setContent {
            TasveerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        io.github.abdulroufsidhu.tasveer.ui.screens.List(vm = vm)
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        cm.stopListening()
    }
}