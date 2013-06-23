package util;

import java.util.Random;

public class EncryptUtil {
	private static final byte OPERATOR_PLUS 		= 'L';
	private static final byte OPERATOR_XOR		= 'Z';
	private static final byte OPERATOR_MINUS 	= 'G';
	private static final byte OPERATOR_OR		= '&';
	private static final int LEN_A = 10;//最低长度
	private static final int LEN_B = 11;//随机长度
	
	private static Random random = new Random();
	
	private static int random(int seed){
		return random.nextInt(seed);
	}

	private static int random(){
		return random.nextInt();
	}
	
	private static byte[] randomBytes(int len){
		byte[] data = new byte[len];
		random.nextBytes(data);
		return data;
	}
	private static final int DICTIONARY_LENGTH = 400;
	private static byte[][] libs = new byte[DICTIONARY_LENGTH][];
	static{
		libs[0] = new byte[0];
		for(int i=1;i<DICTIONARY_LENGTH;i++){
			libs[i] = generateEncrypt();
		}
	}
	/**
	 * @author lzg
	 * @return
	 * 产生加密码字典,每次最多产生21个长度的加密码，循环使用每一个加密码
	 */
	private static byte[] generateEncrypt(){
		int len = LEN_A + random(LEN_B);
		if(len > 128){
			return new byte[0];
		}
		byte[] data = randomBytes(len << 1);
		for(int i=0;i<len;i++){
			data[i<<1] = randomOperator();
		}

		return data;
	}
	/**
	 * @author lzg 2010-5-11
	 * @return
	 * 加密元码
	 */
	private static byte randomOperator() {
		switch(random() % 4){
		case 0:
			return OPERATOR_PLUS;//L
		case 1:
			return OPERATOR_XOR;//Z
		case 2:
			return OPERATOR_OR;//G
		case 3:
			return OPERATOR_MINUS;//&
		}
		return OPERATOR_XOR;
	}

	/**
	 * @author lzg
	 * @param src 明文
	 * @param key 密钥
	 * @param sid 流水号
	 * @return 密文
	 * 加密
	 * 加密原理：
	 * 1.根据流水号和密钥长度获取加密起始位置start
	 * 2.从起始位置取密钥
	 * 3.取出对应位置的密钥
	 * 4.将明文与对应密钥的下一个密钥进行加密产生密文
	 */
	public static byte[] encrypt(byte[] src,byte[] key,int sid){
		if(src == null) return src;
		if(key == null) return src;
		if(key.length == 0) return src;
		byte[] data = new byte[src.length];
		int start = getStart(sid,key.length);//(((byte)sid+256) % (length >> 1)) << 1
		for(int i=0,j=start;i<src.length;i++,j+=2){
			if(j >= key.length) j=0;
			switch(key[j]){
			case OPERATOR_PLUS:
				data[i] = (byte)(src[i] + key[j+1]);
				break;
			case OPERATOR_MINUS:
				data[i] = (byte)(src[i] - key[j+1]);
				break;
			case OPERATOR_OR:
				data[i] = (byte)(~src[i] & 0xFF);
				break;
			case OPERATOR_XOR:
				default:
				data[i] = (byte)(src[i] ^ key[j+1]);
				break;
			}
		}
		return data;
	}
	/**
	 * @author lzg
	 * @param sid 流水号
	 * @param length 密钥长度
	 * @return
	 */
	private static int getStart(int sid,int length){
		return (((byte)sid+256) % (length >> 1)) << 1;
	}
	/**
	 * @author lzg
	 * @param src 密文
	 * @param key 密钥
	 * @param sid 流水号
	 * @return 明文
	 * 解密
	 * 解密原理：
	 * 1.根据流水号和密钥长度产生起始位置start
	 * 2.根据起始位置取得相应密钥
	 * 3.根据密钥类型得到解密算法
	 * 4.将相应位置的下一个密钥与密文进行解密
	 */
	public static byte[] decrypt(byte[] src,byte[] key,int sid){
		if(src == null) return src;
		if(key == null) return src;
		if(key.length == 0) return src;
		byte[] data = new byte[src.length];
		int start = getStart(sid,key.length);
		for(int i=0,j=start;i<src.length;i++,j+=2){
			if(j >= key.length) j=0;
			switch(key[j]){
			case OPERATOR_PLUS:
				data[i] = (byte)(src[i] - key[j+1]);
				break;
			case OPERATOR_MINUS:
				data[i] = (byte)(src[i]+ key[j+1]);
				break;
			case OPERATOR_OR:
				data[i] = (byte)(~src[i] & 0xFF);
				break;
			case OPERATOR_XOR:
				default:
				data[i] = (byte)(src[i] ^ key[j+1]);
				break;
			}
		}
		return data;
	}
	
	private static int getRandomEncryptID(){
		return random.nextInt(DICTIONARY_LENGTH);
	}
	/**
	 * @author liuzg
	 * @return
	 * 获取密钥索引
	 */
	public static int changeEncryptIndex(){
		int index = getRandomEncryptID();
		return index;
	}
	/**
	 * @author liuzg
	 * @param index
	 * @return
	 * 根据密钥索引获取密钥
	 */
    public static byte[] getEncryptKey(int index){
    	if(index <= -1 || index > DICTIONARY_LENGTH){
			index=0;
		}
    	return libs[index];
    }

	public static void main(String[] args){
		String src="由Client发起连接请求，由Server和Client共同维护连接的存在，两者均具备被动接收和主动发送的条件。 ";
		int sid=new Random().nextInt(255);
		int index=changeEncryptIndex();
		byte[] keys=getEncryptKey(index);
		System.out.println("加密前数据:"+src);
		byte[] jiami=encrypt(src.getBytes(),keys,sid);
		System.out.println("加密后数据:"+new String(jiami));
		byte[] jiemi=decrypt(jiami, keys, sid);
		System.out.println("解密后数据:"+new String(jiemi));
	}

}
