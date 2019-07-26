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
//        String testStr = "安卓源码设计模式解析";
//        int commonByte = testStr.getBytes().length;
//        int gbkByte = testStr.getBytes("GBK").length;
//        System.out.println("commonByteLength = " + commonByte);
//        System.out.println("gbkByteLength = " + gbkByte);
//
//        try {
//            String result = insertStr(8, "\n", testStr);
//            System.out.println("result = \n" + result);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        List<String> bookNameList = new ArrayList<String>();
        bookNameList.add("楼");
        bookNameList.add("大耳朵贴图");
        bookNameList.add("Android开发艺术探索");
        bookNameList.add("安卓源码设计模式解析");
        bookNameList.add("hello安卓");
        bookNameList.add("aaaaaaaaaaaa");
        bookNameList.add("七七七七七七七");
        bookNameList.add("八八八八八八八八");
        bookNameList.add("九九九九九九九九九");
        bookNameList.add("serx十十十十十十十十");
        bookNameList.add("十一一一一一一一一一一");
        bookNameList.add("十二ses二二二二二二二二二");
        bookNameList.add("十三三3三三三三三三三三三");
        bookNameList.add("十四四四四四四四四四四四四四");
        bookNameList.add("十五五五五五五五五五ddddddsele");
        bookNameList.add("十六六六5六六六六六六六六六六六");
        bookNameList.add("十七七七七aa七5七七aaaaa七aaa七aa");
        bookNameList.add("十八八八八八八efef八八八八八八八八八");
        bookNameList.add("十九九九九九九九九九九九九九九九九九九");
        bookNameList.add("二6十十十十十十十十十十十十十十十十十十");
        bookNameList.add("二十一一一一一一一一一一一一一一一一一一一");
        bookNameList.add("二十二二9二二二二二二二二二二二二二二androfsfsafss");
        bookNameList.add("二十三三se三三47三三三三三ES三三三GR三三三三三");
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
            System.out.println("bookName = " + bookName);
            int gbkBookNameLength = bookNameGBKBytes.length;
            //判断书名长度，限制显示一行，超出用省略号
            String shortBookName = "";
            if (gbkBookNameLength > 18) {
                String lastText = new String(bookNameGBKBytes, 14, 2, "gbk");
                System.out.println("lastText = " + lastText + "\nlength = " + lastText.length());
                if (bookName.contains(lastText)) {
                    System.out.println("正常字符");
                    shortBookName = new String(bookNameGBKBytes, 0, 16, "GBK");
                } else {
                    System.out.println("异常字符");
                    shortBookName = new String(bookNameGBKBytes, 0, 17, "GBK");
                }

                //超出的内容用省略号代替
                bookName = shortBookName + "...";
                System.out.println("shortBookName = " + shortBookName);
            }
//            gbkBookNameLength = bookName.getBytes("GBK").length;
//          //插入换行符
//            if (gbkBookNameLength >= 22) {
//                String formatBookName = insertStr(10, "\n", bookName);
//                resultBuilder.append("《").append(formatBookName).append("》");
//            } else  {
//                resultBuilder.append("《").append(bookName).append("》");
//            }

            resultBuilder.append("《").append(bookName).append("》");

            //根据书名长度计算需要制表符个数
            int tableCount = calcTableCount(bookName);
            System.out.println("tableCount = " + tableCount);
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
        else if (requireLength > 10 && requireLength <= 12) tabCount = 3;
        else if (requireLength > 12 && requireLength <= 16) tabCount = 2;
        else if (requireLength > 16 && requireLength <= 20) tabCount = 1;
        else if (requireLength >= 21) tabCount = 0;

        System.out.println("汉字长度 = " + requireLength + "\ttabCount = " + tabCount);
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

    /**
     * 插入方法
     *
     * @param num
     *            每隔几个字符插入一个字符串（中文字符）
     * @param splitStr
     *            待指定字符串
     * @param str
     *            原字符串
     * @return 插入指定字符串之后的字符串
     * @throws UnsupportedEncodingException
     */
    private static String insertStr(int num, String splitStr, String str) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer();
        String temp = str;

        int len = str.length();
        while (len > 0) {
            int idx = getEndIndex(temp, num);
            sb.append(temp.substring(0, idx + 1));
            //解决最后位总是插入
            String value = temp.substring(0, idx + 1);
            int length = value.getBytes("GBK").length;
            if (length >= 20) {
                sb.append(splitStr);
            }

            System.out.println("value = \n" + sb);
            temp = temp.substring(idx + 1);
            len = temp.length();
        }

        return sb.toString();
    }

    /**
     * 两个数字/英文
     *
     * @param str
     *            字符串
     * @param num
     *            每隔几个字符插入一个字符串
     * @return int 最终索引
     * @throws UnsupportedEncodingException
     */
    private static int getEndIndex(String str, double num) throws UnsupportedEncodingException {
        int idx = 0;
        double val = 0.00;
        // 判断是否是英文/中文
        for (int i = 0; i < str.length(); i++) {
            if (String.valueOf(str.charAt(i)).getBytes("UTF-8").length >= 3) {
                // 中文字符或符号
                val += 1.00;
            } else {
                // 英文字符或符号
                val += 0.50;
            }
            if (val >= num) {
                idx = i;
                if (val - num == 0.5) {
                    idx = i - 1;
                }
                break;
            }
        }
        if (idx == 0) {
            idx = str.length() - 1;
        }
        return idx;
    }

    public static boolean isChinese(String str) {
        String regEx = "[\u4e00-\u9fa5]";
        Pattern pat = Pattern.compile(regEx);
        Matcher matcher = pat.matcher(str);
        boolean flg = false;
        if (matcher.find())
            flg = true;

        return flg;
    }
}
