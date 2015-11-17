package de.brockhaus.opcua.kepware;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import org.junit.Test;
import org.opcfoundation.ua.common.ServiceResultException;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.SessionActivationException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.ConnectException;
import com.prosysopc.ua.client.InvalidServerEndpointException;
import de.brockhaus.opcua.kepware.Asgard;

/**
 * 
 * @author jperez@brockhaus-gruppe.de, Nov 16, 2015
 * Copyright by: Brockhaus Group (Wiesbaden, Heidelberg, Bhubaneswar/India)
 *
 */

public class AsgardReaderTest 
{
	public static void main(String[] args) throws InvalidServerEndpointException, UnknownHostException, ConnectException, SessionActivationException, URISyntaxException, SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException, InterruptedException 
	{
		// instantiates the AsgardReaderTest class
		AsgardReaderTest art = new AsgardReaderTest();
		// runs the test method
		art.test();
	}
	
	// use appropriate annotation to the junit test
	@Test
	public void test() throws UnknownHostException, URISyntaxException, SecureIdentityException, IOException, InvalidServerEndpointException, ConnectException, SessionActivationException, ServiceException, StatusException, ServiceResultException, InterruptedException 
	{
		// instantiates the Asgard class
		Asgard ar = new Asgard();
		// creates all the connection initial configuration needed to connect to the server
		ar.init();
		// establishes the connection to the server
		ar.connect();
		// reads every second (1000 ms) the selected tag value
		ar.doRead();
		// Allows to disconnect from the server.
		ar.disconnect();
	}
}