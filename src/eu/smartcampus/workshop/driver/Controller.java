package eu.smartcampus.workshop.driver;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import eu.smartcampus.api.datapointconnectivity.*;
import eu.smartcampus.api.datapointconnectivity.IDatapointConnectivityService.DatapointListener;
import eu.smartcampus.api.datapointconnectivity.IDatapointConnectivityService.ErrorType;
import eu.smartcampus.api.datapointconnectivity.IDatapointConnectivityService.OperationFailedException;
import eu.smartcampus.api.datapointconnectivity.IDatapointConnectivityService.ReadCallback;
import eu.smartcampus.api.datapointconnectivity.IDatapointConnectivityService.WriteCallback;
import eu.smartcampus.api.datapointconnectivity.IDatapointConnectivityService.WritingConfirmationLevel;

/**
 * 
 * Reads data from the property file
 * Stores the current values corresponding to the all datapoints in the data structure
 * Calls driver's methods to set/get values to the datapoints
 */
public class Controller {
	/**
	 * Data structure to store datapoint values
	 */
	private static Map<DatapointAddress, DatapointValue> values;
	/**
	 * Data structure to store datapoint addresses
	 */
	private static DatapointAddress[] addresses;
	static PilotsImp driver;
/**
 * Initialize object of the class PilotsImp, which represents driver for communication with the datapoints
 * Initialize HashMap object to store the values corresponding to each datapoint address
 * Launch method getAllDatapoints() of the class PilotsImp object
 */
	public Controller(){
		
		try{
			driver = new PilotsImp();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		addresses = driver.getAllDatapoints();
		values = new HashMap<DatapointAddress,DatapointValue>();
		for (DatapointAddress adr : addresses) {
			values.put(adr, new DatapointValue("100"));
			System.out.println("value in the map inside controller constructor:" + values.get(adr));
		}
	}
	
	/**
	 * Sets the predefine value to the given datapoint by calling method setValue, value is 100 - switch on the light
	 * @param adr address of the datapoint
	 */
	public void lampON(DatapointAddress adr){
		setValue(adr, new DatapointValue ("100"));
	}

	/**
	 * Sets the predefine value to the given datapoint by calling method setValue, value is 0 - switch off the light
	 * @param adr address of the datapoint
	 */
	public void lampOFF(DatapointAddress adr){
		setValue(adr, new DatapointValue("0"));
	}
	/**
	 * Sets a value to the given datapoint, before calling the driver method to send data to the datapoint
	 * it checks if the current state is equal to the value.
	 * Creates a WriteCallback object and give this object to the driver's method requestDatapointWrite
	 * @param address address of the datapoint to set value to
	 * @param value value to set to the given datapoint
	 */
	public void setValue(DatapointAddress address, DatapointValue value){
		try {
			final DatapointValue p[] = new DatapointValue[1];
			p[0] = value;
			final Long time = values.get(address).getTimestamp();
			final String strval = values.get(address).getValue();
			final int val = Integer.parseInt(strval);
			final boolean a = p[0].getTimestamp()>time;
			final boolean b = Integer.parseInt(p[0].getValue()) != val;

			if( a && b)
			{
				driver.requestDatapointWrite(address, p, 
						new IDatapointConnectivityService.WriteCallback() 
				{
					@Override
					public void onWriteCompleted(DatapointAddress address,
						WritingConfirmationLevel confirmationLevel, int requestId) {
						values.put(address, p[0]);
						System.out.println("request confirmed: " + confirmationLevel);
					}
					@Override
					public void onWriteAborted(DatapointAddress address, ErrorType reason,
						int requestId) {
						System.out.println("request aborted: " + reason);
					}
				});
			}
		}catch (OperationFailedException e) {
			System.out.println("Failed to write data to address" + address);
			e.printStackTrace();
		}
	}
	
	/**
	 * Reads value from the datapoint and stores it to the HashMap object
	 * While reading value, method initializes ReadCallback interface from the package "eu.smartcampus.api.datapointconnectivity"
	 * and passes this object to the requestDatapointRead method of the driver object
	 * @param address address of the given datapoint to read data from
	 */
	public void readValue(DatapointAddress address){
		try {
			driver.requestDatapointRead(address, new IDatapointConnectivityService.ReadCallback() {
			    @Override
			    public void onReadAborted(DatapointAddress address, ErrorType reason, int requestId) {
			        System.out.println("The request was aborted because: " + reason);
			        
			    }

			    @Override
			    public void onReadCompleted(DatapointAddress address, DatapointValue[] readings, int requestId) {
			    	values.put(address, readings[0]);
			        System.out.println("Read was a success, value returned was: " + readings[0].getValue());
			        System.out.println("Address is: " + address);
			    }
			});
		} catch (OperationFailedException e) {
			System.out.println("Failed to read data from address" + address);
			e.printStackTrace();
		}
	}
	
	public String getValue(DatapointAddress adr){
		return values.get(adr).getValue();
	}
}
