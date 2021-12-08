import java.util.List;

import com.vinlink.ws.*;

public class Client {

	public static void main(String[] args) {
		
		// create account at www.vinlink.com
		String LOGIN = "login";
		String PASSWD = "password";

		try {
			Decoder service = new Decoder();
			HeaderHandlerResolver handlerResolver = new HeaderHandlerResolver();
			handlerResolver.setUser(LOGIN);
			handlerResolver.setPasswd(PASSWD);
			service.setHandlerResolver(handlerResolver);
			DecoderPortType port = service.getDecoderHttpPort();

			for (String vin: args) {
				ArrayOfReport reports = port.decode(vin, ReportTypes.BASIC_PLUS);
				List<Report> reprtList = reports.getReport();
				for (Report r : reprtList) {
					List<Item> items = r.getVinSpecification().getValue().getItem();
					for (Item i : items) {
						String n = i.getName().getValue();
						String v = i.getValue().getValue();
						System.out.printf("%s\t%s\n", n, v);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
