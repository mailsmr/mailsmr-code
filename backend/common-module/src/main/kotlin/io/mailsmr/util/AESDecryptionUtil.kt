package io.mailsmr.util

import org.jasypt.util.text.AES256TextEncryptor

object AESDecryptionUtil {
    fun decryptPassword(password: String, decryptionPassword: String): String {
        val aes = AES256TextEncryptor()
        aes.setPassword(decryptionPassword)
        return aes.decrypt(password)
    }

    fun encryptPassword(password: String, encryptionPassword: String): String {
        val aes = AES256TextEncryptor()
        aes.setPassword(encryptionPassword)
        return aes.encrypt(password)
    }
}
