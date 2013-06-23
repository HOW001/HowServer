package util;
import javax.crypto.Cipher;
import java.security.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Dec 4, 2003
 * Time: 9:29:19 PM
 * To change this template use Options | File Templates.

 *@author xiaoyusong@etang.com
 * 该类是加密处理类.主要用来对数据进行加密,解密操作.
 */
public class DESEncryptUtil {
    /**
     * 将指定的数据根据提供的密钥进行加密
     * @param key  密钥
     * @param data 需要加密的数据
     * @return  byte[] 加密后的数据
     * @throws util.EncryptException
     */
    public static byte[] doEncrypt(Key key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] raw = cipher.doFinal(data);
            return raw;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将给定的已加密的数据通过指定的密钥进行解密
     * @param key  密钥
     * @param raw  待解密的数据
     * @return   byte[] 解密后的数据
     * @throws util.EncryptException
     */
    public static byte[] doDecrypt(Key key, byte[] raw)
{
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] data = cipher.doFinal(raw);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 得到一个密钥的密码
     * @param key 密钥
     * @param cipherMode 密码的类型
     * @return  Cipher
     * @throws util.EncryptException 当加密出现异常情况时,产生异常信息
     */
    public static Cipher getCipher(Key key, int cipherMode) {
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(cipherMode, key);
            return cipher;
        } catch (Exception e) {
            e.printStackTrace();
//            throw new EncryptException("Generate Cipher occurs Exception.[" +e.getMessage() + "]");
        }
        return null;
    }
}
