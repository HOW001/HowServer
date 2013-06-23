package db.model;

public class Test implements DataBaseEntry {
    private int id;
    private int version;
    private int a;
    private String b;
	@Override
	public void initDBEntry(Player p) {
		// TODO Auto-generated method stub

	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public int getA() {
		return a;
	}
	public void setA(int a) {
		this.a = a;
	}
	public String getB() {
		return b;
	}
	public void setB(String b) {
		this.b = b;
	}

}
