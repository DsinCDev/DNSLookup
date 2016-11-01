
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

/**
 * 
 */

/**
 * @author Donald Acton
 * This example is adapted from Kurose & Ross
 *
 */
public class DNSlookup {


	static final int MIN_PERMITTED_ARGUMENT_COUNT = 2;
	static boolean tracingOn = false;
	static InetAddress rootNameServer;
	static int portNumber = 53;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		String fqdn;
		DNSResponse response; // Just to force compilation
		int argCount = args.length;
		
		if (argCount < 2 || argCount > 3) {
			usage();
			return;
		}

		rootNameServer = InetAddress.getByName(args[0]);
		// Convert to bytes
		byte[] ipByte = rootNameServer.getAddress();
		fqdn = args[1];
		
		if (argCount == 3 && args[2].equals("-t"))
				tracingOn = true;
		
		// Start adding code here to initiate the lookup
		
		byte[] query = generateQuery(fqdn);
		
	}

	private static byte[] generateQuery(String fqdn) throws IOException {
		// Header Section
		
		// Generate Query ID
		Random rng = new Random();
		int randomInteger = rng.nextInt(65536);
		
		byte[] qID = new byte[] { (byte)randomInteger };
		
		// The rest of the header
		byte[] rest = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, 
								  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 };
		
		byte[] header = new byte[qID.length + rest.length];
		System.arraycopy(qID, 0, header, 0, qID.length);
		System.arraycopy(rest, 0, header, qID.length, rest.length);
		
		System.out.println(Arrays.toString(header));
		
		// Question Section
		
		String[] fqdnParts = fqdn.split("\\.");
		int length = fqdnParts.length;
		int counter = 0;
		
		for (int i = 0; i < length; i++) {
			counter += fqdnParts[i].length();
		}
		
		byte[] qName = new byte[length + counter];
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		for (int i = 0; i < length; i++) {
			outputStream.write((byte) fqdnParts[i].length()); 
			outputStream.write(fqdnParts[i].getBytes());
		}
		
		// End of stream
		outputStream.write((byte) 0x00);
		
		qName = outputStream.toByteArray();
		
		byte[] qType = new byte[] { (byte) 0x00, (byte) 0x01 };
		
		byte[] qClass = new byte[] { (byte) 0x00, (byte) 0x01 };
		
		ByteArrayOutputStream qStream = new ByteArrayOutputStream();
		qStream.write(qName);
		qStream.write(qType);
		qStream.write(qClass);
		
		byte[] question = qStream.toByteArray();
		
		byte[] query = new byte[header.length + question.length];
		System.arraycopy(header, 0, query, 0, header.length);
		System.arraycopy(question, 0, query, header.length, question.length);
		
		System.out.println(Arrays.toString(query));
		
		return query;
	}

	private static Object generateQName(String fqdn) {
		// TODO Auto-generated method stub
		return null;
	}

	private static void usage() {
		System.out.println("Usage: java -jar DNSlookup.jar rootDNS name [-t]");
		System.out.println("   where");
		System.out.println("       rootDNS - the IP address (in dotted form) of the root");
		System.out.println("                 DNS server you are to start your search at");
		System.out.println("       name    - fully qualified domain name to lookup");
		System.out.println("       -t      -trace the queries made and responses received");
	}
}


