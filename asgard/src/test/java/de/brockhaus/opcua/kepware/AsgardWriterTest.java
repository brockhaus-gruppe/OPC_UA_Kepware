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

public class AsgardWriterTest 
{
	@Test
	public void test() throws UnknownHostException, URISyntaxException, SecureIdentityException, IOException, InvalidServerEndpointException, ConnectException, SessionActivationException, ServiceException, StatusException, ServiceResultException, InterruptedException, AddressSpaceException 
	{
		Asgard aw = new Asgard();
		aw.init();
		aw.connect();
		aw.doWrite();
		aw.disconnect();
	}
}