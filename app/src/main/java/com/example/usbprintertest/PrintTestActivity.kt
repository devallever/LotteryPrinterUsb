package com.example.usbprintertest

import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class PrintTestActivity : Activity(), View.OnClickListener {

    private val TAG = "PrintTestActivity"

    private lateinit var mBtnPrint: Button
    private lateinit var mIvImage: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print_test)

        ToastUtil.initToast(this)

        initData()
        initView()

        PrintManager.initPrinter(this)

//        val url = "https://raw.githubusercontent.com/devallever/LotteryPrinterUsb/master/app/src/main/assets/print_config.json"
//        PrintHelper.getPrintConfig(this, url)
    }

    override fun onDestroy() {
        super.onDestroy()
        PrintManager.destroy(this)
    }

    private fun initData() {
    }

    private fun initView() {
        mBtnPrint = findViewById(R.id.btn_print)
        mBtnPrint.setOnClickListener(this)
        mIvImage = findViewById(R.id.image)
    }


    override fun onClick(v: View?) {
        when (v) {
            mBtnPrint -> {
//                print()
                val testUrl = "http://rc.interlib.com.cn:8088/rcrobotsite//web/api/rcrobot/print/rule/getFilledFormatJson.html?libcode=ETJ022005&deviceId=1111&rdid=441284199304265211&tdsourcetag=s_pcqq_aiomsg"
                val url =
                    "https://raw.githubusercontent.com/devallever/LotteryPrinterUsb/master/app/src/main/assets/print_config.json"
                PrintHelper.getPrintConfig(this, testUrl)
            }
        }
    }

    private fun print() {
        if (!PrintManager.canPrint(this)) {
            return
        }

        val title = "中新友好图书馆-个人借阅凭条"
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
        val tips = resources.getString(
            R.string.receipt_tips,
            registerDays,
            currentBorrowCount,
            canBorrowCount,
            deadLine,
            totalBorrowCount,
            savingMoney
        )


        val content = "\n姓名：$name" +
                "\n证号：$readerId" +
                "\n日期：$date" +
                "\n当前借阅：$currentBorrowBooks" +
                "\n\n$tips\n"

        val qrCodeContent = "baidu.com"

        val filePath =
            Environment.getExternalStorageDirectory().absolutePath + File.separator + packageName + File.separator + "logo.png"

        val qrcodePath =
            Environment.getExternalStorageDirectory().absolutePath + File.separator + packageName + File.separator + "tianjinzhongxin.jpg"

        val indexList = mutableListOf(27, 40)
        val bookNameList = mutableListOf("当前借阅: ", "《红楼梦》", "《大耳朵图图》")
        val returnDateList = mutableListOf("应还日期", "2019-10-30", "2019-10-30")
        val remainDayList = mutableListOf("剩余天数", "30", "30")
        val tableList = mutableListOf<MutableList<String>>()
        tableList.add(bookNameList)
        tableList.add(returnDateList)
        tableList.add(remainDayList)


        val printResult = PrintManager.appendString(title, PrintManager.PRINT_MIDDLE, 1)
            .appendString("姓名：$name")
            .appendString("证号：$readerId")
            .appendString("日期：$date")
            .appendTable(indexList, tableList)
//            .appendString("当前借阅：$currentBorrowBooks")
            .appendString("\n$tips")
//            .appendString(content)
//            .appendQRCode(qrCodeContent, 26, 8)
            .appendString("")
            .appendImage(qrcodePath)
            .appendImage(filePath)
            .print()
        if (printResult) {
            Log.d(TAG, "打印成功")
        } else {
            Log.d(TAG, "打印失败")
        }
    }
}