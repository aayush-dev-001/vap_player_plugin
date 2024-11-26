package com.flutter.animation.vap.vap_player_plugin

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.tencent.qgame.animplayer.AnimView
import com.tencent.qgame.animplayer.AnimConfig
import com.tencent.qgame.animplayer.inter.IAnimListener
import com.tencent.qgame.animplayer.util.ScaleType
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.BinaryMessenger
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File

class VapViewFactory(
    private val messenger: BinaryMessenger,
    private val onViewCreated: (Int, VapView) -> Unit
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        return VapView(context, viewId, args as? Map<String?, Any?>, messenger).also { view ->
            onViewCreated(viewId, view)
        }
    }
}

class VapView(
    private val context: Context,
    private val id: Int,
    private val creationParams: Map<String?, Any?>?,
    messenger: BinaryMessenger
) : PlatformView, IAnimListener {
    private val TAG = "VapView"
    private val animView: AnimView = AnimView(context)
    private val container: FrameLayout = FrameLayout(context)
    private var currentFile: File? = null
    private val methodChannel: MethodChannel
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isPlaying = false
    private var isDestroyed = false
    private var scaleType = ScaleType.CENTER_CROP
    private var loopCount = 1

    init {
        methodChannel = MethodChannel(messenger, "vap_player_plugin_$id")
        container.addView(animView)
        animView.setScaleType(ScaleType.CENTER_CROP)
        animView.setAnimListener(this)

        creationParams?.let { params ->
            params["loop"]?.let { count ->
                loopCount = count as Int
            }
            params["videoPath"]?.let { path ->
                mainHandler.post {
                    playVideo(path as String)
                }
            }
        }
    }

    fun playVideo(path: String) {
        if (isDestroyed) return

        try {
            Log.d(TAG, "Playing video: $path")
            val file = File(path)
            if (file.exists()) {
                stopCurrentPlayback()
                currentFile = file
                isPlaying = true

                mainHandler.postDelayed({
                    if (!isDestroyed && isPlaying) {
                        animView.supportMask(true, true)
                        animView.setLoop(loopCount)
                        animView.startPlay(file)
                    }
                }, 100) // Small delay to ensure clean start
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing video", e)
            sendError("Error playing video: ${e.message}")
        }
    }

    private fun stopCurrentPlayback() {
        try {
            isPlaying = false
            animView.stopPlay()
            currentFile = null
            mainHandler.removeCallbacksAndMessages(null)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping video", e)
        }
    }

    private fun sendError(message: String) {
        if (!isDestroyed) {
            mainHandler.post {
                methodChannel.invokeMethod(
                    "onVideoError", mapOf(
                        "type" to -1,
                        "message" to message
                    )
                )
            }
        }
    }

    override fun onVideoConfigReady(config: AnimConfig): Boolean {
        Log.d(TAG, "Video config ready")
        return !isDestroyed
    }

    override fun onVideoStart() {
        Log.d(TAG, "Video started")
        if (!isDestroyed) {
            mainHandler.post {
                methodChannel.invokeMethod("onVideoStart", null)
            }
        }
    }

    override fun onVideoRender(frameIndex: Int, config: AnimConfig?) {
        // Frame rendering callback
    }

    override fun onVideoComplete() {
        Log.d(TAG, "Video completed")
        if (!isDestroyed) {
            mainHandler.post {
                methodChannel.invokeMethod("onVideoComplete", null)
            }
        }
    }

    override fun onVideoDestroy() {
        Log.d(TAG, "Video destroyed")
        if (!isDestroyed) {
            mainHandler.post {
                methodChannel.invokeMethod("onVideoDestroy", null)
            }
        }
    }

    override fun onFailed(errorType: Int, errorMsg: String?) {
        if (isDestroyed) return

        Log.e(TAG, "Video failed: type=$errorType, msg=$errorMsg")
        mainHandler.post {
            methodChannel.invokeMethod(
                "onVideoError", mapOf(
                    "type" to errorType,
                    "message" to (errorMsg ?: "Unknown error")
                )
            )
        }
    }

    override fun getView(): View = container

    override fun dispose() {
        try {
            isDestroyed = true
            stopCurrentPlayback()
        } catch (e: Exception) {
            Log.e(TAG, "Error disposing view", e)
        }
    }
}