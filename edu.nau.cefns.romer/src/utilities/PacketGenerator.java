package utilities;

public class PacketGenerator {
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @return
	 */
	public static byte[] Reset_Command_Packet(String hub_high, String hub_low, String dest_high, String dest_low){
		return Reset_Command_Packet((byte)Integer.parseInt(hub_high,16),(byte)Integer.parseInt(hub_low,16),(byte)Integer.parseInt(dest_high,16),(byte)Integer.parseInt(dest_low,16));	
	}
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @return
	 */
	public static byte[] Reset_Command_Packet(byte hub_high, byte hub_low, byte dest_high, byte dest_low){
		byte[] cmdMessage = 
	    	{
	    		//=======NET=======
	    		
	    		//DESTINATION ADDRESS [2]
	    		hub_high,//(Destination HI)
	    		hub_low,//(Destination LOW)
	    		//SOURCE ADDRESS [2]
	    		(byte)0xFE,//(Source)
	    		(byte)0xFE,
	    		
	    		//=======TRANSPORT=======
	    		
	    		//MESSAGE ID [1]
	    		(byte)0x03,//(Operational)
	    		//MESSAGE FLAGS [1]
	    		(byte)0x20,//(Single Message)
	    		//MESSAGE NUMBER [2]
	    		(byte)0x00,
	    		(byte)0x01,//(Message 1 of 1)
	    		//MESSAGE ADDRESS [2]
	    		dest_high,
	    		dest_low,//(WiSARD ID)
	    		//MESSAGE PAYLOAD LENGTH [1] Byte Position: 10
	    		(byte)0x00,
	    		
	    		//=======MESSAGE PAYLOAD=======
	    		
	    		//DATA ELEMENT ID [1]
	    		(byte)0x01, //(CMD packet)
	    		//DATA PAYLOAD LENGTH [1]
	    		(byte)0x06,
	    		//DATA ELEMENT VERSION [1]
	    		(byte)0x6E, //??
	    		
	    		//=======DATA ELEMENT PAYLOAD=======
	    		
	    		//COMMAND ID [2]
	    		(byte)0x00, //Always the CP
	    		(byte)0x0B, //Reset Command
	    	};
		
		cmdMessage[10] = (byte) (cmdMessage.length+1);
		return cmdMessage;
	}
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @param processor
	 * @param transducer
	 * @param command
	 * @return
	 */
	public static byte[] Valve_Command_Packet(String hub_high, String hub_low, String dest_high, String dest_low, String processor, String transducer, String command){
		return Valve_Command_Packet(
				(byte)Integer.parseInt(hub_high,16),
				(byte)Integer.parseInt(hub_low,16),
				(byte)Integer.parseInt(dest_high,16),
				(byte)Integer.parseInt(dest_low,16),
				(byte)Integer.parseInt(processor,16),
				(byte)Integer.parseInt(transducer,16),
				(byte)Integer.parseInt(command,16)
				);	
	}
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @param processor
	 * @param transducer
	 * @param command
	 * @return
	 */
	public static byte[] Valve_Command_Packet(byte hub_high, byte hub_low, byte dest_high, byte dest_low, byte processor, byte transducer, byte command){
		
	    //Variables for storing fetched data and the command		    		
	    byte[] cmdMessage = 
	    	{
	    		//=======NET=======
	    		
	    		//DESTINATION ADDRESS [2]
	    		hub_high,//(Destination HI)
	    		hub_low,//(Destination LOW)
	    		//SOURCE ADDRESS [2]
	    		(byte)0xFE,//(Source)
	    		(byte)0xFE,
	    		
	    		//=======TRANSPORT=======
	    		
	    		//MESSAGE ID [1]
	    		(byte)0x03,//(Operational)
	    		//MESSAGE FLAGS [1]
	    		(byte)0x20,//(Single Message)
	    		//MESSAGE NUMBER [2]
	    		(byte)0x00,
	    		(byte)0x01,//(Message 1 of 1)
	    		//MESSAGE ADDRESS [2]
	    		dest_high,
	    		dest_low,//(WiSARD ID)
	    		//MESSAGE PAYLOAD LENGTH [1] Byte Position: 10
	    		(byte)0x00,
	    		
	    		//=======MESSAGE PAYLOAD=======
	    		
	    		//DATA ELEMENT ID [1]
	    		(byte)0x01, //(CMD packet)
	    		//DATA PAYLOAD LENGTH [1]
	    		(byte)0x06,
	    		//DATA ELEMENT VERSION [1]
	    		(byte)0x6E, //??
	    		
	    		//=======DATA ELEMENT PAYLOAD=======
	    		
	    		//COMMAND ID [2]
	    		processor, //Processor ID
	    		transducer, //Transducer ID
	    		command //0x5B for on ( index is 16)
	    	};
	    		
	    cmdMessage[10] = (byte) (cmdMessage.length+1);
	    return cmdMessage;
	}
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @param command
	 * @return
	 */
	public static byte[] RSSI_Command_Packet(String hub_high, String hub_low, String dest_high, String dest_low, String command){
		return RSSI_Command_Packet(
				(byte)Integer.parseInt(hub_high,16),
				(byte)Integer.parseInt(hub_low,16),
				(byte)Integer.parseInt(dest_high,16),
				(byte)Integer.parseInt(dest_low,16),
				(byte)Integer.parseInt(command,16)
			);	
	}
	/**
	 * 
	 * @param hub_high
	 * @param hub_low
	 * @param dest_high
	 * @param dest_low
	 * @param command
	 * @return
	 */
	public static byte[] RSSI_Command_Packet(byte hub_high, byte hub_low, byte dest_high, byte dest_low, byte command){
		byte[] cmdMessage = 
	    	{
	    		//=======NET=======
	    		
	    		//DESTINATION ADDRESS [2]
	    		hub_high,//(Destination HI)
	    		hub_low,//(Destination LOW)
	    		//SOURCE ADDRESS [2]
	    		(byte)0xFE,//(Source)
	    		(byte)0xFE,
	    		
	    		//=======TRANSPORT=======
	    		
	    		//MESSAGE ID [1]
	    		(byte)0x03,//(Operational)
	    		//MESSAGE FLAGS [1]
	    		(byte)0x20,//(Single Message)
	    		//MESSAGE NUMBER [2]
	    		(byte)0x00,
	    		(byte)0x01,//(Message 1 of 1)
	    		//MESSAGE ADDRESS [2]
	    		dest_high,
	    		dest_low,//(WiSARD ID)
	    		//MESSAGE PAYLOAD LENGTH [1] Byte Position: 10
	    		(byte)0x00,
	    		
	    		//=======MESSAGE PAYLOAD=======
	    		
	    		//DATA ELEMENT ID [1]
	    		(byte)0x01, //(CMD packet)
	    		//DATA PAYLOAD LENGTH [1]
	    		(byte)0x06,
	    		//DATA ELEMENT VERSION [1]
	    		(byte)0x6E, //??
	    		
	    		//=======DATA ELEMENT PAYLOAD=======
	    		
	    		//COMMAND ID [2]
	    		(byte)0x00, //Always the CP
	    		(byte)0x0A, //RSSI Command
	    		command		//On/Off
	    	};
		cmdMessage[10] = (byte) (cmdMessage.length+1);
		return cmdMessage;
	}

}
