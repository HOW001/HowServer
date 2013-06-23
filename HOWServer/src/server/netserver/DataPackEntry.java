package server.netserver;

import org.apache.mina.transport.socket.nio.NioSession;

/**
 * @author lzg------2011-2-16 数据命令封装
 */
public class DataPackEntry {
	
	/**
	 * 客户端连接类型
	 */
	public static final byte CONN_TYPE_SOCKET = 1;

	public static final byte CONN_TYPE_HTTP = 2;
	
	
	private NioSession session;
	private byte[] data;
	/**
     * 连接类型，分为Socket和Http两种
     */
    public byte   connectionType;
    
    public int command;
	public DataPackEntry(NioSession session,byte connType,int cmd, byte[] data) {
		this.session = session;
		this.connectionType=connType;
		this.command=cmd;
		this.data = data;
	}

	public NioSession getSession() {
		return this.session;
	}

	public byte[] getData() {
		return data;
	}
    
	public byte getConnType(){
		return connectionType;
	}
}
