package com.rosan.dhizuku.ui.activity

import android.os.Bundle
import android.widget.Toast

import androidx.activity.ComponentActivity

import com.rosan.dhizuku.server.DhizukuDAReceiver
import com.rosan.dhizuku.R

import org.koin.core.component.KoinComponent

class FinalizeActivity : ComponentActivity(), KoinComponent {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DhizukuDAReceiver.grantPermissions(this)
        setResult(RESULT_OK)
        Toast.makeText(this, getString(R.string.home_status_owner_granted), Toast.LENGTH_LONG).show()
        finish()
    }
}
