package xyz.re_talk.global.util

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.springframework.stereotype.Component

@Component
class SecurityUtils (
    @Value($$"${AES_KEY}") private val rawKey: String
) {
    private lateinit var keySpec: SecretKeySpec
    private val algorithm = "AES/CBC/PKCS5Padding"
    private val ivSize = 16

    @PostConstruct
    fun init() {
        val keyBytes = rawKey.toByteArray()
        if (keyBytes.size != 32) throw IllegalArgumentException("AES-KEY는 반드시 32바이트여야 합니다.")
        keySpec = SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(text: String): String {
        val cipher = Cipher.getInstance(algorithm)

        val iv = ByteArray(ivSize).apply { SecureRandom().nextBytes(this) }
        val ivSpec = IvParameterSpec(iv)

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encrypted = cipher.doFinal(text.toByteArray())

        val combined = ByteArray(ivSize + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, ivSize)
        System.arraycopy(encrypted, 0, combined, ivSize, encrypted.size)

        return Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(combinedBase64: String): String {
        val combined = Base64.getDecoder().decode(combinedBase64)

        val iv = ByteArray(ivSize)
        System.arraycopy(combined, 0, iv, 0, ivSize)
        val ivSpec = IvParameterSpec(iv)

        val encryptedSize = combined.size - ivSize
        val encrypted = ByteArray(encryptedSize)
        System.arraycopy(combined, ivSize, encrypted, 0, encryptedSize)

        val cipher = Cipher.getInstance(algorithm)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        return String(cipher.doFinal(encrypted))
    }

    fun generateFingerprint(roomId: String, time: String, sender: String, content: String): String {
        val raw = "$roomId|$time|$sender|$content"
        return MessageDigest.getInstance("SHA-256")
            .digest(raw.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}