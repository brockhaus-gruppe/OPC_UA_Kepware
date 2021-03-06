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

public class AsgardReaderTest{
	public static void main(String[] args) throws InvalidServerEndpointException, 
		UnknownHostException, ConnectException, SessionActivationException, 
		URISyntaxException, SecureIdentityException, IOException, ServiceException, 
		StatusException, ServiceResultException, InterruptedException{
		
		AsgardReaderTest art = new AsgardReaderTest();
		art.test();
	}
	
	// use appropriate annotation to the junit test
	@Test
	public void test() throws UnknownHostException, URISyntaxException, 
		SecureIdentityException, IOException, InvalidServerEndpointException, 
		ConnectException, SessionActivationException, ServiceException, StatusException, 
		ServiceResultException, InterruptedException{
		
		Asgard ar = new Asgard();
		/* create all the connection initial configuration needed to connect 
	    to the server */
		ar.init();
		// establish the connection to the server
		ar.connect();
		// read every second (1000 ms) the selected tags value
		ar.doRead();
		// Allow to disconnect from the server.
		ar.disconnect();
	}
}