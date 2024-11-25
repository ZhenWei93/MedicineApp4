package edu.fju.medicineapp.utility

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.JsonParseException
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptUtility
{
    enum class Type constructor(val key: String)
    {
        REQUEST("pitayapasswordin"),
        BIOMETRIC("pitayapasswordin"),
        WebNonHis("shinkongwuhosuss")
    }

    private const val CIPHER_ALGORITHM_CBC = "AES/CBC/PKCS5Padding"
    private const val initVector = "skhskhskhskhskhs"
    private val iv = IvParameterSpec(initVector.toByteArray(StandardCharsets.UTF_8))

    @JvmStatic
    fun encrypt(sSrc: String, type: Type): String
    {
        return encrypt(sSrc, type.key)
    }

    @JvmStatic
    fun decrypt(sSrc: String, type: Type): String
    {
        return decrypt(sSrc, type.key)
    }

    private fun encrypt(sSrc: String, sKey: String?): String
    {
        try
        {
            if (sKey == null)
                return ""

            val cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC)
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec(sKey), iv)
            val encrypted = cipher.doFinal(sSrc.toByteArray(StandardCharsets.UTF_8))
            return Base64.encodeToString(encrypted, Base64.DEFAULT)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            return ""
        }
    }

    private fun decrypt(sSrc: String, sKey: String?): String
    {
        try
        {
            if (sKey == null)
                return ""

            val skeySpec = getSecretKeySpec(sKey)
            val cipher = Cipher.getInstance(CIPHER_ALGORITHM_CBC)
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
            val encrypted1 = Base64.decode(sSrc, Base64.DEFAULT)
            val original = cipher.doFinal(encrypted1)
            return String(original, StandardCharsets.UTF_8)
        }
        catch (e: IllegalBlockSizeException)
        {
            return ""
        }
        catch (e: BadPaddingException)
        {
            return ""
        }
        catch (e: Exception)
        {
            return ""
        }
    }

    @JvmStatic
    fun <T> decryptClazz(info: String, clazz: Class<T>): T?
    {
        return try
        {
            val decryptString = decrypt(info, Type.REQUEST)
            Gson().fromJson(decryptString, clazz)
        }
        catch (e: JsonParseException)
        {
            null
        }
    }

    private fun getSecretKeySpec(sKey: String): SecretKeySpec
    {
        val raw = sKey.toByteArray(StandardCharsets.UTF_8)
        return SecretKeySpec(raw, "AES")
    }

    @JvmStatic
    fun encodeMd5(text: String): String
    {
        try
        {
            val instance: MessageDigest = MessageDigest.getInstance("MD5")
            val digest: ByteArray = instance.digest(text.toByteArray())
            var sb = StringBuffer()
            for (b in digest)
            {
                var i: Int = b.toInt() and 0xff
                var hexString = Integer.toHexString(i)
                if (hexString.length < 2)
                {
                    hexString = "0$hexString"
                }
                sb.append(hexString)
            }
            return sb.toString()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }

        return ""
    }
}