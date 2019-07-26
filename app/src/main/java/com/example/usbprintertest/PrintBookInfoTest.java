package com.example.usbprintertest;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            String result = formatLoanPrintData(bookName, "2019-10-21", "30");
            System.out.println(result);
            System.out.println();
        }
    }

    private static String formatLoanPrintData(String bookName, String returnDate, String leftDays) {
        if (bookName == null || "".equals(bookName)) {
            return "";
        }
        StringBuilder resultBuilder = new StringBuilder();
        try {
            byte[] bookNameGBKBytes = bookName.getBytes("GBK");
//            System.out.println("bookName = " + bookName);
            int gbkBookNameLength = bookNameGBKBytes.length;
            //判断书名长度，限制显示一行，超出用省略号
            String shortBookName;
            if (gbkBookNameLength > 18) {
                String lastText = new String(bookNameGBKBytes, 14, 2, "gbk");
//                System.out.println("lastText = " + lastText + "\nlength = " + lastText.length());
                if (bookName.contains(lastText)) {
//                    System.out.println("正常字符");
                    shortBookName = new String(bookNameGBKBytes, 0, 16, "GBK");
                } else {
//                    System.out.println("异常字符");
                    shortBookName = new String(bookNameGBKBytes, 0, 17, "GBK");
                }

                //超出的内容用省略号代替
                bookName = shortBookName + "...";
//                System.out.println("shortBookName = " + shortBookName);
            }

            resultBuilder.append("《").append(bookName).append("》");

            //根据书名长度计算需要制表符个数
            int tableCount = calcTableCount(bookName);
//            System.out.println("tableCount = " + tableCount);
            for (int i = 0; i < tableCount; i++) {
                resultBuilder.append("\t");
            }
            resultBuilder.append(returnDate);
            resultBuilder.append("\t");
            resultBuilder.append("    ").append(leftDays);
            return resultBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static int calcTableCount(String bookName) {
        int length = bookName.length();
        int chineseCount = 0;
        for (int i = 0 ; i< bookName.length(); i++) {
            char c = bookName.charAt(i);
            if (isChineseChar(c)) {
                chineseCount ++;
            }
        }

//        System.out.println("总长度 = " + length);
//        System.out.println("汉字个数 = " + chineseCount);

        int otherCount = length - chineseCount;
        int otherPartCount = otherCount / 2;

        int requireLength = chineseCount + otherPartCount;

        int tabCount = 0;

        if (requireLength > 0 && requireLength <= 1 ) tabCount = 3;
        else if (requireLength > 1 && requireLength <= 5) tabCount = 2;
        else if (requireLength > 5 && requireLength <= 9) tabCount = 1;
        else if (requireLength  == 10) tabCount = 0;

//        System.out.println("汉字长度 = " + requireLength + "\ttabCount = " + tabCount);
        return tabCount;
    }

    /**
     * 校验一个字符是否是汉字
     *
     * @param c
     *  被校验的字符
     * @return true代表是汉字
     */
    private static boolean isChineseChar(char c) {
        try {
            return String.valueOf(c).getBytes("UTF-8").length > 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
