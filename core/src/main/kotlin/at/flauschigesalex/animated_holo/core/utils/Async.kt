package at.flauschigesalex.animated_holo.core.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private val context = SupervisorJob() + Dispatchers.IO
private val scope = CoroutineScope(context)

fun scheduleAsync(block: suspend () -> Unit) = runCatching { 
    scope.launch { block() }
}