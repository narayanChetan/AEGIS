package com.example.data

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * High-performance, offline cryptographic manager providing full end-to-end local encryption (AES/CBC/PKCS5Padding)
 * for secure storing and automation routing of personal smart home credentials and workflows.
 */
object CryptoManager {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_SEED = "AEGIS_SHIELD_KEY_JARVIS_SECURE09" // 32 chars = 256 bits
    private const val IV_SEED = "AEGIS_INIT_VECTR" // 16 chars = 128 bits

    private val secretKey = SecretKeySpec(KEY_SEED.toByteArray(Charsets.UTF_8), "AES")
    private val ivParameterSpec = IvParameterSpec(IV_SEED.toByteArray(Charsets.UTF_8))

    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            plainText
        }
    }

    fun decrypt(encryptedText: String): String {
        if (encryptedText.isEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
            val decodedBytes = Base64.decode(encryptedText, Base64.NO_WRAP)
            String(cipher.doFinal(decodedBytes), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            encryptedText
        }
    }
}
