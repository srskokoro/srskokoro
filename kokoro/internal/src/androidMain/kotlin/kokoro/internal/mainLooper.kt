package kokoro.internal

import android.os.Looper

@JvmField val mainLooper: Looper = Looper.getMainLooper()

@JvmField val mainThread: Thread = mainLooper.thread
