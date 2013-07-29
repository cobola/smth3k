package com.jimidigi.smth3k.common;


import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtility {

    public static final String LOGINED = "logined";
    public static final String GUEST_LOGINED = "guest_logined";
    public static final String LOGINED_ID = "loginedID";

    public static final String LOGOUT = "logout";

    public static final String URL = "url";

    public static final String USERID = "userid";
    public static final String BOARD = "board";
    public static final String BID = "bid";
    public static final String SUBJECT_ID = "subjectID";
    public static final String AUTHOR = "author";
    public static final String TITLE = "title";
    public static final String SUBJECT = "subject";
    public static final String POST = "post";
    public static final String BOARD_TYPE = "boardType";
    public static final String SUBJECT_LIST = "subjectList";
    public static final String PROFILE = "profile";
    public static final String MAIL_BOX_TYPE = "boxType";
    public static final String MAIL = "mail";
    public static final String WRITE_TYPE = "write_type";
    public static final String IS_REPLY = "is_reply";
    public static final String REFRESH_BOARD = "refreshBoard";

    public final static SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyy-MM-dd HH:mm:ss");
    public final static SimpleDateFormat dayFormat = new SimpleDateFormat(
            "yyy-MM-dd");


    public final static java.util.regex.Pattern dateplustime = Pattern.compile("(\\d{4})[\\-\\\\/\\s]?(\\d{1,2})[\\-\\\\/\\s]?(\\d{1,2})[\\s]?(\\d{2}):(\\d{2}):(\\d{2})");
    public final static java.util.regex.Pattern onlytime = Pattern.compile("(\\d{2}):(\\d{2}):(\\d{2})");
    public final static java.util.regex.Pattern onlydate = Pattern.compile("(\\d{4})[\\-\\\\/\\s]?(\\d{1,2})[\\-\\\\/\\s]?(\\d{1,2})");


    /**
     * 从链接中提取相关参数
     */
    public static Map<String, String> getUrlParams(String url) {
        Map<String, String> paramMap = new HashMap<String, String>();
        if (!url.equals("")) {
            url = url.substring(url.indexOf('?') + 1);
            String paramaters[] = url.split("&");
            for (String param : paramaters) {
                String values[] = param.split("=");
                if (values.length > 1) {
                    paramMap.put(values[0], values[1]);
                }
            }
        }
        return paramMap;
    }


    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        }

        // handle negatives
        if (end < 0) {
            end = str.length() + end; // remember end is negative
        }
        if (start < 0) {
            start = str.length() + start; // remember start is negative
        }

        // check length next
        if (end > str.length()) {
            end = str.length();
        }

        // if start is greater than end, return ""
        if (start > end) {
            return "";
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }

    public static boolean isEmpty(String str) {
        return str == null || str.equals("");
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static Date toDate(String dateStr) {
        try {
            return new Date(Long.valueOf(dateStr) * 1000);
        } catch (Exception e) {
            return new Date();
        }
    }

    /**
     * convert string to java.util.Date。
     *
     * @param str         source date of string
     * @param fromPattern dateplustime of str
     * @return java.util.Date object date
     * @throws java.text.ParseException
     */
    public static Date strToUtilDate(String str, String fromPattern)
            throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(fromPattern);
        Date d = format.parse(str);
        return d;
    }

    public static Date strToUtilDate(String str)
            throws ParseException {
        String fromPattern = "yyyy-MM-dd hh:mm:ss";
        return strToUtilDate(str, fromPattern);
    }

    private static String subStringBetween(String line, String str1, String str2) {
        int idx1 = line.indexOf(str1);
        int idx2 = line.indexOf(str2);
        return line.substring(idx1 + str1.length(), idx2);
    }

    private static String getSubString(int start, int end, String content) {
        return content.substring(start, end).trim();
    }

    private static int getIntFromSubString(int start, int end, String content) {
        return Integer.parseInt(content.substring(start, end).trim());
    }


    public static Object[] parsePostContent(String content) {
        Date date = new Date();
        if (content == null) {
            return new Object[]{"", date};
        }
        content = content.replace("\\n", "\n").replace("\\r", "\r")
                .replace("\\/", "/").replace("\\\"", "\"").replace("\\'", "'");
        String[] lines = content.split("\n");
        StringBuilder sb = new StringBuilder();
        int linebreak = 0;
        int linequote = 0;
        int seperator = 0;
        for (String line : lines) {
            if (line.startsWith("发信人:") || line.startsWith("寄信人:")) {                /*
                 * line = "<font color=#6699FF>" +
				 * MyUtils.subStringBetween(line, "发信人: ", ", 信区:") + "</font>";
				 * sb.append(line);
				 */
                continue;
            } else if (line.startsWith("标  题:")) {
                continue;
            } else if (line.startsWith("发信站:")) {
                line = subStringBetween(line, "(", ")");
                SimpleDateFormat sdf = new SimpleDateFormat(
                        "EEE MMM d HH:mm:ss yyyy", Locale.US);
                try {
                    date = sdf.parse(line);
                    continue;
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }
            if (line.equals("--")) {
                if (seperator > 0) {
                    break;
                }

                seperator++;
            } else {
                if (seperator > 0) {
                    if (line.length() > 0) {
                        line = "<font color=#33CC66>" + line + "</font>";
                    } else {
                        continue;
                    }
                }
            }

            if (line.startsWith(":")) {
                linequote++;
                if (linequote > 5) {
                    continue;
                } else {
                    line = "<font color=#006699>" + line + "</font>";
                }
            } else {
                linequote = 0;
            }

            if (line.equals("")) {
                linebreak++;
                if (linebreak > 1) {
                    continue;
                }
            } else {
                linebreak = 0;
            }

            if (line.contains("※ 来源:·水木社区")) {
                break;
            }
            sb.append(line).append("<br />");
        }

        String result = sb.toString().trim();
        return new Object[]{result, date};
    }

    public static int parseInt(String text) {
        if (text.trim().length() > 0) {
            return Integer.parseInt(text.trim());
        } else {
            return 0;
        }
    }

    public static int filterUnNumber(String str) {
        String regExpression = "[^0-9]";
        Pattern pattern = Pattern.compile(regExpression);
        Matcher matcher = pattern.matcher(str);
        String temp = matcher.replaceAll("").trim();
        if (temp.length() > 0) {
            return parseInt(temp);
        } else {
            return 0;
        }
    }

    public static String filterUrl(String str) {
        str = str.replace("m.newsmth.net", "");
        String regExpression = "[^a-zA-Z0-9_]+";
        Pattern pattern = Pattern.compile(regExpression);
        Matcher matcher = pattern.matcher(str);
        return matcher.replaceAll("").trim();

    }


    public synchronized static Date parseDate(String date) {


        String d = filterDate(dateplustime, date);
        if (StringUtility.isEmpty(d)) {
            d = filterDate(onlydate, date);
            if (StringUtility.isEmpty(d)) {
                d = filterDate(onlytime, date);
                if (StringUtility.isNotEmpty(d)) {
                    Date tmp = new Date();
                    d = dayFormat.format(tmp) + " " + d;
                }
            } else {
                d = d + " 00:00:00";
            }
        }

        Date d2 = null;
        try {
            d2 = dateFormat.parse(d);
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return d2;

    }



    public synchronized static String filterDate(Pattern p, String date) {
        Matcher matcher = p.matcher(date);

        StringBuffer sb = new StringBuffer();
        if (matcher.find()) {
            sb.append(matcher.group());
        }

        return sb.toString();

    }


    /**
     * 字符串转整数
     *
     * @param str
     * @param defValue
     * @return
     */
    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
        }
        return defValue;
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static int toInt(Object obj) {
        if (obj == null) return 0;
        return toInt(obj.toString(), 0);
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static long toLong(String obj) {
        try {
            return Long.parseLong(obj);
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 字符串转布尔值
     *
     * @param b
     * @return 转换异常返回 false
     */
    public static boolean toBool(String b) {
        try {
            return Boolean.parseBoolean(b);
        } catch (Exception e) {
        }
        return false;
    }


    public static String filterNullBr(String content) {
        if (isEmpty(content)) return "";
        content = content.replaceAll("\\n[\\n]", "\\n");
        content = content.replaceAll("<br*>[<br*>]", "<br/>");
        return content;
    }

    public static boolean isNotEmpty(Elements tmp) {
        return tmp != null && tmp.size() > 0;
    }


}
