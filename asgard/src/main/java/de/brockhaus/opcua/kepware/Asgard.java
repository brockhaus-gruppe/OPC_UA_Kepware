package de.brockhaus.opcua.kepware;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.UnsignedShort;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.ApplicationType;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.EUInformation;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.MonitoringMode;
import org.opcfoundation.ua.core.NodeClass;
import org.opcfoundation.ua.core.Range;
import org.opcfoundation.ua.core.ReferenceDescription;
import org.opcfoundation.ua.transport.security.SecurityMode;
import org.opcfoundation.ua.utils.AttributesUtil;
import org.opcfoundation.ua.utils.MultiDimensionArrayUtils;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.SessionActivationException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.client.ConnectException;
import com.prosysopc.ua.client.InvalidServerEndpointException;
import com.prosysopc.ua.client.MonitoredDataItem;
import com.prosysopc.ua.client.MonitoredDataItemListener;
import com.prosysopc.ua.client.MonitoredItem;
import com.prosysopc.ua.client.ServerConnectionException;
import com.prosysopc.ua.client.Subscription;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.nodes.UaDataType;
import com.prosysopc.ua.nodes.UaInstance;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaReferenceType;
import com.prosysopc.ua.nodes.UaType;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.types.opcua.AnalogItemType;

/**
 * 
 * @author jperez@brockhaus-gruppe.de, Nov 16, 2015
 * Copyright by: Brockhaus Group (Wiesbaden, Heidelberg, Bhubaneswar/India)
 *
 */

public class Asgard {
	
	// create an UaClient object which encapsulates the connection to the OPC UA server
	static UaClient client;
	
	// define the server you are connecting to
	static final String SERVERURI = "opc.tcp://127.0.0.1:49320";
	
	// define a target node Id for the selected node
	NodeId target;
	
	// create an array of tags which can be read or written
	ArrayList<NodeId> TagsArray = new ArrayList<NodeId>();
	
	// define a list of references(children or hierarchical relationships) 
	// for each selected node
	List<ReferenceDescription> references;
	
	/* this variable selects the possible data that can be obtained from the node.
	In this case, the parameter "value" of the node (integer number 13 in the 
	node attributes list).*/
	UnsignedInteger attributeId = UnsignedInteger.valueOf(13);
	
	// allow to monitor variables that are changing in the server
	Subscription subscription = new Subscription();
	
	Logger log = Logger.getLogger(this.getClass().getName());

	public Asgard() throws FileNotFoundException, IOException {
		// define logging behaviour establishing properties by a configuration file
		Properties props = new Properties();
		props.load(new FileInputStream("src/main/resources/log4j.properties"));
		PropertyConfigurator.configure(props);
	}

	public void init() throws URISyntaxException, UnknownHostException, SecureIdentityException, IOException {
		// initiate the connection to the server
		client = new UaClient(SERVERURI);
		
		// define the security level in the OPC UA binary communications
		client.setSecurityMode(SecurityMode.NONE);
		
		// create an application instance certificate
		initialize(client);
		
		log.info("[-- STARTING INITIAL SETUP --]");
	}

	public void connect()
			throws InvalidServerEndpointException, ConnectException, SessionActivationException, ServiceException {
		// connect to the server
		client.connect();
		log.info("[-- CONNECTED TO THE SERVER --]");
	}

	public void doRead() throws ServiceException, StatusException, ServiceResultException, InterruptedException {
		// select the root node & browse the references for the root node
		NodeId nodeId = Identifiers.RootFolder;
		browse(nodeId);

		// select the Objects node & browse the references for this node
		nodeId = selectNode(1);
		browse(nodeId);

		// select the Channel node "Siemens PLC S7-1200" & browse its references
		nodeId = selectNode(14);
		browse(nodeId);

		// select the Device node "s7-1200" & browse its references
		nodeId = selectNode(2);
		browse(nodeId);

		// select the node "Inputs" & browse its references
		nodeId = selectNode(3);
		browse(nodeId);

		// select the tags corresponding to "Inputs"
		int[] tags = {0, 1, 2, 3, 4, 5, 6, 7, 8};
		for (int i = 0; i < tags.length; i++)
			TagsArray.add(i, selectNode(tags[i]));

		// subscribe to data changes for the selected tags
		subscribe(TagsArray);

		log.info("[-- READING VALUES CHANGES FROM THE SERVER NODES --]");
		
		// looping endlessly
		while(true);
	}

