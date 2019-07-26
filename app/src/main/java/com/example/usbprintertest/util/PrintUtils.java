package com.example.usbprintertest.util;

public class PrintUtils {

    /***
     * 格式化 打印借书信息
     * @param bookName 书名
     * @param returnDate 应还日期 2019-01-01
     * @param leftDays 剩余天数 30
     * @return
     */
    public static String formatLoanPrintData(String bookName, String returnDate, String leftDays) {
        if (bookName == null || "".equals(bookName)) {
            return "";
        }
        StringBuilder resultBuilder = new StringBuilder();
        try {
            String formattedBookName = formatBookName(bookName);

            resultBuilder.append(formattedBookName);

            //根据书名长度计算需要制表符个数
            int tableCount = calcTableCount(formattedBookName);
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

    /***
     * 格式化书名
     * @param bookName 书名
     * @return 《书名》
     */
    public static String formatBookName(String bookName){
        StringBuilder resultBuilder = new StringBuilder();
        if (bookName == null || "".equals(bookName)) {
            return resultBuilder.append("《》").toString();
        }

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
        } catch (Exception e) {
            e.printStackTrace();
            return resultBuilder.append("《》").toString();
        }
        return resultBuilder.append("《").append(bookName).append("》").toString();
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
