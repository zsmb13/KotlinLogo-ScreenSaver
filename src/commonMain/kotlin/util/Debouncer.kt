package util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

class Debouncer(private val delayMs: Long) {
    private val scope = CoroutineScope(EmptyCoroutineContext)
    private var job: Job? = null

    fun execute(task: () -> Unit) {
        job?.cancel()
        job = scope.launch {
            delay(delayMs)
            task()
        }
    }
}
