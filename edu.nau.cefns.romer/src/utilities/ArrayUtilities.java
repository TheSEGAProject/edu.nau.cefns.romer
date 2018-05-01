package utilities;

public class ArrayUtilities
{
	static public int[] extend_int_array(int[] array, int ext)
	{
		int[] temp_array = array;
		// array = new int[array.length + ext];
		array = new int[array.length + ext];
		for (int i = 0; i < temp_array.length; i++)
			array[i] = temp_array[i];
		return array;
	}

	static public byte[] convert_to_byte_array(int[] int_array)
	{
		byte[] byte_array = new byte[int_array.length];
		for (int i = 0; i < int_array.length; i++)
			byte_array[i] = (byte)int_array[i];
		return byte_array;
	}
	
	static public int[] convert_to_int_array(byte[] byte_array)
	{
		int[] int_array = new int[byte_array.length];
		for (int i = 0; i < byte_array.length; i++) {
			if (byte_array[i] < 0)
				int_array[i] = byte_array[i] + 256;
			else
				int_array[i] = byte_array[i];
		}
		return int_array;
	}
}
