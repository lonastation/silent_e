package com.linn.slient_e

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.linn.slient_e.ui.theme.Slient_eTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Slient_eTheme {
                SlientApp()
            }
        }
    }
}

