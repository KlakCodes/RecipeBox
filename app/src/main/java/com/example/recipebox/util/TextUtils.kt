package com.example.recipebox.util

/** "1 step", "4 steps". */
fun plural(count: Int, noun: String): String = if (count == 1) "1 $noun" else "$count ${noun}s"
