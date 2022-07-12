package com.example.locationtestapp.util

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException


suspend fun <T> Task<T>.suspend() = suspendCancellableCoroutine<T?> { cont ->
    addOnCanceledListener {
        cont.cancel()
    }.addOnSuccessListener { value: T? ->
        cont.resumeWith(Result.success(value))
    }.addOnFailureListener {
        cont.resumeWithException(it)
    }
}