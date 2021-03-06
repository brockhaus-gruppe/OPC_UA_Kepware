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
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.client.ConnectException;
import com.prosysopc.ua.client.InvalidServerEndpointException;
import de.brockhaus.opcua.kepware.Asgard;

/**
 * 
 * @author jperez@brockhaus-gruppe.de, Nov 16, 2015
 * Copyright by: Brockhaus Group (Wiesbaden, Heidelberg, Bhubaneswar/India)
 *
 */

public class AsgardWriterTest{
	
	public static void main(String[] args) throws InvalidServerEndpointException, 
		UnknownHostException, ConnectException, SessionActivationException, 
		URISyntaxException, SecureIdentityException, IOException, ServiceException, 
		StatusException, ServiceResultException, InterruptedException, 
		AddressSpaceException{
		
		AsgardWriterTest awt = new AsgardWriterTest();
		awt.test();
	}
	
	// use appropriate annotation to the junit test
	@Test
	public void test() throws UnknownHostException, URISyntaxException, 
		SecureIdentityException, IOException, InvalidServerEndpointException, 
		ConnectException, SessionActivationException, ServiceException, StatusException, 
		ServiceResultException, InterruptedException, AddressSpaceException{
		
		Asgard aw = new Asgard();
		/* create all the connection initial configuration needed to connect 
	    to the server */
		aw.init();
		// establish the connection to the server
		aw.connect();
		// write a constant value for the selected tags.
		aw.doWrite();
		// Allow to disconnect from the server.
		aw.disconnect();
	}
}