package utilities;

import helpers.SampleTimestampPackage;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.rbnb.sapi.ChannelMap;

public class DataGeneratorCodec {
	public static byte[] encodeValuePair(byte type,long timestamp,Object data){
		byte[] time_stamp = ByteBuffer.allocate(8).putLong(timestamp).array();
		byte[] data_point = null;
		
		switch(type){
		case ChannelMap.TYPE_FLOAT32:
			data_point = ByteBuffer.allocate(4).putFloat((float)data).array();
			break;
		case ChannelMap.TYPE_FLOAT64:
			data_point = ByteBuffer.allocate(8).putDouble((double)data).array();
			break;
		case ChannelMap.TYPE_INT16:
			data_point = ByteBuffer.allocate(2).putShort((short)data).array();
			break;
		case ChannelMap.TYPE_INT32:
			data_point = ByteBuffer.allocate(4).putInt((int)data).array();
			break;
		case ChannelMap.TYPE_INT64:
			data_point = ByteBuffer.allocate(8).putLong((long)data).array();
			break;
		case ChannelMap.TYPE_INT8:
			data_point = ByteBuffer.allocate(1).put((byte)data).array();
			break;
		case ChannelMap.TYPE_STRING:
			String str = (String)data;
			data_point = str.getBytes();
			break;
		default: 
			//log unkown data type
			break;

		}
		if(data_point != null){
			byte[] blob = new byte[1 + time_stamp.length + data_point.length];
			System.arraycopy(new byte[]{type},0,blob,0,1);
			System.arraycopy(time_stamp,0,blob,1,time_stamp.length);
			System.arraycopy(data_point,0,blob,1 + time_stamp.length,data_point.length);
			return blob;
		}
		else{
			return null;
		}
	}
	
	public static SampleTimestampPackage decodeValuePair(double rbnb_timestamp,byte[] blob){	
		int data_type = blob[0];
		long final_rbnb_timestamp = (long)(rbnb_timestamp*1000);
		
		long sample_timestamp = ByteBuffer.wrap(blob, 1, 8).getLong();		
		
		switch(data_type){
		case ChannelMap.TYPE_FLOAT32:
			return new SampleTimestampPackage(final_rbnb_timestamp,sample_timestamp,ByteBuffer.wrap(blob, 9, 4).getFloat());			
		case ChannelMap.TYPE_FLOAT64:
			return new SampleTimestampPackage(final_rbnb_timestamp,sample_timestamp,ByteBuffer.wrap(blob, 9, 8).getDouble());
		case ChannelMap.TYPE_INT16:
			return new SampleTimestampPackage(final_rbnb_timestamp,sample_timestamp,ByteBuffer.wrap(blob, 9, 2).getShort());
		case ChannelMap.TYPE_INT32:
			return new SampleTimestampPackage(final_rbnb_timestamp,sample_timestamp,ByteBuffer.wrap(blob, 9, 4).getInt());
		case ChannelMap.TYPE_INT64:
			return new SampleTimestampPackage(final_rbnb_timestamp,sample_timestamp,ByteBuffer.wrap(blob, 9, 8).getLong());
		case ChannelMap.TYPE_INT8:
			return new SampleTimestampPackage(final_rbnb_timestamp,sample_timestamp,ByteBuffer.wrap(blob, 9, 1).get());
		case ChannelMap.TYPE_STRING:
			return new SampleTimestampPackage(final_rbnb_timestamp,sample_timestamp,new String(Arrays.copyOfRange(blob, 10, blob.length)));
		default: 
			//log unkown data type
			return null;

		}
	
		
		
		
		
	}

}
