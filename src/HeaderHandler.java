import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class HeaderHandler implements SOAPHandler<SOAPMessageContext> {

	private boolean handleMessageClearText(SOAPMessageContext smc) {
		Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outboundProperty.booleanValue()) {
			try {
				SOAPEnvelope envelope = smc.getMessage().getSOAPPart().getEnvelope();
				SOAPHeader header = envelope.getHeader();
				SOAPElement security = header.addChildElement("Security", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
				SOAPElement usernameToken = security.addChildElement("UsernameToken", "wsse");
				usernameToken.addAttribute(new QName("xmlns:wsu"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
				SOAPElement username = usernameToken.addChildElement("Username", "wsse");
				username.addTextNode(user);
				SOAPElement password = usernameToken.addChildElement("Password", "wsse");
				password.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
				password.addTextNode(passwd);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return outboundProperty;
	}

	private boolean handleMessageDigest(SOAPMessageContext smc) {
		Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
		if (outboundProperty.booleanValue()) {
			try {
				SecureRandom rnd = SecureRandom.getInstance("SHA1PRNG");
				byte[] bNonce = new byte[16];
				rnd.nextBytes(bNonce);
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				df.setTimeZone(TimeZone.getTimeZone("UTC"));
				String now = df.format(Calendar.getInstance().getTime());
				byte[] bNow = now.getBytes("UTF-8");
				byte[] bPasswd = passwd.getBytes("UTF-8");
				ByteArrayOutputStream buff = new ByteArrayOutputStream();
				buff.write(bNonce);
				buff.write(bNow);
				buff.write(bPasswd);
				MessageDigest md = MessageDigest.getInstance("SHA-1");
				byte[] bDigestedPasswd = md.digest(buff.toByteArray());
				byte[] bPasswdBase64 = Base64.getMimeEncoder().encode(bDigestedPasswd);
				byte[] bNonceBase64 = Base64.getMimeEncoder().encode(bNonce);
				SOAPEnvelope envelope = smc.getMessage().getSOAPPart().getEnvelope();
				SOAPHeader header = envelope.getHeader();
				SOAPElement security = header.addChildElement("Security", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
				SOAPElement usernameToken = security.addChildElement("UsernameToken", "wsse");
				SOAPElement username = usernameToken.addChildElement("Username", "wsse");
				username.addTextNode(user);
				SOAPElement password = usernameToken.addChildElement("Password", "wsse");
				password.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest");
				password.addTextNode(new String(bPasswdBase64));
				SOAPElement nonce = usernameToken.addChildElement("Nonce", "wsse");
				nonce.addTextNode(new String(bNonceBase64));
				SOAPElement created = usernameToken.addChildElement("Created", "wsu", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
				created.addTextNode(now);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return outboundProperty;
	}

	public boolean handleMessage(SOAPMessageContext smc) {
		// return handleMessageClearText(smc);
		return handleMessageDigest(smc);
	}

	public Set getHeaders() {
		return null;
	}

	public boolean handleFault(SOAPMessageContext context) {
		return true;
	}

	public void close(MessageContext context) {
	}

	private String user = null;
	private String passwd = null;

	public void setUser(String user) {
		this.user = user;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

}