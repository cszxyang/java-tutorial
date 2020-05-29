package com.cszxyang.jt.spi.util;

public class MathUtil {
    /**
     * 通过二进制位操作将originValue转化为非负数:
     *      0和正数返回本身
     *      负数通过二进制首位取反转化为正数或0（Integer.MIN_VALUE将转换为0）
     * return non-negative int value of originValue
     * @param originValue originValue
     * @return positive int
     */
    public static int getNonNegative(int originValue){
        return 0x7fffffff & originValue;
    }
}
