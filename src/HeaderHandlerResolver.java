import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

public class HeaderHandlerResolver implements HandlerResolver {

	public List<Handler> getHandlerChain(PortInfo portInfo) {
		List<Handler> handlerChain = new ArrayList<Handler>();
		HeaderHandler hh = new HeaderHandler();
		hh.setUser(user);
		hh.setPasswd(passwd);
		handlerChain.add(hh);
		return handlerChain;
	}

	private String user;
	private String passwd;

	public void setUser(String user) {
		this.user = user;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

}