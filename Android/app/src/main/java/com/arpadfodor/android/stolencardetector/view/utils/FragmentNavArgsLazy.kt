package com.arpadfodor.android.stolencardetector.view.utils

import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.navigation.NavArgs
import androidx.navigation.NavArgsLazy

@MainThread
inline fun <reified Args : NavArgs> Fragment.navArgs() = NavArgsLazy(Args::class) {
    arguments ?: throw IllegalStateException("Fragment $this has null arguments")
}