package util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.native.concurrent.ThreadLocal

class Debouncer(private val delayMs: Long = 100) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var job: Job? = null

    fun execute(task: () -> Unit) {
        job?.cancel()
        job = scope.launch {
            debugLog { "Scheduling debounced task on $coroutineContext" }
            delay(delayMs)
            debugLog { "Executing debounced task on $coroutineContext" }
            task()
        }
    }
}
