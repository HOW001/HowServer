/**
 * 赛道章节信息
 */
package util.binreader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuzg
 *
 */
public class ChapterInfoData implements PropertyReader {
    public static final int LOCKTYPEGATE=1;//关卡完成
    public static final int LOCKTYPETASK=2;//任务完成
    public static final int LOCKTYPEITEM=3;//道具使用

    public int id;
    public String title;
    public int type;
    public int lock_type;//1:关卡完成2:任务完成3:道具使用
    public String unlock_condition;//0为无条件，完成指定id比赛后可开启，支持填写多个&
    public int min_level;
    public int max_level;
    public String  desc;

    private static Map<Integer,ChapterInfoData> datas=new HashMap<Integer,ChapterInfoData>();
    
    //比赛章节列表
    private static Map<Integer,List<Integer>> chapterList=new HashMap<Integer,List<Integer>>();
    
    public List<Integer> conditionList=new ArrayList<Integer>();
	/* (non-Javadoc)
	 * @see util.binreader.PropertyReader#addData()
	 */
	@Override
	public void addData(boolean isReLoad) {
		if(isReLoad==false){
			datas.put(id,this);
		}
		for(String gateID:unlock_condition.split("&")){
			int gate=Integer.parseInt(gateID);
			conditionList.add(gate);
		}
        List<Integer> temp=chapterList.get(type);
        if(temp==null){
        	temp=new ArrayList<Integer>();
        }
        if(temp.contains(id)==false){
        	temp.add(id);
        }
        chapterList.put(type,temp);
	}
	@Override
	public void clearData() {
		conditionList.clear();
	}
	@Override
	public void clearStaticData() {
		chapterList.clear();
	}
	/**
	 * @author liuzg
	 * @param id
	 * @return
	 * 
	 */
	public static ChapterInfoData getChapterInfo(int id){
		return datas.get(id);
	}
	/**
	 * @author liuzg
	 * @param type
	 * @return
	 * 获取指定比赛的章节列表
	 */
    public static List<Integer> getChapterList(int type){
    	if(chapterList.get(type)==null){
    		return new ArrayList<Integer>();
    	}
    	return chapterList.get(type);
    }
	/* (non-Javadoc)
	 * @see util.binreader.PropertyReader#getData(int)
	 */
	@Override
	public PropertyReader getData(int id) {
		return datas.get(id);
	}
}
