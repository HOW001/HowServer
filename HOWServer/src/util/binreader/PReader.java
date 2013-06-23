package util.binreader;

import org.apache.log4j.Logger;


/**
 * 读策划给的数据文件
 * @author Administrator
 *
 */
public class PReader extends BinReader{	
	private static Logger logger=Logger.getLogger(PReader.class);
	private PReader(){
	}
	private static PReader reader;
	public static PReader getInstance() {
		if(reader == null){
			reader = new PReader();
		}
		return reader;
	}

	public void init() {
		readAllData();
	}
	public void reLoad(){
		logger.info("********************重新加载一次静态数据****************************");
		readAllData();
	}
}
