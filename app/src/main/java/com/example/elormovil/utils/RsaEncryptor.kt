package com.example.elormovil.utils

import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object RsaEncryptor {

    fun encrypt(text: String, publicKeyStr: String): String {
        val keyBytes = android.util.Base64.decode(publicKeyStr, android.util.Base64.DEFAULT)

        val keySpec = X509EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val publicKey = keyFactory.generatePublic(keySpec)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)

        val encrypted = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
        return android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP)
    }
}
