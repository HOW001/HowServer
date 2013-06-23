package server.cmds;
import org.apache.mina.transport.socket.nio.NioSession;
public class Command {
	protected NioSession session;
	protected int command;
	protected byte[] data;
	protected byte connType;
	public Command(NioSession session,int command,byte connType,byte[] data){
		this.session = session;
		this.command = command;
		this.data = data;
		this.connType=connType;
	}
	
	public String toString(){
		return Integer.toHexString(command);
	}
}
