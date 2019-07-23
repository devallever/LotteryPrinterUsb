package com.example.usbprintertest

class PrintData {
    val type: Int = 0
    val text: String = ""
    val hidden: String = "0"
    val config: PrintConfig? = null
}

class PrintConfig {
    var blob: Int = 0
    var align: Int = 0
    var textScale: Int = 0
    var underLine: Int = 0
    var italic: Int = 0
    var lineSpace: Int = 40
    var textSpace: Int = 0
    var leftMargin: Int = 0
    var rightMargin: Int = 0
    var qrLeftMargin: Int = 0
    var qrScale: Int = 8
    var imageUrl: String = ""
    var tableIndexList: MutableList<Int>? = null
    var tableColumn: MutableList<TableColumn>? = null

    override fun toString(): String {
        return "\nblob[$blob]\n" +
                "align[$align]\n" +
                "textScale[$textScale]\n" +
                "underLine[$underLine]\n" +
                "italic[$italic]\n" +
                "lineSpace[$lineSpace]\n" +
                "textSpace[$textSpace]\n" +
                "leftMargin[$leftMargin]\n" +
                "rightMargin[$rightMargin]\n" +
                "qrLeftMargin[$qrLeftMargin]\n" +
                "qrScale[$qrScale]\n" +
                "imageUrl[$imageUrl]\n"
    }
}

class TableColumn {
    var data: MutableList<String>? = null
}