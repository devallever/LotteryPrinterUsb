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
                print()
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


        val printResult = PrintManager.appendString(title, PrintManager.PRINT_MIDDLE, 1)
            .appendString("姓名：$name 左边距 10", leftMargin = 10)
            .appendString("证号：$readerId 左边距 0 间距 = 3", textMargin = 3)
            .appendString("日期：$date 间距 = 2", textMargin = 2)
            .appendString("当前借阅：$currentBorrowBooks")
            .appendString("\n$tips 间距 = 0 斜体", italic = 1, lineHeight = 30)
//            .appendString(content)
//            .appendQRCode(qrCodeContent, 26, 8)
            .appendString("")
//            .appendImage(this, filePath)
            .print()
        if (printResult) {
            Log.d(TAG, "打印成功")
        } else {
            Log.d(TAG, "打印失败")
        }
    }
}