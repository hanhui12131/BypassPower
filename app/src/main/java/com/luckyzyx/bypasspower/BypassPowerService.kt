package com.luckyzyx.bypasspower

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.topjohnwu.superuser.ipc.RootService
import java.io.File

object BypassPowerService {
    private const val TAG = "BypassPowerService"
    private val mainHandler = Handler(Looper.getMainLooper())
    private var controller: IBypassPowerController? = null
    private var connection: ServiceConnection? = null
    private var watchedBinder: IBinder? = null
    private var deathRecipient: IBinder.DeathRecipient? = null
    private val pending = ArrayList<(IBypassPowerController?) -> Unit>()

    fun get(context: Context, result: (IBypassPowerController?) -> Unit) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            val appContext = context.applicationContext
            mainHandler.post { get(appContext, result) }
            return
        }
        val current = controller
        if (current != null && current.asBinder().isBinderAlive) {
            result(current)
            return
        }
        if (current != null) discardConnection()
        pending.add(result)
        if (connection != null) return
        bind(context.applicationContext)
    }

    fun invalidate() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post { invalidate() }
            return
        }
        discardConnection()
    }

    private fun bind(context: Context) {
        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                if (connection !== this) return
                accept(service)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                if (connection !== this) return
                Log.w(TAG, "root service disconnected")
                forgetBinder()
                controller = null
                connection = null
                deliver(null)
            }

            override fun onNullBinding(name: ComponentName?) {
                if (connection !== this) return
                Log.e(TAG, "null binding")
                forgetBinder()
                controller = null
                connection = null
                deliver(null)
            }
        }
        connection = conn
        try {
            RootService.bind(Intent(context, Controller::class.java), conn)
        } catch (t: Throwable) {
            Log.e(TAG, "bind", t)
            if (connection === conn) {
                connection = null
                deliver(null)
            }
        }
    }

    private fun accept(service: IBinder?) {
        if (service == null || !service.isBinderAlive) {
            discardConnection()
            deliver(null)
            return
        }
        val recipient = IBinder.DeathRecipient {
            mainHandler.post {
                if (watchedBinder !== service) return@post
                Log.w(TAG, "root binder died")
                discardConnection()
                deliver(null)
            }
        }
        deathRecipient = recipient
        watchedBinder = service
        try {
            service.linkToDeath(recipient, 0)
        } catch (t: Throwable) {
            Log.e(TAG, "linkToDeath", t)
            discardConnection()
            deliver(null)
            return
        }
        controller = IBypassPowerController.Stub.asInterface(service)
        deliver(controller)
    }

    private fun discardConnection() {
        val stale = connection
        forgetBinder()
        controller = null
        connection = null
        if (stale == null) return
        unbindQuietly(stale)
    }

    private fun unbindQuietly(conn: ServiceConnection) {
        try {
            RootService.unbind(conn)
        } catch (t: Throwable) {
            Log.e(TAG, "unbind", t)
        }
    }

    private fun forgetBinder() {
        val binder = watchedBinder
        val recipient = deathRecipient
        watchedBinder = null
        deathRecipient = null
        if (binder == null || recipient == null) return
        try {
            binder.unlinkToDeath(recipient, 0)
        } catch (_: Throwable) {
        }
    }

    private fun deliver(value: IBypassPowerController?) {
        if (pending.isEmpty()) return
        val callbacks = pending.toList()
        pending.clear()
        for (callback in callbacks) {
            try {
                callback(value)
            } catch (t: Throwable) {
                Log.e(TAG, "callback", t)
            }
        }
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
