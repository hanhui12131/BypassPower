package com.luckyzyx.bypasspower

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.topjohnwu.superuser.ipc.RootService
import java.io.File

object BypassPowerService {
    private const val TAG = "BypassPowerService"
    private var controller: IBypassPowerController? = null

    fun get(context: Context, result: (IBypassPowerController?) -> Unit) {
        val current = controller
        if (current != null) {
            result(current)
            return
        }
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                controller = IBypassPowerController.Stub.asInterface(service)
                result(controller)
            }

            override fun onServiceDisconnected(name: ComponentName?) = Unit
        }
        RootService.bind(Intent(context, Controller::class.java), connection)
    }

    class Controller : RootService() {
        override fun onBind(intent: Intent): IBinder = ControllerBinder()
    }

    private class ControllerBinder : IBypassPowerController.Stub() {
        override fun checkBypassMode(): Boolean {
            return try {
                node.exists()
            } catch (t: Throwable) {
                Log.e(TAG, "checkBypassMode", t)
                false
            }
        }

        override fun getBypassMode(): Boolean {
            return try {
                when (node.bufferedReader().readLine()?.replace(" ", "")?.substring(0, 1)
                    ?.toIntOrNull()) {
                    1 -> false
                    0 -> true
                    else -> false
                }
            } catch (t: Throwable) {
                Log.e(TAG, "getBypassMode", t)
                false
            }
        }

        override fun setBypassMode(status: Boolean) {
            try {
                if (node.exists()) node.writeText(if (status) "0" else "1")
            } catch (t: Throwable) {
                Log.e(TAG, "setBypassMode", t)
            }
        }

        companion object {
            private val node = File("/sys/devices/virtual/oplus_chg/battery/mmi_charging_enable")
        }
    }
}
