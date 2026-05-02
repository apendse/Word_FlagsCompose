package com.aap.worldflags.repo

fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}