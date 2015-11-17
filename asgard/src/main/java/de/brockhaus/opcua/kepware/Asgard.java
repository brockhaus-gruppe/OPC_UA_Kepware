package de.brockhaus.opcua.kepware;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

public class Asgard 
{
	
	/** */
	static UaClient client;
	
	static final String SERVERURI = "opc.tcp://127.0.0.1:49320";
	
	NodeId target;
	
	ArrayList<NodeId> TagsArray = new ArrayList<NodeId>();
	
	List<ReferenceDescription> references;
	UnsignedInteger attributeId = UnsignedInteger.valueOf(13);
	Subscription subscription = new Subscription();
	Logger log = Logger.getLogger(this.getClass().getName());

	// Constructor
	public Asgard() throws FileNotFoundException, IOException {
		Properties props = new Properties();
		props.load(new FileInputStream("src/main/resources/log4j.properties"));
		PropertyConfigurator.configure(props);
	}

	// Methods
	public void init() throws URISyntaxException, UnknownHostException, SecureIdentityException, IOException {
		client = new UaClient(SERVERURI);
		client.setSecurityMode(SecurityMode.NONE);
		initialize(client);
		log.info("[-- STARTING INITIAL SETUP --]");
	}

	public void connect()
			throws InvalidServerEndpointException, ConnectException, SessionActivationException, ServiceException {
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

		log.info("[-- READING VALUES FROM THE SERVER NODE --]");
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
				int[] tags = {0};
				for (int i = 0; i < tags.length; i++)
					TagsArray.add(i, selectNode(tags[i]));

				// subscribe to data changes for the selected tags
				subscribe(TagsArray);
				
				// write values
				write(TagsArray);
	}

	public void disconnect() throws ServiceException {
		client.removeSubscription(subscription);
		client.disconnect();
		println("");
		log.info("[-- DISCONNECTED FROM THE SERVER --]");

	}

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

	public static void initialize(UaClient client) throws SecureIdentityException, IOException, UnknownHostException {
		// *** Application Description is sent to the server
		ApplicationDescription appDescription = new ApplicationDescription();
		appDescription.setApplicationName(new LocalizedText("OpcuaClient", Locale.ENGLISH));
		// 'localhost' (all lower case) in the URI is converted to the actual
		// host name of the computer in which the application is run
		appDescription.setApplicationUri("urn:localhost:UA:OpcuaClient");
		appDescription.setProductUri("urn:prosysopc.com:UA:OpcuaClient");
		appDescription.setApplicationType(ApplicationType.Client);

		final ApplicationIdentity identity = new ApplicationIdentity();
		identity.setApplicationDescription(appDescription);
		client.setApplicationIdentity(identity);
	}

	public void browse(NodeId nodeId) throws ServiceException, StatusException {
		printCurrentNode(nodeId);
		client.getAddressSpace().setMaxReferencesPerNode(1000);
		client.getAddressSpace().setReferenceTypeId(Identifiers.HierarchicalReferences);
		references = client.getAddressSpace().browse(nodeId);
		log.info("[-- NODE HIERARCHICAL REFERENCES --]");
		for (int i = 0; i < references.size(); i++)
			printf("%d - %s\n", i, referenceToString(references.get(i)));
		println("");
	}

	protected void printCurrentNode(NodeId nodeId) {
		log.info("[-- SELECTED NODE --]");
		if (client.isConnected())
			// Find the node from the NodeCache
			try {
				UaNode node = client.getAddressSpace().getNode(nodeId);

				if (node == null)
					return;
				String currentNodeStr = getCurrentNodeAsString(node);
				if (currentNodeStr != null) {
					println(currentNodeStr);
					println("");
				}
			} catch (ServiceException e) {
				println(e);
			} catch (AddressSpaceException e) {
				println(e);
			}
	}

	protected String referenceToString(ReferenceDescription r)
			throws ServerConnectionException, ServiceException, StatusException {
		if (r == null)
			return "";
		String referenceTypeStr = null;
		try {
			// Find the reference type from the NodeCache
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
				// Find the type from the NodeCache
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

	protected String getCurrentNodeAsString(UaNode node) {
		String nodeStr = "";
		String typeStr = "";
		String analogInfoStr = "";
		nodeStr = node.getDisplayName().getText();
		UaType type = null;
		if (node instanceof UaInstance)
			type = ((UaInstance) node).getTypeDefinition();
		typeStr = (type == null ? nodeClassToStr(node.getNodeClass()) : type.getDisplayName().getText());

		// This is the way to access type specific nodes and their
		// properties, for example to show the engineering units and
		// range for all AnalogItems
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

	private String nodeClassToStr(NodeClass nodeClass) {
		return "[" + nodeClass + "]";
	}

	public NodeId selectNode(int selection) throws ServiceResultException {
		ReferenceDescription r = references.get(selection);
		target = client.getAddressSpace().getNamespaceTable().toNodeId(r.getNodeId());
		return target;
	}

	protected void readAttributeId() {
		log.info("[-- NODE ATTRIBUTES LIST --]");
		for (long i = Attributes.NodeId.getValue(); i < Attributes.UserExecutable.getValue(); i++)
			printf("%d - %s\n", i, AttributesUtil.toString(UnsignedInteger.valueOf(i)));
	}

	protected void subscribe(ArrayList<NodeId> array) throws ServiceException, StatusException, InterruptedException {
		for (int i = 0; i < array.size(); i++) {
			MonitoredDataItem item = new MonitoredDataItem(array.get(i), attributeId, MonitoringMode.Reporting);
			subscription.addItem(item);
			client.addSubscription(subscription);
			item.setDataChangeListener(dataChangeListener);
		}
	}

	private static MonitoredDataItemListener dataChangeListener = new MonitoredDataItemListener() {
		@Override
		public void onDataChange(MonitoredDataItem sender, DataValue prevValue, DataValue value) {
			MonitoredItem i = sender;
			println(dataValueToString(i.getNodeId(), i.getAttributeId(), value));
		}
	};

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

	protected void write(ArrayList<NodeId> tags)
			throws ServiceException, AddressSpaceException, StatusException, InterruptedException 
	{
		println("");
		log.info("[-- WRITING VALUES TO THE SERVER NODE --]");
		
		for(int i = 0; i < tags.size(); i++)
		{
			UaNode node = client.getAddressSpace().getNode(tags.get(i));
			println("Writing to node " + tags.get(i) + " - " + node.getDisplayName().getText());

			// Find the DataType if setting Value - for other properties you must
			// find the correct data type yourself
			UaDataType dataType = null;
			if (attributeId.equals(Attributes.Value) && (node instanceof UaVariable)) 
			{
				UaVariable v = (UaVariable) node;
				// Initialize DataType node, if it is not initialized yet
				if (v.getDataType() == null)
					v.setDataType(client.getAddressSpace().getType(v.getDataTypeId()));
				dataType = (UaDataType) v.getDataType();
				println("DataType: " + dataType.getDisplayName().getText());
				boolean value = true;
				println("Value: " + String.valueOf(value));
				client.writeAttribute(tags.get(i), attributeId, value);
			}
		}
		Thread.sleep(3000);
	}
}