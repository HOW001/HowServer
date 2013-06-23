package world.object;

public class GridState {
	public static final byte GRID_STATE_OK=0;//开放状态
	public static final byte GRID_STATE_NOT_PASS=0;//格子未通过
	public static final byte GRID_STATE_PASS_RATE_MIX=1;//完成度不够
	
	
	private int state=0;//完成状态
	private String desc="";//状态描述
	private int gridId=0;//格子id
	
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public int getGridId() {
		return gridId;
	}
	public void setGridId(int gridId) {
		this.gridId = gridId;
	}
	@Override
	public String toString() {
		return "GridState [state=" + state + ", desc=" + desc + ", gridId=" + gridId + "]";
	}
	
	
}
