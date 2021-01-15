package com.appleframework.config.core.util;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
	
	public static boolean isNullOrEmpty(String s) {
		if( s == null || s.trim().length() == 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean isMac(String mac) {
		String regex = "[a-f\\d]{2}[a-f\\d]{2}[a-f\\d]{2}[a-f\\d]{2}[a-f\\d]{2}[a-f\\d]{2}";
		if(mac != null && mac.toLowerCase().matches(regex) ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * 按默认样式(yyyy-MM-dd)格式化日期
	 * @param date
	 * @return
	 */
	public static String dateFormat(Date date) {
		if (date!=null) {
			DateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
			return sf.format(date);
		}else {
			return "";
		}
		
	}
	
	/**
	 * 格式化日期
	 * @param date
	  * @param style 显示的样式 如:yyyy-MM-dd HH:mm
	 * @return
	 */
	public static String dateFormat(Date date,String style) {
		if (date!=null) {
			DateFormat sf=new SimpleDateFormat(style);
			return sf.format(date);
		}else {
			return "";
		}
	}
	
	/**
	 * 去除java字符串里面的特殊字符
	 * @param 
	  * @param 
	 * @return
	 */
	public String stringFilter(String str) {
		// 只允许字母和数字
		// String regEx = "[^a-zA-Z0-9]";
		// 清除掉所有特殊字符
		String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim();
	}
	
	/**
	 * 判断指定字符串是否全部为数字
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isDigit(String s) {
		if (isNullOrEmpty(s)) {
			return false;
		}

		for (int i = s.length() - 1; i >= 0; i--) {
			if (!Character.isDigit(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	// Trim
    //-----------------------------------------------------------------------
    /**
     * <p>Removes control characters (char &lt;= 32) from both
     * ends of this String, handling {@code null} by returning
     * {@code null}.</p>
     *
     * <p>The String is trimmed using {@link String#trim()}.
     * Trim removes start and end characters &lt;= 32.
     * To strip whitespace use {@link #strip(String)}.</p>
     *
     * <p>To trim your choice of characters, use the
     * {@link #strip(String, String)} methods.</p>
     *
     * <pre>
     * StringUtils.trim(null)          = null
     * StringUtils.trim("")            = ""
     * StringUtils.trim("     ")       = ""
     * StringUtils.trim("abc")         = "abc"
     * StringUtils.trim("    abc    ") = "abc"
     * </pre>
     *
     * @param str  the String to be trimmed, may be null
     * @return the trimmed string, {@code null} if null String input
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }
    
    
 // 判断一个字符串是否null或""
 	public static boolean isEmpty(String str) {
 		if (str == null) {
 			return true;
 		}
 		else if (str.length() == 0) {
 			return true;
 		}
 		else if ("null".equals(str)) {
 			return true;
 		}
 		else if ("0".equals(str)) {
 			return true;
 		}
 		return false;
 	}
 	
 	public static boolean isEmptyString(String str) {
 		return (str == null) || (str.trim().length() == 0);
 	}

 	public static String replaceAll(String s, String oss, String nss) {
 		if (null == s || oss == null || nss == null) {
 			return s;
 		}
 		String rlt = s;
 		StringBuffer sb = new StringBuffer();
 		while (true) {
 			int idx = rlt.indexOf(oss);
 			if (idx < 0) {
 				break;
 			}
 			sb.delete(0, sb.length());
 			if (idx > 0) {
 				sb.append(rlt.substring(0, idx));
 			}
 			sb.append(nss);
 			sb.append(rlt.substring(idx + oss.length()));
 			rlt = sb.toString();
 		}
 		return rlt;
 	}

 	/**
 	 * 字符串通过分隔符增加名值对
 	 * 
 	 * @param string
 	 * @param name
 	 * @param value
 	 * @param valueSeparate
 	 * @param paramSeparate
 	 * @return param
 	 */
 	public static String appendParam(String string, String name, String value,
 			String valueSeparate, String paramSeparate) {
 		StringBuffer sb = new StringBuffer();
 		if (null == string || "".equals(string)) {
 			sb.append(name);
 			sb.append(valueSeparate);
 			sb.append(value);
 		} else {
 			sb.append(string);
 			sb.append(paramSeparate);
 			sb.append(name);
 			sb.append(valueSeparate);
 			sb.append(value);
 		}
 		return sb.toString();
 	}

 	/**
 	 * 字符串通过分隔符及参数名查找参数值
 	 * 
 	 * @param string
 	 *            原字符串
 	 * @param name
 	 *            分隔名
 	 * @param valueSeparate
 	 *            值分隔符
 	 * @param paramSeparate
 	 *            参数分隔符
 	 * @return 从字符串里取出被分隔的串
 	 */
 	public static String getValueFromString(String string, String name,
 			String valueSeparate, String paramSeparate) {
 		String[] params = string.split(paramSeparate);
 		for (int i = 0; i < params.length; i++) {
 			String[] param = params[i].split(valueSeparate);
 			if (param != null && param.length > 0 && param[0].equals(name)) {
 				if (param.length > 1) {
 					return param[1];
 				}
 				else {
 					return null;
 				}
 			}
 		}
 		return null;
 	}

 	/**
 	 * 获取唯一的标识值
 	 * 
 	 * @return
 	 */
 	public static String getUUID() {
 		return UUID.randomUUID().toString();
 	}

 	/**
 	 * 根据用","隔开的字符Id转换成list
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public static List<Integer> getListId(String id) {
 		String[] str = id.split(",");
 		List<Integer> list = new ArrayList<Integer>();
 		for (int i = 0; i < str.length; i++) {
 			list.add(Integer.parseInt(str[i]));
 		}
 		return list;
 	}

 	/**
 	 * 判断两个字符串是否相等
 	 * 
 	 * @param arg1
 	 * @param arg2
 	 * @return
 	 */
 	public static Boolean isEqualString(String arg1, String arg2) {
 		return arg1.equals(arg2);
 	}

 	/**
 	 * 格式化字符串
 	 * 
 	 * @param arg
 	 * @param objects
 	 * @return
 	 */
 	public static String formatterString(String arg, Object... objects) {
 		return MessageFormat.format(arg, objects);
 	}

 	
 	static boolean equalsHandlingNull(Object a, Object b) {
        if (a == null && b != null)
            return false;
        else if (a != null && b == null)
            return false;
        else if (a == b) // catches null == null plus optimizes identity case
            return true;
        else
            return a.equals(b);
    }

    static boolean isC0Control(int codepoint) {
      return (codepoint >= 0x0000 && codepoint <= 0x001F);
    }

    public static String renderJsonString(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append('"');
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            switch (c) {
            case '"':
                sb.append("\\\"");
                break;
            case '\\':
                sb.append("\\\\");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\r':
                sb.append("\\r");
                break;
            case '\t':
                sb.append("\\t");
                break;
            default:
                if (isC0Control(c))
                    sb.append(String.format("\\u%04x", (int) c));
                else
                    sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    static String renderStringUnquotedIfPossible(String s) {
        // this can quote unnecessarily as long as it never fails to quote when
        // necessary
        if (s.length() == 0)
            return renderJsonString(s);

        // if it starts with a hyphen or number, we have to quote
        // to ensure we end up with a string and not a number
        int first = s.codePointAt(0);
        if (Character.isDigit(first) || first == '-')
            return renderJsonString(s);

        if (s.startsWith("include") || s.startsWith("true") || s.startsWith("false")
                || s.startsWith("null") || s.contains("//"))
            return renderJsonString(s);

        // only unquote if it's pure alphanumeric
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (!(Character.isLetter(c) || Character.isDigit(c) || c == '-'))
                return renderJsonString(s);
        }

        return s;
    }

    static boolean isWhitespace(int codepoint) {
        switch (codepoint) {
        // try to hit the most common ASCII ones first, then the nonbreaking
        // spaces that Java brokenly leaves out of isWhitespace.
        case ' ':
        case '\n':
        case '\u00A0':
        case '\u2007':
        case '\u202F':
            // this one is the BOM, see
            // http://www.unicode.org/faq/utf_bom.html#BOM
            // we just accept it as a zero-width nonbreaking space.
        case '\uFEFF':
            return true;
        default:
            return Character.isWhitespace(codepoint);
        }
    }

    public static String unicodeTrim(String s) {
        // this is dumb because it looks like there aren't any whitespace
        // characters that need surrogate encoding. But, points for
        // pedantic correctness! It's future-proof or something.
        // String.trim() actually is broken, since there are plenty of
        // non-ASCII whitespace characters.
        final int length = s.length();
        if (length == 0)
            return s;

        int start = 0;
        while (start < length) {
            char c = s.charAt(start);
            if (c == ' ' || c == '\n') {
                start += 1;
            } else {
                int cp = s.codePointAt(start);
                if (isWhitespace(cp))
                    start += Character.charCount(cp);
                else
                    break;
            }
        }

        int end = length;
        while (end > start) {
            char c = s.charAt(end - 1);
            if (c == ' ' || c == '\n') {
                --end;
            } else {
                int cp;
                int delta;
                if (Character.isLowSurrogate(c)) {
                    cp = s.codePointAt(end - 2);
                    delta = 2;
                } else {
                    cp = s.codePointAt(end - 1);
                    delta = 1;
                }
                if (isWhitespace(cp))
                    end -= delta;
                else
                    break;
            }
        }
        return s.substring(start, end);
    }
		
}
