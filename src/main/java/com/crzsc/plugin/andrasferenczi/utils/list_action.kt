package com.crzsc.plugin.andrasferenczi.utils

fun List<() -> Unit>.mergeCalls(): (() -> Unit)? =
    if (isEmpty())
        null
    else {
        { this.forEach { it() } }
    }
