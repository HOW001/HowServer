package util.binreader;

public interface PropertyReader {
	/**
	 * 
	 * @param isReLoad 是否重新加载，第二次加载数据时无须put到集合，但需要进行具体的相关处理
	 */
	void addData(boolean isReLoad);
	PropertyReader getData(int id);
	/**
	 * 在加载数据之前，先清空一次映射当中的相关集合
	 */
	void clearData();
	/**
	 * @author liuzg
	 * 在加载数据之前，清空一次静态数据
	 */
	void clearStaticData();
}
