package com.luckyzyx.bypasspower

import android.app.Activity
import android.content.ComponentName
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.app.StatusBarManager

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.add_tile).setOnClickListener { requestAddTile() }
    }

    private fun requestAddTile() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(this, R.string.add_tile_manually, Toast.LENGTH_LONG).show()
            return
        }
        val manager = getSystemService(StatusBarManager::class.java)
        manager.requestAddTileService(
            ComponentName(this, BypassPowerModeTile::class.java),
            getString(R.string.bypass_power),
            Icon.createWithResource(this, R.drawable.baseline_power_24),
            mainExecutor
        ) { result ->
            val message = when (result) {
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED,
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED -> R.string.add_tile_done
                else -> R.string.add_tile_failed
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
