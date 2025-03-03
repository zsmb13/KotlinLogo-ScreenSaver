package util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Debouncer(private val delayMs: Long = 100) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var job: Job? = null

    fun execute(task: () -> Unit) {
        job?.cancel()
        job = scope.launch {
            delay(delayMs)
            task()
        }
    }
}
