package com.example.usbprintertest

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Parcelable
import android.util.Log
import com.printsdk.cmd.PrintCmd
import com.printsdk.usbsdk.UsbDriver
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object PrintManager {

    //默认为:1, 0 靠左、1  居中、2:靠右
    const val PRINT_LEFT = 0
    const val PRINT_MIDDLE = 1
    const val PRINT_RIGHT = 2

    private val TAG = PrintManager::class.java.simpleName

    private const val PID11 = 8211
    private const val PID13 = 8213
    private const val PID15 = 8215
    private const val VENDORID = 1305

    private const val ACTION_USB_PERMISSION = "com.usb.sample.USB_PERMISSION"

    private var mUsbDriver: UsbDriver? = null
    private var mUsbDev1: UsbDevice? = null        //打印机1
    private var mUsbDev2: UsbDevice? = null        //打印机2
    private var mUsbDev: UsbDevice? = null

    private var mUsbManager: UsbManager? = null
    private var mUsbReceiver: UsbReceiver? = null

    private var connectedStatus = false

    fun initPrinter(context: Context) {
        registUSBReceiver(context)
        connectPrinter(context)
        setDefaultPrinterParameters()
    }

    private fun connectPrinter(context: Context) {
        try {
            if (mUsbDriver?.isConnected == false) {
                // USB线未连接
                mUsbManager?.deviceList?.map {
                    val device = it.value
                    if (device.productId == PID11 && device.vendorId == VENDORID
                        || device.productId == PID13 && device.vendorId == VENDORID
                        || device.productId == PID15 && device.vendorId == VENDORID
                    ) {
                        connectedStatus = mUsbDriver?.usbAttached(device) == true
                        if (!connectedStatus) {
                            return@map
                        }
                        connectedStatus = mUsbDriver?.openUsbDevice(device) == true

                        // 打开设备
                        if (connectedStatus) {
                            if (device.productId == PID11) {
                                mUsbDev1 = device
                                mUsbDev = mUsbDev1
                                Log.d(TAG, "打印机1")
                            } else {
                                mUsbDev2 = device
                                mUsbDev = mUsbDev2
                                Log.d(TAG, "打印机2")
                            }
//                            Log.d(TAG, context.getString(R.string.usb_driver_success))
                            ToastUtil.show(context.getString(R.string.USB_Driver_Success))
                            return@map
                        } else {
//                            Log.d(TAG, context.getString(R.string.usb_driver_success))
                            ToastUtil.show(context.getString(R.string.USB_Driver_Failed))
                            return@map
                        }
                    }
                }
            } else {
                connectedStatus = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ToastUtil.show(e.message.toString())
        }
    }

    fun destroy(context: Context) {
        unRegistUSBReciever(context)
        mUsbDriver = null
        mUsbDev = null
        mUsbDev1 = null
        mUsbDev2 = null
    }

    fun appendString(
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
    ): PrintManager {
        setDefaultPrinterParameters()
        //对齐
        mUsbDriver?.write(PrintCmd.SetAlignment(align), mUsbDev)
        //加粗
        mUsbDriver?.write(PrintCmd.SetBold(blob), mUsbDev)
        //斜体
        mUsbDriver?.write(PrintCmd.SetItalic(italic), mUsbDev)
        //下划线0,1,2
        mUsbDriver?.write(PrintCmd.SetUnderline(underLine), mUsbDev)
        //左边距
        mUsbDriver?.write(PrintCmd.SetLeftmargin(leftMargin), mUsbDev)
        //右边距
        mUsbDriver?.write(PrintCmd.SetRightmargin(rightMargin), mUsbDev)
        //放大
        mUsbDriver?.write(PrintCmd.SetSizetext(scale, scale), mUsbDev)
        //行间距(行高)
        mUsbDriver?.write(PrintCmd.SetLinespace(lineHeight), mUsbDev)
        //字符间距abc
        mUsbDriver?.write(PrintCmd.SetSpacechar(textMargin), mUsbDev)
        //汉字间距
        mUsbDriver?.write(PrintCmd.SetSpacechinese(textMargin, textMargin), mUsbDev)


        val data = PrintCmd.PrintString(content, 0)
        mUsbDriver?.write(data, data.size, mUsbDev)
        return this
    }

    fun appendTable(indexList: MutableList<Int>, dataList: MutableList<MutableList<String>>): PrintManager {
        //第一部分
        val dataPartOneBuilder = StringBuilder()
        indexList.mapIndexed { index, it ->
            // 转换第1列
            dataPartOneBuilder.append(ImageUtils.intToHexString(Integer.valueOf(it), 1) + " ")
            if (index == indexList.size - 1) {
                dataPartOneBuilder.append("00 ")
            }
        }

        val partOneData = dataPartOneBuilder.toString()
        //列数
        val columnCount = dataList.size
        //行数
        val rowCount = dataList[0].size

        for (row in 0 until rowCount) {
            val dataPartTwoBuilder = StringBuilder()
            for (column in 0 until columnCount) {
                val data = ImageUtils.stringTo16Hex(dataList[column][row])
                if (column != columnCount - 1) {
                    dataPartTwoBuilder.append(data + "09 ")
                } else {
                    dataPartTwoBuilder.append(data + "0A 0A")
                }
            }
            val finalData = partOneData + dataPartTwoBuilder.toString()
            val seat = ImageUtils.hexStr2Bytesnoenter(finalData)

            if ("" != finalData) {
                mUsbDriver?.write(PrintCmd.SetAlignment(0))
                mUsbDriver?.write(PrintCmd.SetLinespace(40))
                mUsbDriver?.write(
                    PrintCmd.SetHTseat(seat, seat.size),
                    seat.size, mUsbDev
                )
                mUsbDriver?.write(PrintCmd.PrintFeedline(0), mUsbDev)      // 走纸换行
            }
        }

//        for (column in 0 until columnCount) {
//            val dataPartTwoBuilder = StringBuilder()
//            for (row in 0 until rowCount) {
//                val data = ImageUtils.stringTo16Hex(dataList[column][row])
//                if (row != rowCount - 1) {
//                    dataPartTwoBuilder.append(data + "09 ")
//                } else {
//                    dataPartTwoBuilder.append(data + "0A 0A")
//                }
//            }
//            val finalData = partOneData + dataPartTwoBuilder.toString()
//            val seat = ImageUtils.hexStr2Bytesnoenter(finalData)
//
//            if ("" != finalData) {
//                mUsbDriver?.write(PrintCmd.SetAlignment(0))
//                mUsbDriver?.write(PrintCmd.SetLinespace(40))
//                mUsbDriver?.write(
//                    PrintCmd.SetHTseat(seat, seat.size),
//                    seat.size, mUsbDev
//                )
//                mUsbDriver?.write(PrintCmd.PrintFeedline(0), mUsbDev)      // 走纸换行
//            }
//        }


        dataList.mapIndexed { index, mutableList ->

        }
        return this
    }

    /***
     * @param marginLeft 0 - 27
     * @param scale 放大 1 - 8
     */
    fun appendQRCode(content: String, marginLeft: Int, scale: Int): PrintManager {
        setDefaultPrinterParameters()
        mUsbDriver?.write(PrintCmd.PrintQrcode(content, marginLeft, scale, 0))
        return this
    }

    fun appendImage(context: Context, filePath: String, leftMargin: Int = 0): PrintManager {
        setDefaultPrinterParameters()
        if (!File(filePath).exists()) {
            return this
        }
        val inputBmp = ImageUtils.getBitmapData(filePath) ?: return this
        val data = ImageUtils.getPixelsByBitmap(inputBmp)
        //左边距
        val leftMargin = calcImageLeftMargin(inputBmp.width)
        mUsbDriver?.write(PrintCmd.SetLeftmargin(leftMargin), mUsbDev)
        mUsbDriver?.write(PrintCmd.PrintDiskImagefile(data, inputBmp.width, inputBmp.height))
        return this
    }

    fun canPrint(context: Context): Boolean {
        val printEndStatus = PrintCmd.getPrintEndStatus(mUsbDriver)
        val printerStatus = getPrinterStatus(mUsbDev)
        val pst = PrintCmd.CheckStatus(PrintCmd.GetStatus())
        Log.d(TAG, "printerStatus = $printerStatus")
        return if (printEndStatus != -1) {
            val checkStatus = checkStatus(context, printerStatus)
            Log.d(TAG, "checkStatus $checkStatus")
            checkStatus == 0
        } else {
            false
        }
    }

    fun print(): Boolean {
        try {
            // 走纸换行
            mUsbDriver?.write(PrintCmd.PrintFeedline(4))
            // 切纸类型0: 全切
            mUsbDriver?.write(PrintCmd.PrintCutpaper(0))
            // 清除缓存,初始化
            mUsbDriver?.write(PrintCmd.SetClean())
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    private fun calcImageLeftMargin(width: Int): Int {
        val defaultPageWidth = 576
        if (width >= defaultPageWidth) return 0
        return (defaultPageWidth - width) / 2
    }

    private fun registUSBReceiver(context: Context) {
        mUsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        mUsbDriver = UsbDriver(mUsbManager, context)
        val permissionIntent1 = PendingIntent.getBroadcast(
            context, 0,
            Intent(ACTION_USB_PERMISSION), 0
        )
        mUsbDriver?.setPermissionIntent(permissionIntent1)
        // Broadcast listen for new devices

        mUsbReceiver = UsbReceiver()
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        context.registerReceiver(mUsbReceiver, filter)
    }

    fun unRegistUSBReciever(context: Context) {
        context.unregisterReceiver(mUsbReceiver)
    }

    private fun checkStatus(context: Context, status: Int): Int {
        var iRet = -1

        val sMsg = StringBuilder()


        //0 打印机正常 、1 打印机未连接或未上电、2 打印机和调用库不匹配
        //3 打印头打开 、4 切刀未复位 、5 打印头过热 、6 黑标错误 、7 纸尽 、8 纸将尽
        when (status) {
            0 -> {
                sMsg.append("正常")
                iRet = 0
            }
            8 -> {
                sMsg.append("纸将尽")
                iRet = 0
            }
            3 -> {
                sMsg.append("打印头打开")
                ToastUtil.show("打印头打开")
            }
            4 -> {
                sMsg.append("切刀未复位")
                ToastUtil.show("切刀未复位")
            }
            5 -> {
                sMsg.append("打印头过热")
                ToastUtil.show("打印头过热")
            }
            6 -> {
                sMsg.append("黑标错误")
                ToastUtil.show("黑标错误")
            }
            7 -> {
                sMsg.append("纸尽==缺纸")
                ToastUtil.show("纸尽==缺纸")
            }
            1 -> {
                sMsg.append("打印机未连接或未上电")
                ToastUtil.show("打印机未连接或未上电")
            }
            else -> {
                sMsg.append("异常")
                ToastUtil.show("异常")
            }
        }
        val message = sMsg.toString()
        Log.d(TAG, message)
        return iRet
    }

    // 检测打印机状态
    private fun getPrinterStatus(usbDev: UsbDevice?): Int {
        var status = -1

        val bRead1 = ByteArray(1)
        val bWrite1 = PrintCmd.GetStatus1()
        if (mUsbDriver?.read(bRead1, bWrite1, usbDev)!! > 0) {
            status = PrintCmd.CheckStatus1(bRead1[0])
        }

        if (status != 0)
            return status

        val bRead2 = ByteArray(1)
        val bWrite2 = PrintCmd.GetStatus2()
        if (mUsbDriver?.read(bRead2, bWrite2, usbDev)!! > 0) {
            status = PrintCmd.CheckStatus2(bRead2[0])
        }

        if (status != 0)
            return status

        val bRead3 = ByteArray(1)
        val bWrite3 = PrintCmd.GetStatus3()
        if (mUsbDriver?.read(bRead3, bWrite3, usbDev)!! > 0) {
            status = PrintCmd.CheckStatus3(bRead3[0])
        }

        if (status != 0)
            return status

        val bRead4 = ByteArray(1)
        val bWrite4 = PrintCmd.GetStatus4()
        if (mUsbDriver?.read(bRead4, bWrite4, usbDev)!! > 0) {
            status = PrintCmd.CheckStatus4(bRead4[0])
        }
        return status
    }

    private fun createFile(context: Context, filePath: String): Boolean {
        return File(filePath).exists()
//        val file = File(filePath)
//        if (file.exists()) {
////            file.delete()
////            file.createNewFile()
//            return true
//        }
//
//        val parent = file.parentFile
//        if (!parent.exists()) {
//            parent.mkdirs()
//        }
//
//        return saveBitmapFile(context, filePath).length() != 0L

    }

    private fun saveBitmapFile(context: Context, filepath: String): File {
        val assetManager = context.assets
        val inputStream = assetManager.open("logo1.png")
        val bufferedInputStream = BufferedInputStream(inputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()
        val fileOutputStream = FileOutputStream(File(filepath))
        try {
            var len = -1
            var buffer = ByteArray(1024)
            while (bufferedInputStream.read(buffer).also { len = it } != -1) {
                byteArrayOutputStream.write(buffer, 0, len)
            }
            fileOutputStream.write(byteArrayOutputStream.toByteArray())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            bufferedInputStream.close()
            byteArrayOutputStream.close()
            fileOutputStream.close()
        }


        return File(filepath)
    }

    private fun setDefaultPrinterParameters() {
//        private var rotate = 0       // 默认为:0, 0 正常、1 90度旋转
//        private var align = 0        // 默认为:1, 0 靠左、1  居中、2:靠右
//        private var underLine = 0    // 默认为:0, 0 取消、   1 下划1、 2 下划2
//        private var linespace = 40   // 默认40, 常用：30 40 50 60 行间距
//        private var cutter = 0       // 默认0，  0 全切、1 半切
        // 对齐方式
        mUsbDriver?.write(PrintCmd.SetAlignment(0), mUsbDev)
        // 字体旋转
        mUsbDriver?.write(PrintCmd.SetRotate(0), mUsbDev)
        // 下划线
        mUsbDriver?.write(PrintCmd.SetUnderline(0), mUsbDev)
        //行大小
        mUsbDriver?.write(PrintCmd.SetLinespace(40), mUsbDev)
    }

    /*
     *  BroadcastReceiver when insert/remove the device USB plug into/from a USB port
     *  创建一个广播接收器接收USB插拔信息：当插入USB插头插到一个USB端口，或从一个USB端口，移除装置的USB插头
     */
    class UsbReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive action = " + intent.action)
            val action = intent.action
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
                if (mUsbDriver?.usbAttached(intent) == true) {
                    Log.d(TAG, "onReceive usbAttached = true")
                    val device = intent
                        .getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice
                    Log.d(TAG, "onReceive device.productId = " + device.productId)
                    Log.d(TAG, "onReceive device.vendorId = " + device.vendorId)
                    if (device.productId == PID11 && device.vendorId == VENDORID
                        || device.productId == PID13 && device.vendorId == VENDORID
                        || device.productId == PID15 && device.vendorId == VENDORID
                    ) {
                        if (mUsbDriver?.openUsbDevice(device) == true) {
                            Log.d(TAG, "onReceive openUsbDevice")
                            if (device.productId == PID11) {
                                mUsbDev1 = device
                                mUsbDev = mUsbDev1
                            } else {
                                mUsbDev2 = device
                                mUsbDev = mUsbDev2
                            }
                        } else {
                            Log.d(TAG, "onReceive not openUsbDevice")
                        }
                    }
                } else {
                    Log.d(TAG, "onReceive usbAttached = false")
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
                val device = intent
                    .getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice
                if (device.productId == PID11 && device.vendorId == VENDORID
                    || device.productId == PID13 && device.vendorId == VENDORID
                    || device.productId == PID15 && device.vendorId == VENDORID
                ) {
                    mUsbDriver?.closeUsbDevice(device)
                    if (device.productId == PID11)
                        mUsbDev1 = null
                    else
                        mUsbDev2 = null
                }
            } else if (ACTION_USB_PERMISSION == action) {
                synchronized(this) {
                    val device = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device.productId == PID11 && device.vendorId == VENDORID
                            || device.productId == PID13 && device.vendorId == VENDORID
                            || device.productId == PID15 && device.vendorId == VENDORID
                        ) {
                            if (mUsbDriver?.openUsbDevice(device) == true) {
                                if (device.productId == PID11) {
                                    mUsbDev1 = device
                                    mUsbDev = mUsbDev1
                                } else {
                                    mUsbDev2 = device
                                    mUsbDev = mUsbDev2
                                }
                            }
                        }
                    } else {
                        ToastUtil.show("permission denied for device")
                    }
                }
            }
        }
    }
}