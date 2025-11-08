package works.szabope.plugins.mypy.action

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin

class ScanJobRegistry {
    private var job: Job? = null

    fun set(job: Job) {
        if (!isAvailable()) {
            throw IllegalStateException("Current job has not been completed!")
        }
        this.job = job
    }

    fun isAvailable() = job?.isCompleted ?: true

    fun isActive() = job?.isActive ?: false

    suspend fun cancel() {
        job?.cancelAndJoin()
    }

    companion object {
        val INSTANCE: ScanJobRegistry by lazy { ScanJobRegistry() }
    }
}