package com.luckyzyx.bypasspower

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

class BypassPowerModeTile : TileService() {
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_POWER_CONNECTED
                || intent.action == Intent.ACTION_POWER_DISCONNECTED
            ) {
                refreshBypass()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        registerPowerReceiver(receiver, filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onStartListening() {
        super.onStartListening()
        refreshFromService()
    }

    override fun onClick() {
        when (currentState()) {
            Tile.STATE_ACTIVE -> runRemote { it.bypassMode = false }
            Tile.STATE_INACTIVE -> {
                if (isCharging()) runRemote { it.bypassMode = true }
                else refreshFromService()
            }
            else -> refreshFromService()
        }
    }

    private fun isCharging(): Boolean {
        val intent = registerPowerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun refreshBypass() {
        if (isCharging()) {
            refreshFromService()
            return
        }
        runRemote { it.bypassMode = false }
    }

    private fun refreshFromService() {
        runRemote { }
    }

    private fun runRemote(action: (IBypassPowerController) -> Unit) {
        request(action, allowRetry = true)
    }

    private fun request(action: (IBypassPowerController) -> Unit, allowRetry: Boolean) {
        BypassPowerService.get(this) { remote ->
            val state = remote?.let { use(it, action) }
            if (state != null) {
                publish(state)
                return@get
            }
            if (!allowRetry) {
                publish(Tile.STATE_UNAVAILABLE)
                return@get
            }
            BypassPowerService.invalidate()
            request(action, allowRetry = false)
        }
    }

    private fun use(remote: IBypassPowerController, action: (IBypassPowerController) -> Unit): Int? {
        if (!remote.asBinder().isBinderAlive) return null
        return try {
            action(remote)
            when {
                !remote.checkBypassMode() -> Tile.STATE_UNAVAILABLE
                remote.bypassMode -> Tile.STATE_ACTIVE
                else -> Tile.STATE_INACTIVE
            }
        } catch (t: Throwable) {
            Log.e(TAG, "use", t)
            null
        }
    }

    private fun currentState(): Int {
        return try {
            qsTile?.state ?: Tile.STATE_UNAVAILABLE
        } catch (t: Throwable) {
            Log.e(TAG, "currentState", t)
            Tile.STATE_UNAVAILABLE
        }
    }

    private fun publish(state: Int) {
        try {
            val tile = qsTile ?: return
            tile.state = state
            tile.updateTile()
        } catch (t: Throwable) {
            Log.e(TAG, "publish", t)
        }
    }

    private fun registerPowerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
    }

    companion object {
        private const val TAG = "BypassPowerModeTile"
    }
}
