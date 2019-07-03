package com.example.usbprintertest

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Created by mukun on 2018/3/29.
 */

object MD5Util {

    /***
     * MD5加码 生成32位md5码
     */
    fun string2MD5(inStr: String): String {
        var md5: MessageDigest? = null
        try {
            md5 = MessageDigest.getInstance("MD5")
        } catch (e: Exception) {
            println(e.toString())
            e.printStackTrace()
            return ""
        }

        val charArray = inStr.toCharArray()
        val byteArray = ByteArray(charArray.size)

        for (i in charArray.indices)
            byteArray[i] = charArray[i].toByte()
        val md5Bytes = md5!!.digest(byteArray)
        val hexValue = StringBuffer()
        for (i in md5Bytes.indices) {
            val `val` = md5Bytes[i].toInt() and 0xff
            if (`val` < 16)
                hexValue.append("0")
            hexValue.append(Integer.toHexString(`val`))
        }
        return hexValue.toString()
    }

    /***
     * MD5加码 生成16位md5码
     */
    fun string2MD5_16(str: String): String {
        var messageDigest: MessageDigest? = null

        try {
            messageDigest = MessageDigest.getInstance("MD5")

            messageDigest!!.reset()

            messageDigest.update(str.toByteArray(charset("UTF-8")))
        } catch (e: NoSuchAlgorithmException) {
            println("NoSuchAlgorithmException caught!")
            System.exit(-1)
            e.printStackTrace()
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        val byteArray = messageDigest!!.digest()

        val md5StrBuff = StringBuffer()

        for (i in byteArray.indices) {
            if (Integer.toHexString(0xFF and byteArray[i].toInt()).length == 1)
                md5StrBuff.append("0").append(Integer.toHexString(0xFF and byteArray[i].toInt()))
            else
                md5StrBuff.append(Integer.toHexString(0xFF and byteArray[i].toInt()))
        }
        //16位加密，从第9位到25位
        return md5StrBuff.substring(8, 24).toString().toUpperCase()
    }
}
