package com.example.usbprintertest

import android.content.Context
import android.os.Environment
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList
import java.util.concurrent.Executors

object PrintHelper {
    private val TAG = "PrintHelper"
    private var mPrintDataList = mutableListOf<PrintData>()
    private val mImageConfig = mutableListOf<PrintConfig>()

    private val EXECUTOR = Executors.newSingleThreadExecutor()

    init {

    }


    fun getPrintConfig(context: Context, url: String) {
        getLocalPrintConfig(context)
//        getNetPrintConfig(context, url)
    }

    private fun getNetPrintConfig(context: Context, url: String) {
        Fuel.get(url).responseString { _, _, result ->
            when (result) {
                is Result.Failure -> {
                    val ex = result.error
                    Log.d(TAG, result.error.message)
                    ToastUtil.show("网络故障，请联系管理员，error：004")
                }

                is Result.Success -> {
                    try {
                        val json1 = JSONObject(result.get())
                        val json = json1.getString("data")
                        mPrintDataList.clear()
                        mPrintDataList = Gson().fromJson(
                            json, object : TypeToken<ArrayList<PrintData>>() {}.type
                        )

                        mImageConfig.clear()
                        mPrintDataList.map {
                            Log.d(TAG, it.text)
                            if (it.type == 2) {
                                if (it.config != null) {
                                    mImageConfig.add(it.config)
                                }
                            }
                            Log.d(TAG, it.config.toString())
                        }

                        Log.d(TAG, "有 ${mImageConfig.size} 个图片")

                        //todo 开启线程池执行下载图片
                        mImageConfig.mapIndexed { index, printConfig ->
                            downloadFile(context, printConfig.imageUrl, index)
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun getLocalPrintConfig(context: Context) {
        EXECUTOR.execute {
            val assetManager = context.assets
            val inputStream = assetManager.open("print_config.json")
            val bufferedInputStream = BufferedInputStream(inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()
            try {
                var len = -1
                val buffer = ByteArray(1024)
                while (bufferedInputStream.read(buffer).also { len = it } != -1) {
                    byteArrayOutputStream.write(buffer, 0, len)
                }
                val dataArray = byteArrayOutputStream.toByteArray()
                val result = String(dataArray)
                try {
                    val json1 = JSONObject(result)
                    val json = json1.getString("data")
                    mPrintDataList.clear()
                    mPrintDataList = Gson().fromJson(
                        json, object : TypeToken<ArrayList<PrintData>>() {}.type
                    )

                    mImageConfig.clear()
                    mPrintDataList.map {
                        Log.d(TAG, it.text)
                        if (it.type == 2) {
                            if (it.config != null) {
                                mImageConfig.add(it.config)
                            }
                        }
                        Log.d(TAG, it.config.toString())
                    }

                    Log.d(TAG, "有 ${mImageConfig.size} 个图片")

                    //todo 开启线程池执行下载图片
                    mImageConfig.mapIndexed { index, printConfig ->
                        downloadFile(context, printConfig.imageUrl, index)
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                bufferedInputStream.close()
                byteArrayOutputStream.close()
            }
        }

    }

    private fun downloadFile(context: Context, url: String, index: Int) {
        Log.d(TAG, "URL = $url")
        EXECUTOR.execute {
            val fileName = MD5Util.string2MD5(url)
            val dir = Environment.getExternalStorageDirectory().absolutePath + File.separator + context.packageName
            val path = dir + File.separator + fileName

            val dirFile = File(dir)
            if (!dirFile.exists()) {
                dirFile.mkdirs()
            }

            val file = File(path)
            if (file.exists()) {
                file.delete()
                file.createNewFile()
            }

            Log.d(TAG, "path = $path")
            try {
                Fuel.get(url).response { request, response, result ->

                    val (data, error) = result

                    if (data != null) {
                        val fos = FileOutputStream(path)
                        fos.write(data)
                        fos.close()
                    }
                    when (result) {
                        is Result.Failure -> {

                        }
                        is Result.Success -> {
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }


            if (index == mImageConfig.size - 1) {
                Log.d(TAG, "所有任务完成")

                if (!PrintManager.canPrint(context)) {
                    return@execute
                }

                //todo 遍历数据打印
                mPrintDataList.map {
                    val config = it.config
                    when(it.type) {
                        0 -> {
                            //文本
                            /***

                            content: String,
                            align: Int = PRINT_LEFT,
                            blob: Int = 0,
                            italic: Int = 0,
                            underLine: Int = 0,
                            scale: Int = 0,
                            leftMargin: Int = 0,
                            rightMargin: Int = 0,
                            lineHeight: Int = 40,
                            textMargin: Int = 0
                            )
                             */
                            if (config == null) {
                                PrintManager.appendString(it.text)
                            } else {
                                PrintManager.appendString(it.text,
                                    align = config.align,
                                    blob = config.blob,
                                    italic = config.italic,
                                    underLine = config.underLine,
                                    scale = config.textScale,
                                    leftMargin = config.leftMargin,
                                    rightMargin = config.rightMargin,
                                    lineHeight = config.lineSpace,
                                    textMargin = config.textSpace)
                            }
                        }
                        1 -> {
                            //二维码
                            PrintManager.appendQRCode(it.text, config?.qrLeftMargin?:0, config?.qrScale?:8)
                        }
                        2 -> {
                            //图片
                            val fileName = MD5Util.string2MD5(config?.imageUrl?:"")
                            val path = path
                            PrintManager.appendImage(context, path)
                        }
                        else -> {

                        }
                    }
                }

                PrintManager.print()
            }
        }
    }
}