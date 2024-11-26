package com.flutter.animation.vap.vap_player_plugin

import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.BinaryMessenger
import android.content.Context
import android.util.Log
import java.io.File

class VapPlayerPlugin : FlutterPlugin, MethodCallHandler {
    private val TAG = "VapPlayerPlugin"
    private lateinit var channel: MethodChannel
    private var applicationContext: Context? = null
    private lateinit var messenger: BinaryMessenger
    private val views = mutableMapOf<Int, VapView>()

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        messenger = flutterPluginBinding.binaryMessenger
        channel = MethodChannel(messenger, "vap_player_plugin")
        channel.setMethodCallHandler(this)
        applicationContext = flutterPluginBinding.applicationContext

        // Register view factory
        flutterPluginBinding
            .platformViewRegistry
            .registerViewFactory(
                "vap_player_view",
                VapViewFactory(messenger) { id, view ->
                    views[id] = view
                }
            )
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        try {
            when (call.method) {
                "getCacheDir" -> {
                    val cacheDir = applicationContext?.cacheDir?.absolutePath
                    if (cacheDir != null) {
                        val dir = File(cacheDir)
                        if (!dir.exists()) {
                            dir.mkdirs()
                        }
                        result.success(cacheDir)
                    } else {
                        result.error("NO_CACHE_DIR", "Could not get cache directory", null)
                    }
                }

                else -> {
                    result.notImplemented()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in method call: ${call.method}", e)
            result.error("OPERATION_ERROR", e.message, null)
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        applicationContext = null
        views.clear()
    }
}
