package com.example.usbprintertest

import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.media.Image
import android.os.Bundle
import android.os.Environment
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.example.usbprintertest.util.Constant
import com.example.usbprintertest.util.T
import com.example.usbprintertest.util.Utils
import com.printsdk.cmd.PrintCmd
import com.printsdk.usbsdk.UsbDriver
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class PrintTestActivity : Activity(), View.OnClickListener {

    private val TAG = "PrintTestActivity"

    private val PID11 = 8211
    private val PID13 = 8213
    private val PID15 = 8215
    private val VENDORID = 1305

    private val ACTION_USB_PERMISSION = "com.usb.sample.USB_PERMISSION"

    internal var mUsbDriver: UsbDriver? = null
    internal var mUsbDev1: UsbDevice? = null        //打印机1
    internal var mUsbDev2: UsbDevice? = null        //打印机2
    internal var mUsbDev: UsbDevice? = null

    private var mUsbManager: UsbManager? = null
    private var mUsbReceiver: UsbReceiver? = null

    private lateinit var mBtnPrint: Button
    private lateinit var mIvImage: ImageView


    private var connectedStatus = false

    private var receive = ""
    private var state = ""
    private var normal = ""
    private var notConnectedOrNotPopwer = ""
    private var notMatch = ""
    private var printerHeadOpen = ""
    private var cutterNotReset = ""
    private var printHeadOverheated = ""
    private var blackMarkError = ""
    private var paperExh = ""
    private var paperWillExh = ""
    private var abnormal = ""

    private var iline = "4"
    private var rotate = 0       // 默认为:0, 0 正常、1 90度旋转
    private var align = 0        // 默认为:1, 0 靠左、1  居中、2:靠右
    private var underLine = 0    // 默认为:0, 0 取消、   1 下划1、 2 下划2
    private var linespace = 40   // 默认40, 常用：30 40 50 60 行间距
    private var cutter = 0       // 默认0，  0 全切、1 半切

    private var mPrintContent = ""



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print_test)

        initData()
        initView()
        initBroadCast()
        initPrinter()

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mUsbReceiver)
        mUsbDriver = null
        mUsbDev = null
        mUsbDev1 = null
        mUsbDev2 = null
    }

    private fun initData() {
        receive = Constant.Receive_CN
        state = Constant.State_CN
        normal = Constant.Normal_CN
        notConnectedOrNotPopwer = Constant.NoConnectedOrNoOnPower_CN
        notMatch = Constant.PrinterAndLibraryNotMatch_CN
        printerHeadOpen = Constant.PrintHeadOpen_CN
        cutterNotReset = Constant.CutterNotReset_CN
        printHeadOverheated = Constant.PrintHeadOverheated_CN
        blackMarkError = Constant.BlackMarkError_CN
        paperExh = Constant.PaperExhausted_CN
        paperWillExh = Constant.PaperWillExhausted_CN
        abnormal = Constant.Abnormal_CN
        mPrintContent = ""
    }

    private fun initView() {
        mBtnPrint = findViewById(R.id.btn_print)
        mBtnPrint.setOnClickListener(this)
        mIvImage = findViewById(R.id.image)
    }

    private fun initBroadCast() {
        mUsbManager = getSystemService(Context.USB_SERVICE) as UsbManager
        mUsbDriver = UsbDriver(mUsbManager, this)
        val permissionIntent1 = PendingIntent.getBroadcast(
            this, 0,
            Intent(ACTION_USB_PERMISSION), 0
        )
        mUsbDriver?.setPermissionIntent(permissionIntent1)
        // Broadcast listen for new devices

        mUsbReceiver = UsbReceiver()
        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        this.registerReceiver(mUsbReceiver, filter)
    }

    private fun initPrinter() {
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
                            T.showShort(this, getString(R.string.USB_Driver_Success))
                            return@map
                        } else {
                            T.showShort(this, getString(R.string.USB_Driver_Failed))
                            return@map
                        }
                    }
                }
            } else {
                connectedStatus = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            T.showShort(this, e.message)
        }
    }


    override fun onClick(v: View?) {
        when (v) {
            mBtnPrint -> {
                T.showShort(this, "")
                print()
            }
        }
    }

    private fun print() {
        val printEndStatus = PrintCmd.getPrintEndStatus(mUsbDriver)
        val printerStatus = getPrinterStatus(mUsbDev)
        Log.d(TAG, "printerStatus = $printerStatus")
        if (printEndStatus != -1) {
            val checkStatus = checkStatus(printerStatus)
            Log.d(TAG, "checkStatus $checkStatus")
            if (checkStatus != 0) {
                return
            }

            // 对齐方式
            mUsbDriver?.write(PrintCmd.SetAlignment(align), mUsbDev)
            // 字体旋转
            mUsbDriver?.write(PrintCmd.SetRotate(rotate), mUsbDev)
            // 下划线
            mUsbDriver?.write(PrintCmd.SetUnderline(underLine), mUsbDev)
            //行大小
            mUsbDriver?.write(PrintCmd.SetLinespace(linespace), mUsbDev)

            //标题
            val title = PrintCmd.PrintString("中新友好图书馆-个人借阅凭条", 0)
            mUsbDriver?.write(PrintCmd.SetAlignment(1), mUsbDev)
//            mUsbDriver?.write(PrintCmd.SetSizetext(2, 2), mUsbDev)
            mUsbDriver?.write(PrintCmd.SetBold(1))
            mUsbDriver?.write(title)

            //内容
            val name = "allever"
            val readerId = "111****11"
            val date = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date())
            val currentBorrowBooks = "《红楼梦》、《大耳朵图图》"
            val registerDays = "365"
            val currentBorrowCount = "8"
            val canBorrowCount = "2"
            val deadLine = SimpleDateFormat("yyyy年MM月dd日").format(Date())
            val totalBorrowCount = "86"
            val savingMoney = "300"
            val tips = resources.getString(R.string.receipt_tips, registerDays, currentBorrowCount, canBorrowCount, deadLine, totalBorrowCount, savingMoney)


            val printStringContent = "\n姓名：$name" +
            "\n证号：$readerId" +
            "\n日期：$date" +
            "\n当前借阅：$currentBorrowBooks" +
            "\n\n$tips\n"
            mUsbDriver?.write(PrintCmd.SetAlignment(align), mUsbDev)
//            mUsbDriver?.write(PrintCmd.SetSizetext(1, 1), mUsbDev)
            mUsbDriver?.write(PrintCmd.SetBold(0))
            val content = PrintCmd.PrintString(printStringContent, 0)
            mUsbDriver?.write(content, content.size, mUsbDev)
//            //打印时间
//            val printTime = PrintCmd.PrintString(
//                "打印时间：" +
//                        SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date()) + "\n\n", 0
//            )
//            mUsbDriver?.write(printTime, printTime.size, mUsbDev)

            //打印二维码
            mUsbDriver?.write(PrintCmd.PrintQrcode("baidu.com", 26, 8, 0))
            //打印换行
            val emptyLine = PrintCmd.PrintString("\n", 0)
            mUsbDriver?.write(emptyLine, emptyLine.size, mUsbDev)

//            //打印二维码提示
//            val qrCodeTips = PrintCmd.PrintString("\n扫码关注公众号\n", 0)
//            mUsbDriver?.write(PrintCmd.SetAlignment(1), mUsbDev)
//            mUsbDriver?.write(qrCodeTips, qrCodeTips.size, mUsbDev)

            //打印logo
            val filePath = Environment.getExternalStorageDirectory().absolutePath + File.separator + packageName + File.separator + "logo.png"
            if (!createFile(filePath)) {
                return
            }
            val inputBmp = Utils.getBitmapData(filePath) ?: return
            mIvImage.setImageBitmap(inputBmp)
            val data = Utils.getPixelsByBitmap(inputBmp)
            mUsbDriver?.write(PrintCmd.PrintDiskImagefile(data, inputBmp.width, inputBmp.height))


            mUsbDriver?.write(PrintCmd.PrintFeedline(4))   // 走纸换行
            mUsbDriver?.write(PrintCmd.PrintCutpaper(cutter)) // 切纸类型
            mUsbDriver?.write(PrintCmd.SetClean())           // 清除缓存,初始化
        } else {
            Log.d(TAG, "printerStatus == -1")
            T.showShort(this, getString(R.string.PrintException))
        }
    }

    private fun createFile(filePath: String): Boolean {
        val file = File(filePath)
        if (file.exists()) {
            return true
        }

        val parent = file.parentFile
        if (!parent.exists()) {
            parent.mkdirs()
        }

        //val bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo1)
        return saveBitmapFile(filePath).length() != 0L

    }

    private fun saveBitmapFile( filepath: String): File {
        val assetManager = assets
        val inputStream = assetManager.open("logo1.png")
        val bufferedInputStream = BufferedInputStream(inputStream)
        val byteArrayOutputStream = ByteArrayOutputStream()
        val fileOutputStream = FileOutputStream(File(filepath))
        try {
            //如果要跳过1个字节数，传的是1
            //跳过数据头，读取源文件数据
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


        val file = File(filepath)//将要保存图片的路径
        return file
    }

    private fun checkStatus(status: Int): Int {
        var iRet = -1

        val sMsg = StringBuilder()


        //0 打印机正常 、1 打印机未连接或未上电、2 打印机和调用库不匹配
        //3 打印头打开 、4 切刀未复位 、5 打印头过热 、6 黑标错误 、7 纸尽 、8 纸将尽
        when (status) {
            0 -> {
                sMsg.append(normal)       // 正常
                iRet = 0
            }
            8 -> {
                sMsg.append(paperWillExh) // 纸将尽
                iRet = 0
            }
            3 -> {
                sMsg.append(printerHeadOpen) //打印头打开
                T.showShort(this@PrintTestActivity, printerHeadOpen)
            }
            4 -> {
                sMsg.append(cutterNotReset)
                T.showShort(this@PrintTestActivity, cutterNotReset)
            }  //切刀未复位
            5 -> {
                sMsg.append(printHeadOverheated)
                T.showShort(this@PrintTestActivity, printHeadOverheated)
            } //打印头过热
            6 -> {
                sMsg.append(blackMarkError)
                T.showShort(this@PrintTestActivity, blackMarkError)
            }  //黑标错误
            7 -> {
                sMsg.append(paperExh)
                T.showShort(this@PrintTestActivity, paperExh)
            }     // 纸尽==缺纸
            1 -> {
                sMsg.append(notConnectedOrNotPopwer)
                T.showShort(this@PrintTestActivity, notConnectedOrNotPopwer)
            }//打印机未连接或未上电
            else -> {
                sMsg.append(abnormal)
                T.showShort(this@PrintTestActivity, abnormal)
            }     // 异常
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


    /*
     *  BroadcastReceiver when insert/remove the device USB plug into/from a USB port
     *  创建一个广播接收器接收USB插拔信息：当插入USB插头插到一个USB端口，或从一个USB端口，移除装置的USB插头
     */
    internal inner class UsbReceiver : BroadcastReceiver() {
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
                        T.showShort(this@PrintTestActivity, "permission denied for device")
                    }
                }
            }
        }
    }

}