package util;

//import util.logger.SysLogger;


public class Random{ 
	public static final int RANDOM_SEED = 100000000;//随机数的种子
	
	private static java.util.Random ran = new java.util.Random();
	
	private Random(){
	}
	private static Random instance; 
	
	protected static Random getInstance(){
		if(instance == null){
			instance = new Random();
		}
		return instance;
	}

	protected static byte[] getBytes(int length){
		byte[] r = new byte[length];
		ran.nextBytes(r);
		return r;
	}
	protected static int getNumber(){
		return ran.nextInt(RANDOM_SEED);
	}
	
	protected static int getNumber(int seed){
		if(seed == 0){
			return 0;
		}
		return getNumber() % seed;
	}
	
	public static int getNumber(int start,int end){
		return start + getNumber(end - start);
	}
	
	public static void main(String[] args){
		for(int i=0;i<1000;i++){
			System.out.println(getNumber(100));
		}
	}

}
