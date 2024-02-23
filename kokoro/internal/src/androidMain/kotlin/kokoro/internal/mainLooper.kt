package kokoro.internal

import android.os.Looper

val mainLooper: Looper = Looper.getMainLooper()

val mainThread: Thread = mainLooper.thread
