
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
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
		
		// Header Section
		
		// Generate Query ID
		Random rng = new Random();
		int randomInteger = rng.nextInt(65536);
		ByteBuffer bb = ByteBuffer.allocate(2);
		byte[] qID = bb.putInt(randomInteger).array();
		
		// The rest of the header
		byte[] rest = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, 
								  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 };
		
		byte[] header = new byte[qID.length + rest.length];
		System.arraycopy(qID, 0, header, 0, qID.length);
		System.arraycopy(rest, 0, header, qID.length, rest.length);
		
		// Question Section
		
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