	public void doWrite() throws ServiceException, StatusException, ServiceResultException, InterruptedException,
			AddressSpaceException {
		// select the root node & browse the references for the root node
				NodeId nodeId = Identifiers.RootFolder;
				browse(nodeId);

				// select the Objects node & browse the references for this node
				nodeId = selectNode(1);
				browse(nodeId);

				// select the Channel node "Siemens PLC S7-1200" & browse its references
				nodeId = selectNode(14);
				browse(nodeId);

				// select the Device node "s7-1200" & browse its references
				nodeId = selectNode(2);
				browse(nodeId);

				// select the node "Outputs" & browse its references
				nodeId = selectNode(4);
				browse(nodeId);

				// select the tags corresponding to "Outputs"
				//int[] tags = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
				int[] tags = {1};
				for (int i = 0; i < tags.length; i++)
					TagsArray.add(i, selectNode(tags[i]));

				// subscribe to data changes for the selected tags
				subscribe(TagsArray);
				Thread.sleep(2000);
				// write values
				write(TagsArray);
	}

	public void disconnect() throws ServiceException {
		// remove the subscription from the client
		client.removeSubscription(subscription);
		// disconnect from the server
		client.disconnect();
		println("");
		log.info("[-- DISCONNECTED FROM THE SERVER --]");

	}
	
	// define some shortcut methods for printing on the console
	public void printf(String s) {
		System.out.printf(s);
	}

	public void print(String s) {
		System.out.print(s);
	}

	public static void println(String s) {
		System.out.println(s);
	}

	public static void println(Exception e) {
		System.out.println(e);
	}

	public static void printf(String format, Object... args) {
		System.out.printf(format, args);
	}
	
	// define some characteristics of the OPC UA Client application
	public static void initialize(UaClient client) throws SecureIdentityException, IOException, UnknownHostException {
		// create an Application Description which is sent to the server
		ApplicationDescription appDescription = new ApplicationDescription();
		appDescription.setApplicationName(new LocalizedText("OpcuaClient", Locale.ENGLISH));
		/* 'localhost' (all lower case) in the ApplicationName and ApplicationURI
		is converted to the actual host name of the computer in which the application
		is run.*/
		// ApplicationUri is a unique identifier for each running instance
		appDescription.setApplicationUri("urn:localhost:UA:OpcuaClient");
		// identify the product and should therefore be the same for all instances
		appDescription.setProductUri("urn:prosysopc.com:UA:OpcuaClient");
		// define the type of application
		appDescription.setApplicationType(ApplicationType.Client);
		
		// define the client application certificate
		final ApplicationIdentity identity = new ApplicationIdentity();
		identity.setApplicationDescription(appDescription);
		// assign the identity to the Client
		client.setApplicationIdentity(identity);
	}

	public void browse(NodeId nodeId) throws ServiceException, StatusException {
		// print information about the selected node currently
		printCurrentNode(nodeId);
		// define a limit of 1000 references per call to the server
		client.getAddressSpace().setMaxReferencesPerNode(1000);
		// receive only the hierarchical references between the nodes
		client.getAddressSpace().setReferenceTypeId(Identifiers.HierarchicalReferences);
		// define the references of the selected node by Id
		references = client.getAddressSpace().browse(nodeId);
		log.info("[-- NODE HIERARCHICAL REFERENCES --]");
		// print the references
		for (int i = 0; i < references.size(); i++)
			printf("%d - %s\n", i, referenceToString(references.get(i)));
		println("");
	}

	protected void printCurrentNode(NodeId nodeId) {
		log.info("[-- SELECTED NODE --]");
		if (client.isConnected())
			// find the node from the NodeCache
			try {
				UaNode node = client.getAddressSpace().getNode(nodeId);

				if (node == null)
					return;
				String currentNodeStr = getCurrentNodeAsString(node);
				if (currentNodeStr != null) {
					// print the corresponding information
					println(currentNodeStr);
					println("");
				}
			} catch (ServiceException e) {
				println(e);
			} catch (AddressSpaceException e) {
				println(e);
			}
	}
	
