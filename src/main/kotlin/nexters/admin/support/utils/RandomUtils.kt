package nexters.admin.support.utils

import kotlin.random.Random

val chars : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun randomStringLengthOf(length: Int) = (1..length)
        .map { Random.nextInt(0, chars.size).let { chars[it] } }
        .joinToString("")
