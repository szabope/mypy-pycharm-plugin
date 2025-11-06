package works.szabope.plugins.mypy.action

import kotlinx.coroutines.Job

class ScanJobRegistry {
    private var job: Job? = null

    fun set(job: Job) {
        if (isActive()) {
            throw IllegalStateException("Job is still active")
        }
        this.job = job
    }

    fun isActive() = job?.isActive ?: false

    fun cancel() {
        job?.cancel()
        job = null
    }

    companion object {
        val INSTANCE: ScanJobRegistry by lazy { ScanJobRegistry() }
    }
}