	// convert the node reference information to String format
	protected String referenceToString(ReferenceDescription r)
			throws ServerConnectionException, ServiceException, StatusException {
		if (r == null)
			return "";
		String referenceTypeStr = null;
		try {
			// find the reference type from the NodeCache
			UaReferenceType referenceType = (UaReferenceType) client.getAddressSpace().getType(r.getReferenceTypeId());
			if ((referenceType != null) && (referenceType.getDisplayName() != null))
				if (r.getIsForward())
					referenceTypeStr = referenceType.getDisplayName().getText();
				else
					referenceTypeStr = referenceType.getInverseName().getText();
		} catch (AddressSpaceException e) {
			println(e);
			print(r.toString());
			referenceTypeStr = r.getReferenceTypeId().getValue().toString();
		}
		String typeStr;
		switch (r.getNodeClass()) {
		case Object:
		case Variable:
			try {
				// find the type from the NodeCache
				UaNode type = client.getAddressSpace().getNode(r.getTypeDefinition());
				if (type != null)
					typeStr = type.getDisplayName().getText();
				else
					typeStr = r.getTypeDefinition().getValue().toString();
			} catch (AddressSpaceException e) {
				println(e);
				print("type not found: " + r.getTypeDefinition().toString());
				typeStr = r.getTypeDefinition().getValue().toString();
			}
			break;
		default:
			typeStr = nodeClassToStr(r.getNodeClass());
			break;
		}
		return String.format("%s%s (ReferenceType=%s, BrowseName=%s%s)", r.getDisplayName().getText(), ": " + typeStr,
				referenceTypeStr, r.getBrowseName(), r.getIsForward() ? "" : " [Inverse]");
	}
	
	// method for manipulating dates and convert them into Strings
	protected static String dateTimeToString(String title, DateTime timestamp, UnsignedShort picoSeconds) {
		if ((timestamp != null) && !timestamp.equals(DateTime.MIN_VALUE)) {
			SimpleDateFormat format = new SimpleDateFormat("yyyy MMM dd (zzz) HH:mm:ss.SSS");
			StringBuilder sb = new StringBuilder(title);
			sb.append(format.format(timestamp.getCalendar(TimeZone.getDefault()).getTime()));
			if ((picoSeconds != null) && !picoSeconds.equals(UnsignedShort.valueOf(0)))
				sb.append(String.format("/%d picos", picoSeconds.getValue()));
			return sb.toString();
		}
		return "";
	}
	
	// show information about the selected current node
	protected String getCurrentNodeAsString(UaNode node) {
		String nodeStr = "";
		String typeStr = "";
		String analogInfoStr = "";
		nodeStr = node.getDisplayName().getText();
		UaType type = null;
		if (node instanceof UaInstance)
			type = ((UaInstance) node).getTypeDefinition();
		typeStr = (type == null ? nodeClassToStr(node.getNodeClass()) : type.getDisplayName().getText());

		/* This is the way to access type specific nodes and their
		properties, for example to show the engineering units and
		range for all AnalogItems */
		if (node instanceof AnalogItemType)
			try {
				AnalogItemType analogNode = (AnalogItemType) node;
				EUInformation units = analogNode.getEngineeringUnits();
				analogInfoStr = units == null ? "" : " Units=" + units.getDisplayName().getText();
				Range range = analogNode.getEuRange();
				analogInfoStr = analogInfoStr
						+ (range == null ? "" : String.format(" Range=(%f; %f)", range.getLow(), range.getHigh()));
			} catch (Exception e) {
				println(e);
			}

		String currentNodeStr = String.format("*** Current Node: %s: %s (ID: %s)%s", nodeStr, typeStr, node.getNodeId(),
				analogInfoStr);
		return currentNodeStr;
	}

	// show information about the node class
	private String nodeClassToStr(NodeClass nodeClass) {
		return "[" + nodeClass + "]";
	}

	/* select the next node according to its integer value
	in the list of references of the current node */
	public NodeId selectNode(int selection) throws ServiceResultException {
		ReferenceDescription r = references.get(selection);
		target = client.getAddressSpace().getNamespaceTable().toNodeId(r.getNodeId());
		return target;
	}

	/* list the possible attributes/properties Ids of the current node. 
	Ids are integer numbers */
	protected void readAttributeId() {
		log.info("[-- NODE ATTRIBUTES LIST --]");
		for (long i = Attributes.NodeId.getValue(); i < Attributes.UserExecutable.getValue(); i++)
			printf("%d - %s\n", i, AttributesUtil.toString(UnsignedInteger.valueOf(i)));
	}

