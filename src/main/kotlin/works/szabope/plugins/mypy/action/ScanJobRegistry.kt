package works.szabope.plugins.mypy.action

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import org.jetbrains.annotations.TestOnly

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

    @TestOnly
    fun reset() {
        job = null
    }

    companion object {
        val INSTANCE: ScanJobRegistry by lazy { ScanJobRegistry() }
    }
}
