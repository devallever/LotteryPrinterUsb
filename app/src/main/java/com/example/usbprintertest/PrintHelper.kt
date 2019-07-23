package com.example.usbprintertest

import android.content.Context
import android.os.Environment
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
                    Log.d(TAG, result.error.message)
                    ToastUtil.show("网络故障，请联系管理员，error：004")
                }

                is Result.Success -> {
                    handlePrintData(context, result.get())
                }
            }
        }
    }

    private fun handlePrintData(context: Context, data: String) {
        EXECUTOR.execute {
            try {
                //local
//                val json1 = JSONObject(data)
//                val json = json1.getString("data")
                //server
                val responseJson = JSONObject(data)
                val jsonDataObj = responseJson.getJSONObject("data")
                val json = jsonDataObj.getString("data")
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

                //下载图片
                mImageConfig.map {
                    downloadFile(context, it.imageUrl)
                }

                printData(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private fun getLocalPrintConfig(context: Context) {
        EXECUTOR.execute {
            val result = readTextAssetsFile(context, "print_config.json")
            handlePrintData(context, result)
        }
    }

    private fun readTextAssetsFile(context: Context, path: String): String {
        var result = ""
        val assetManager = context.assets
        val inputStream = assetManager.open(path)
        val bufferedInputStream = BufferedInputStream(inputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()
        try {
            var len = -1
            val buffer = ByteArray(1024)
            while (bufferedInputStream.read(buffer).also { len = it } != -1) {
                byteArrayOutputStream.write(buffer, 0, len)
            }
            val dataArray = byteArrayOutputStream.toByteArray()
            result = String(dataArray)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            bufferedInputStream.close()
            byteArrayOutputStream.close()
        }
        return result
    }

    private fun printData(context: Context) {
        if (!PrintManager.canPrint(context)) {
            ToastUtil.show("打印机异常，无法打印")
            return
        }

        for (it in mPrintDataList) {
            if (it.hidden == "1") {
                continue
            }
            val config = it.config
            when(it.type) {
                0 -> {
                    //文本
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
                    val dir = Environment.getExternalStorageDirectory().absolutePath + File.separator + context.packageName
                    val fileName = MD5Util.string2MD5(it.config?.imageUrl?:"")
                    val path = dir + File.separator + fileName
                    PrintManager.appendImage(path)

                }
                3 -> {
                    //表格
                    val indexList = it.config?.tableIndexList
                    val tableColumnList = it.config?.tableColumn
                    val tableList = mutableListOf<MutableList<String>>()
                    tableColumnList?.map {
                        val data = it.data
                        data?.let {
                            tableList.add(it)
                        }
                    }
                    indexList?.let {
                        PrintManager.appendTable(indexList, tableList)
                    }

                }
                else -> {

                }
            }
        }

//        //todo 遍历数据打印
//        mPrintDataList.map { it ->
//            val config = it.config
//            when(it.type) {
//                0 -> {
//                    //文本
//                    if (config == null) {
//                        PrintManager.appendString(it.text)
//                    } else {
//                        PrintManager.appendString(it.text,
//                            align = config.align,
//                            blob = config.blob,
//                            italic = config.italic,
//                            underLine = config.underLine,
//                            scale = config.textScale,
//                            leftMargin = config.leftMargin,
//                            rightMargin = config.rightMargin,
//                            lineHeight = config.lineSpace,
//                            textMargin = config.textSpace)
//                    }
//                }
//                1 -> {
//                    //二维码
//                    PrintManager.appendQRCode(it.text, config?.qrLeftMargin?:0, config?.qrScale?:8)
//                }
//                2 -> {
//                    //图片
//                    val dir = Environment.getExternalStorageDirectory().absolutePath + File.separator + context.packageName
//                    val fileName = MD5Util.string2MD5(it.config?.imageUrl?:"")
//                    val path = dir + File.separator + fileName
//                    PrintManager.appendImage(path)
//
//                }
//                3 -> {
//                    //表格
//                    val indexList = it.config?.tableIndexList
//                    val tableColumnList = it.config?.tableColumn
//                    val tableList = mutableListOf<MutableList<String>>()
//                    tableColumnList?.map {
//                        val data = it.data
//                        data?.let {
//                            tableList.add(it)
//                        }
//                    }
//                    indexList?.let {
//                        PrintManager.appendTable(indexList, tableList)
//                    }
//
//                }
//                else -> {
//
//                }
//            }
//        }

        PrintManager.print()
    }

    private fun downloadFile(context: Context, url: String) {
        Log.d(TAG, "URL = $url")
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
            //同步请求
            val (request, response, result) = url.httpGet().response()
            val (data, error) = result

            if (data != null) {
                val fos = FileOutputStream(path)
                fos.write(data)
                fos.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}