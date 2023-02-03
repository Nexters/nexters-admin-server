package nexters.admin.support.extensions

import java.security.MessageDigest

fun String.sha256Encrypt(): String = bytesToHex(SHA256.digest(this.toByteArray()))

private val SHA256: MessageDigest = MessageDigest.getInstance("SHA-256")

private fun bytesToHex(bytes: ByteArray): String =
        bytes.fold("") { previous, current -> previous + "%02x".format(current) }
