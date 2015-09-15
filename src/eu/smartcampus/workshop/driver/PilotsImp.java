package eu.smartcampus.workshop.driver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import eu.smartcampus.api.datapointconnectivity.AbstractDatapointConnectivityService;
import eu.smartcampus.api.datapointconnectivity.DatapointAddress;
import eu.smartcampus.api.datapointconnectivity.DatapointMetadata;
import eu.smartcampus.api.datapointconnectivity.DatapointValue;
import eu.smartcampus.api.datapointconnectivity.IDatapointConnectivityService;
import eu.smartcampus.api.datapointconnectivity.WriteCallbackImpl;
/**
 * Driver, implementation of the abstract class AbstractDatapointConnectivityService.
 *
 */
public final class PilotsImp extends
        AbstractDatapointConnectivityService {
	
	
    private final MessageReceiver messageReceiver;
    private final MessageSender messageSender;
    private final Socket socket;
    private int nextRequestID;
    /**
     * Stores a name of the file with the properties
     */
    private final static String PROP_FILE = "Myyrmaki_datapoints.properties";
    private final Properties properties = new Properties();
    /**
     * Stores the values of all datapoints as strings
     */
	private static HashMap<DatapointAddress,String> valueSet;
	
	public PilotsImp() throws UnknownHostException, IOException {
    	
        nextRequestID = 0;
        socket = new Socket("127.0.0.1", 80);
        //socket = new Socket("10.254.1.1", 50000);
        messageSender = new MessageSender(socket);
        messageReceiver = new MessageReceiver(socket);
        messageSender.start();
        messageReceiver.start();
    }
	/**
	 * Method returns value corresponding to the given datapoint
	 * @param address datapoint address
	 */
    public String getDatapointValue(DatapointAddress address){
    	return valueSet.get(address);
    }
    
    /**
     * Method returns array of addresses taken from a file with the properties
     */
    @Override
    public DatapointAddress[] getAllDatapoints() {
        // TODO Auto-generated method stub
    	try {
            final InputStream input = new FileInputStream(PROP_FILE);
            properties.load(input);
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
	    final int numberOfAvailableDatapoints = Integer.parseInt(properties.getProperty("dp_quantity"));
	    final DatapointAddress[] addresses = new DatapointAddress[numberOfAvailableDatapoints];
	    for (int i = 1; i <= numberOfAvailableDatapoints; i++) {
	    	addresses[i - 1] = new DatapointAddress(properties.getProperty("dp" + i + "_datapoint_address"));
	    }
	    return addresses;
    }

    /**
     * Not implemented
     */
    @Override
    public DatapointMetadata getDatapointMetadata(DatapointAddress address) throws OperationFailedException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Method returns name of the implementation
     */
    @Override
    public String getImplementationName() {
        return "Metropolia Gateway Driver Implementation";
    }

    /**
     * Reads data from the given datapoint
     * @param address the datapoint address
     * @param readCallback the link to the callback object
     * @throws OperationFailedException
     * @return an int value requestID - uniquely identified each time this method called
     */
    @Override
    public int requestDatapointRead(final DatapointAddress address, final ReadCallback readCallback) throws OperationFailedException {
        final int thisRequestID = nextRequestID++;
        new Thread() {
            public void run() {
                String messageReceived;
                final String first = address.getAddress().split(":")[0];
                final String second = address.getAddress().split(":")[1];
                final String messageToSend = ">V:1,C:152,@1.1." + first + "." + second +"#";
                messageSender.submitMessage(messageToSend);
                try {
                    final String expectedResponse = "?V:1,C:152,@1.1." + first + "." + second;
                    do {
                        messageReceived = messageReceiver.getNextReceivedMessage();
                    } while (!messageReceived.contains(expectedResponse));
                    System.out.println(messageReceived);
                    String newvalue = messageReceived.split("=")[1];
                    newvalue = newvalue.split("#")[0];
                    final DatapointValue value = new DatapointValue(newvalue);
                    
                    readCallback.onReadCompleted(address, new DatapointValue[] { value }, thisRequestID);
                } catch (InterruptedException e) {
                    readCallback.onReadAborted(address, ErrorType.DEVICE_CONNECTION_ERROR, nextRequestID);
                }
            }

        }.start();
        return thisRequestID;
    }

    @Override
    public int requestDatapointWindowRead(DatapointAddress address, long startTimestamp, long finishTimestamp, ReadCallback readCallback) throws OperationFailedException {
        return requestDatapointRead(address, readCallback);
    }

    /**
     * Receives an address as a string and a set of values to send to the datapoint, after executing method calls a WriteCallback object.
     * @param address the datapoint address
     * @param values array of vavlues
     * @param write
     * @return an int value requestID - uniquely identified each time this method called
     */
    @Override
    public int requestDatapointWrite(final DatapointAddress address, final DatapointValue[] values, final WriteCallback writeCallback) throws OperationFailedException {
        final int thisRequestID = nextRequestID++;
        new Thread() {
            public void run() {
            	if(address.getAddress().contains("1:")){
            		final String first = address.getAddress().split(":")[0];
            		final String second = address.getAddress().split(":")[1];
            		final String value = values[0].getValue();
            		final String messageToSend = ">V:1,C:14,L:" + value + ",F:5,@1.1." + first + "." + second + "#";
            		messageSender.submitMessage(messageToSend);
            		writeCallback.onWriteCompleted(address, WritingConfirmationLevel.GATEWAY_CONFIRMED, thisRequestID);
            	}else{
            		final String group = address.getAddress().split(":")[0];
            		final String scene = address.getAddress().split(":")[1];
            		final String messageToSend = ">V:1,C:11,G" + group + ",K:0,B:1,S:" + scene + ",F:5#";
                	messageSender.submitMessage(messageToSend);
                	writeCallback.onWriteCompleted(address, WritingConfirmationLevel.GATEWAY_CONFIRMED, thisRequestID);
            	}
            }
        }.start();
        return thisRequestID;
    }

}




