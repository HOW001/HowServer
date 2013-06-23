package util;

public class MathUtils {
	/**
	 * 获取一个[0,seed)之间随机数
	 * @param seed
	 * @return
	 */
	public static int random(int seed){
		return Random.getNumber(seed);
	}
	
	/**
	 * 最多18次幂(long 型有符号最多19位)
	 * @return
	 */
	public static long getTenPower(int m){
		switch(m){
		case 0:
			return 1;
		case 1:
			return 10;
		case 2:
			return 100;
		case 3:
			return 1000;
		case 4:
			return 10000;
		case 5:
			return 100000;
		case 6:
			return 1000000;
		case 7:
			return 10000000;
		case 8:
			return 100000000;
		case 9:
			return 1000000000;
		case 10:
			return 10000000000l;
		case 11:
			return 100000000000l;
		case 12:
			return 1000000000000l;
		case 13:
			return 10000000000000l;
		case 14:
			return 100000000000000l;
		case 15:
			return 1000000000000000l;
		case 16:
			return 10000000000000000l;
		case 17:
			return 100000000000000000l;
		case 18:
			return 1000000000000000000l;
		default: return 0;
		
		}
	}
	
	/**
	 * 获取[start,end)之间的随机数
	 * @param start
	 * @param end
	 * @return
	 */
	public static int random(int start,int end){
		if(start == end){
			return start;
		}
		if(end > start){
			return Random.getNumber(start, end);
		} else {
			return Random.getNumber(end,start);
		}
	}

	/**
	 * long 型设置特殊位数值(二进制)
	 * @param insertl
	 * @param orgl
	 * @param begainBits右边开始第一位是1
	 * @param endBits
	 * @return
	 */
	public static long setPartValueX2(long insertl,long orgl,int begainBits,int endBits){
		insertl = insertl << (64-(endBits-begainBits+1));
		insertl = insertl >>>(64-endBits);
		long lb;
		if (begainBits!=1){
			lb = orgl <<(64-begainBits+1);
			lb = lb >>>(64-begainBits+1);
			
		}else{
			lb = 0;
		}
		long le;
		if (endBits!=64){
			le= orgl >>> endBits;
			le= le << endBits;
		}else{
			le = 0;
		}
		return (le + insertl + lb);
	}
	/**
	 * long  型取特殊位数值(二进制)
	 * @param org
	 * @param bigainBits右边开始第一位是1
	 * @param endBits
	 * @return
	 */
	public static long getPartValueX2(long org,int bigainBits,int endBits){
		org = org<<(64-endBits);
		org = org>>>(64-endBits);
		org = org>>>bigainBits-1;
		return org;
	}
	
	/**
	 *  long  型取特殊位数值(十进制)
	 * @param insertl
	 * @param orgl
	 * @param begainBits
	 * @param endBits
	 * @return
	 */
	public static long setPartValueX10(long insertl,long orgl,int begainBits,int endBits){
		long b = MathUtils.getTenPower(begainBits-1);
		long e = MathUtils.getTenPower(endBits);
		long c = MathUtils.getTenPower(endBits - begainBits+1);
		long l1,l2,l3;
		l3 = orgl % b;
		l1 = orgl / e;
		l2 = insertl % c;
		return l1*e + l2*b + l3;
	}
	/**
	 * long  型取特殊位数值(十进制)
	 * @param org
	 * @param bigainBits右边开始第一位是1
	 * @param endBits
	 * @return
	 */
	public static int getPartValueX10(long l,int begainBits,int endBits){
		long b = MathUtils.getTenPower(begainBits-1);
		long e = MathUtils.getTenPower(endBits);
		return (int)(l%e/b);
	}
	
	public static int sqrt(long x) {
		if(x <= 0) return 0;
		if(x <= 1) return 1;
		return (int)Math.sqrt(x);
	}
	
	public static byte[] randomBytes(int length){
		return Random.getBytes(length);
	}

	/**
	 * BitSet Logic
	 * @param number
	 * @param index
	 * @param b
	 * @return
	 */
	public static int setBoolean(int number,int index,boolean b){
		if(index < 0 || index >= 32){
			return number;
		}
		if(b){
			return number | (1 << index);
		} else {
			return number & (~(1 << index));
		}
	}
	
	/**
	 * BitSet Logic
	 * @param number
	 * @param index
	 * @return
	 */
	public static boolean getBoolean(int number,int index){
		if(index < 0 || index >= 32){
			return false;
		}
		return (number & (1 << index)) == (1 << index);
	}
	
	public static void main(String[] args){
		setPartValueX2(1,1125899906842624l,23,31);
	}
}
