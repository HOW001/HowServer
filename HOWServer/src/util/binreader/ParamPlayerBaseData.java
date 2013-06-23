/**
 * 
 */
package util.binreader;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liuzg
 *
 * 玩家基准数据
 */
public class ParamPlayerBaseData implements PropertyReader {
    public int id;
    public double shield;
    public double power;
    public double speed;
    public double handle_use;
    public double attack;
    public double reaction;
    public double mark;
    public long max_exp;

	private static Map<Integer,ParamPlayerBaseData> datas=new HashMap<Integer,ParamPlayerBaseData>();
	/* (non-Javadoc)
	 * @see util.binreader.PropertyReader#addData()
	 */
	@Override
	public void addData(boolean isReLoad) {
		if(isReLoad==false){
			datas.put(id, this);
		}
	}
	@Override
	public void clearData() {
		
	}
	@Override
	public void clearStaticData() {
		
	}
    public static ParamPlayerBaseData getDataForLevel(int level){
    	return datas.get(level);
    }
	/* (non-Javadoc)
	 * @see util.binreader.PropertyReader#getData(int)
	 */
	@Override
	public PropertyReader getData(int id) {
		// TODO Auto-generated method stub
		return datas.get(id);
	}

}
