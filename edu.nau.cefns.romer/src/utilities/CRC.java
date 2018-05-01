package utilities;

public class CRC
{
	static public boolean check_crc(int[] packet)
	{
		// rec_packet information
		int length = packet.length;
		// CRC is assumed to be the last two bytes of the packet
		int rec_crc = (packet[length - 2] << 8) + packet[length - 1];
		// Remove the CRC
		int[] new_packet = new int[length - 2];
		for (int i = 0; i < length - 2; i++)
			new_packet[i] = packet[i];
		
		int crc = compute_crc(new_packet);
		// Does the CRC match rec_packet CRC
		if (crc == rec_crc)
			return true;
		else
			return false;
	}
	
	static public int compute_crc(int[] packet)
	{
		int crc = 0xFFFF;
		int poly = 0x1021;
		for (int n : packet) {
			for (int i = 0; i < 8; i++) {
				boolean bit = ((n >> (7 - i) & 1) == 1);
				boolean c15 = ((crc >> 15 & 1) == 1);
				crc <<= 1;
				if (c15 ^ bit)
					crc ^= poly;
			}
		}
		return crc &= 0xFFFF;
	}
}