	protected void subscribe(ArrayList<NodeId> array) throws ServiceException, StatusException, InterruptedException {
		// define a for loop for the array of monitored nodes(items)
		for (int i = 0; i < array.size(); i++) {
			// include a number of monitored items, which you listen to
			MonitoredDataItem item = new MonitoredDataItem(array.get(i), attributeId, MonitoringMode.Reporting);
			// add the monitored item to the subscription
			subscription.addItem(item);
			// establish a listener for each item
			item.setDataChangeListener(dataChangeListener);
		}
		// add the subscription to the client
		client.addSubscription(subscription);
	}
	
	// define the corresponding listener that monitors and print value changes on items
	private static MonitoredDataItemListener dataChangeListener = new MonitoredDataItemListener() {
		@Override
		public void onDataChange(MonitoredDataItem sender, DataValue prevValue, DataValue value) {
			MonitoredItem i = sender;
			println(dataValueToString(i.getNodeId(), i.getAttributeId(), value));
		}
	};
	
	// print the information about the value change on item according a predefined format
	protected static String dataValueToString(NodeId nodeId, UnsignedInteger attributeId, DataValue value) {
		StringBuilder sb = new StringBuilder();
		sb.append("Node: ");
		sb.append(nodeId);
		sb.append(".");
		sb.append(AttributesUtil.toString(attributeId));
		sb.append(" | Status: ");
		sb.append(value.getStatusCode());
		if (value.getStatusCode().isNotBad()) {
			sb.append(" | Value: ");
			if (value.isNull())
				sb.append("NULL");
			else {
				if (Attributes.Value.equals(attributeId))
					try {
						UaVariable variable = (UaVariable) client.getAddressSpace().getNode(nodeId);
						if (variable == null)
							sb.append("(Cannot read node datatype from the server) ");
						else {

							NodeId dataTypeId = variable.getDataTypeId();
							UaType dataType = variable.getDataType();
							if (dataType == null)
								dataType = client.getAddressSpace().getType(dataTypeId);

							Variant variant = value.getValue();
							variant.getCompositeClass();
							if (attributeId.equals(Attributes.Value))
								if (dataType != null)
									sb.append("(" + dataType.getDisplayName().getText() + ")");
								else
									sb.append("(DataTypeId: " + dataTypeId + ")");
						}
					} catch (ServiceException e) {
					} catch (AddressSpaceException e) {
					}
				final Object v = value.getValue().getValue();
				if (value.getValue().isArray())
					sb.append(MultiDimensionArrayUtils.toString(v));
				else
					sb.append(v);
			}
		}
		sb.append(dateTimeToString(" | ServerTimestamp: ", value.getServerTimestamp(), value.getServerPicoseconds()));
		sb.append(dateTimeToString(" | SourceTimestamp: ", value.getSourceTimestamp(), value.getSourcePicoseconds()));
		return sb.toString();
	}

	// write a predefined value for the tags from the ArrayList array
	protected void write(ArrayList<NodeId> tags)
			throws ServiceException, AddressSpaceException, StatusException, InterruptedException{
		println("");
		log.info("\n[-- WRITING VALUES TO THE SERVER NODES --]");
		
		for(int i = 0; i < tags.size(); i++)
		{
			// select the node corresponding to the selected tag
			UaNode node = client.getAddressSpace().getNode(tags.get(i));
			println("Writing to node " + tags.get(i) + " - " + node.getDisplayName().getText() 
					+ " " + new Date(System.currentTimeMillis()));

			/* find the DataType if setting Value - for other properties you must
		 	find the correct data type yourself */
			UaDataType dataType = null;
			if (attributeId.equals(Attributes.Value) && (node instanceof UaVariable)) 
			{
				UaVariable v = (UaVariable) node;
				// initialize DataType node, if it is not initialized yet
				if (v.getDataType() == null)
					v.setDataType(client.getAddressSpace().getType(v.getDataTypeId()));
				dataType = (UaDataType) v.getDataType();
				println("DataType: " + dataType.getDisplayName().getText());
				// define the value for the selected node.
				// In this case, a boolean value
				String value = "true";
				Object convertedValue = dataType != null
							? client.getAddressSpace().getDataTypeConverter().parseVariant(value, dataType) : value;
					boolean status = client.writeAttribute(tags.get(i), attributeId, convertedValue);
					Thread.sleep(4000);
					status = client.writeAttribute(tags.get(i), attributeId, false);
					if (status)
						println("OK");
					else
						println("OK (completes asynchronously)");
			}
		}
		while(true);
	}
}