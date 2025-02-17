package com.linn.silent_e

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.linn.silent_e.ui.theme.Silent_eTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Silent_eTheme {
                SilentApp()
            }
        }
    }
}

