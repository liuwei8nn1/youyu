package com.youyu.common.util;

import lombok.extern.slf4j.Slf4j;

/**
 * 校验码生成工具类（通用）
 * <p>
 * 职责：
 * 1. 为任意字符串添加校验码（防止篡改/输入错误）
 * 2. 验证字符串和校验码的合法性
 * 3. 从带校验码的字符串中提取原始内容
 * <p>
 * 格式：{原始字符串}{校验码}
 * 示例：ORDER1234567 （原始字符串 + 校验码，最后一位是数字0-9）
 *       17445888000001234563 （数字字符串 + 校验码，最后一位是数字0-9）
 * <p>
 * 校验码算法：Luhn算法变种（加权模10算法）
 * - 对字符串中每个字符的ASCII码进行加权求和
 * - 权重从右到左依次为 1, 2, 1, 2, ...
 * - 如果某位乘以权重后 >= 10，则减去9
 * - 最后取模10得到校验码（0-9的数字字符）
 */
@Slf4j
public abstract class CheckDigitUtil {

    /**
     * 为long类型数字添加校验码
     * <p>
     * 格式：{数字字符串}{校验码(1位)}
     *
     * @param number 原始数字
     * @return 带校验码的字符串
     */
    public static String addCheckDigit(long number) {
        return addCheckDigit(String.valueOf(number));
    }

    /**
     * 为字符串添加校验码
     * <p>
     * 格式：{原始字符串}{校验码(1位)}
     *
     * @param str 原始字符串（可以是任意字符串）
     * @return 带校验码的字符串
     * @throws IllegalArgumentException 如果字符串为空或null
     */
    public static String addCheckDigit(String str) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException("输入字符串不能为空");
        }
        return str + calculateCheckDigit(str);
    }

    /**
     * 计算校验码（Luhn算法变种）
     * <p>
     * 使用字符的ASCII码值进行计算，支持任意字符串
     *
     * @param str 原始字符串
     * @return 校验码字符 ('0'-'9')
     */
    private static char calculateCheckDigit(String str) {
        int sum = 0;
        boolean alternate = false;
        
        // 从右到左遍历每一位字符
        for (int i = str.length() - 1; i >= 0; i--) {
            int charValue = (int) str.charAt(i);
            
            if (alternate) {
                charValue *= 2;
                if (charValue > 9) {
                    charValue -= 9;
                }
            }
            
            sum += charValue;
            alternate = !alternate;
        }
        
        int checkDigit = (10 - (sum % 10)) % 10;
        return (char) ('0' + checkDigit);
    }

    /**
     * 验证带校验码的字符串是否合法
     *
     * @param strWithCheckDigit 带校验码的字符串（最后一位是校验码）
     * @return true-合法，false-非法
     */
    public static boolean validate(String strWithCheckDigit) {
        if (strWithCheckDigit == null || strWithCheckDigit.length() < 2) {
            log.warn("字符串长度不正确: {}", strWithCheckDigit);
            return false;
        }

        // 提取原始部分和校验码部分
        String originalPart = strWithCheckDigit.substring(0, strWithCheckDigit.length() - 1);
        char providedCheckDigit = strWithCheckDigit.charAt(strWithCheckDigit.length() - 1);

        // 验证校验码是否正确
        char calculatedCheckDigit = calculateCheckDigit(originalPart);
        
        if (providedCheckDigit != calculatedCheckDigit) {
            log.warn("校验码不匹配，期望: {}, 实际: {}, 字符串: {}", 
                    calculatedCheckDigit, providedCheckDigit, strWithCheckDigit);
            return false;
        }

        return true;
    }

    /**
     * 从带校验码的字符串中提取原始内容（去除校验码）
     *
     * @param strWithCheckDigit 带校验码的字符串（最后一位是校验码）
     * @return 原始字符串（不含校验码）
     * @throws IllegalArgumentException 如果字符串不合法或未通过校验
     */
    public static String removeCheckDigit(String strWithCheckDigit) {
        if (!validate(strWithCheckDigit)) {
            throw new IllegalArgumentException("字符串校验失败: " + strWithCheckDigit);
        }
        
        return strWithCheckDigit.substring(0, strWithCheckDigit.length() - 1);
    }


}
