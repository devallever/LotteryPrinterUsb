package com.example.usbprintertest;

import com.example.usbprintertest.util.PrintUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by allever on 19-7-22.
 */
public class PrintBookInfoTest {
    public static void main(String[] args) throws UnsupportedEncodingException {
        List<String> bookNameList = new ArrayList<String>();
        bookNameList.add("楼");
        bookNameList.add("大耳朵贴图");
        bookNameList.add("Android开发艺术探索");
        bookNameList.add("安卓源码设计模式解析");
        bookNameList.add("一二三四五六七八九十");
        bookNameList.add("0一二三四五六七八九十");
        bookNameList.add("01一二三四五六七八九十");


        for (String bookName: bookNameList) {
            String result = PrintUtils.formatLoanPrintData(bookName, "2019-10-21", "30");
            System.out.println(result);
            System.out.println();
        }
    }
}
