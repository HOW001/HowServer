package util;

/**
 * Description:基础数据装换工具<br>
 * 将short/int装换为byte[]时，采用“big-endian”模式，即“高位在前，低位在后”的方式<br>
 * 例如：<br>
 * int i = 0x12345678;<br>
 * b[0] = 0x12; b[1] = 0x34; b[2] = 0x56; b[3] = 0x78;<br>
 * 注:目前的版本不提供“little-endian”模式
 * 
 * @author Insunny
 * @version 0.1
 */
public class BaseDataConvertor
{
    /**
     * 将int值转换成为byte数组，采用“big-endian”模式
     * 
     * @param _input
     * @return 将byte[]作为结果返回
     */
    public static byte[] int2Bytes (int _input)
    {
        byte[] bytes = new byte[4];
        int2Bytes(_input, bytes, 0);
        return bytes;
    }

    /**
     * 将int值转换成为byte数组，结果存放在指定的output数组中
     * 
     * @param _input 待转换的int值
     * @param _output 结果存放目标数组
     * @param _offset 结果存放的起始位置
     */
    public static void int2Bytes (int _input, byte[] _output, int _offset)
    {
        if (_output == null)
            throw new NullPointerException("output array is null");
        if (_offset + 4 > _output.length)
            throw new IndexOutOfBoundsException(
                    "offset or size of output array is not correct");
        _output[_offset] = (byte) ((_input & 0xff000000) >> 24);
        _output[_offset + 1] = (byte) ((_input & 0xff0000) >> 16);
        _output[_offset + 2] = (byte) ((_input & 0xff00) >> 8);
        _output[_offset + 3] = (byte) (_input & 0xff);
    }

    /**
     * 将byte数组转换为int值
     * 
     * @param _input 输入的byte[]
     * @param _offset 起始位置
     * @return
     */
    public static int bytes2Int (byte[] _input, int _offset)
    {
        if (_input == null)
            throw new NullPointerException("output array is null");
        if (_offset + 4 > _input.length)
            throw new IndexOutOfBoundsException(
                    "offset or size of input array is not correct");
        return ((_input[_offset] & 0xff) << 24)
                | ((_input[_offset + 1] & 0xff) << 16)
                | ((_input[_offset + 2] & 0xff) << 8)
                | (_input[_offset + 3] & 0xff);
    }

    /**
     * 将short值转换为byte[]
     * 
     * @param _input 待转换的short值
     * @return 转换结果
     */
    public static byte[] short2Bytes (short _input)
    {
        byte[] bytes = new byte[2];
        short2Bytes(_input, bytes, 0);
        return bytes;
    }

    /**
     * 将short值转换为byte[]
     * 
     * @param _input 待转换的short值
     * @param _output 存放结果的byte数组
     * @param _offset 存放结果的起始位置
     */
    public static void short2Bytes (short _input, byte[] _output, int _offset)
    {
        if (_output == null)
            throw new NullPointerException("output array is null");
        if (_offset + 2 > _output.length)
            throw new IndexOutOfBoundsException(
                    "offset or size of output array is not correct");
        _output[_offset] = (byte) ((_input & 0xff00) >> 8);
        _output[_offset + 1] = (byte) (_input & 0xff);
    }

    /**
     * 将byte数组转化为short值
     * 
     * @param _input
     * @param _offset
     * @return
     */
    public static short bytes2Short (byte[] _input, int _offset)
    {
        if (_input == null)
            throw new NullPointerException("output array is null");
        if (_offset + 2 > _input.length)
            throw new IndexOutOfBoundsException(
                    "offset or size of input array is not correct");
        return (short) (((_input[_offset] & 0xff) << 8) | (_input[_offset + 1] & 0xff));
    }

    public static String bytes2String (byte _input[], int len)
    {
        int n = (len + 1) / 2;
        if (n == 0)
            return "";
        char chs[] = new char[n];
        for (int i = 0; i < n; i++)
            chs[i] = (char) (_input[2 * i] << 8 | _input[2 * i + 1] & 0xff);

        return new String(chs);
    }

}
