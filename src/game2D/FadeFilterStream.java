package game2D;
import java.io.*;

public class FadeFilterStream extends FilterInputStream {

	FadeFilterStream(InputStream in) { super(in); }
	
	/**
	 * Get a value from the array 'buffer' at the given 'position'
	 * and convert it into short big-endian format
	 * */
	public short getSample(byte[] buffer, int position)
	{
		return (short) (((buffer[position+1] & 0xff) << 8) |
					     (buffer[position] & 0xff));
	}
	
	/**
	 * Set a short value 'sample' in the array 'buffer' at the
	 * given 'position' in little-endian format
	 * */
	public void setSample(byte[] buffer, int position, short sample)
	{
		buffer[position] = (byte)(sample & 0xFF);
		buffer[position+1] = (byte)((sample >> 8) & 0xFF);
	}

	public int read(byte [] sample, int offset, int length) throws IOException
	{
		int bytesRead = super.read(sample,offset,length);
		float change = 2.0f * (2.4f / (float)bytesRead);
		float volume = 1.0f;
		short amp=0;

		for (int p=0; p<bytesRead; p = p + 2)
		{
			amp = getSample(sample,p);
			amp = (short)((float)amp * volume);
			setSample(sample,p,amp);
			volume = volume - change;
		}
		
		return length;
	}
}
