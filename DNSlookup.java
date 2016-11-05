
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
	static InetAddress rootNameServerOriginal;
	static String fqdnOriginal;
	static int portNumber = 53;   
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		String fqdn;
		int argCount = args.length;
		
		if (argCount < 2 || argCount > 3) {
			usage();
			return;
		}

		rootNameServer = InetAddress.getByName(args[0]);
		rootNameServerOriginal = rootNameServer;
		// Convert to bytes
		fqdn = args[1];
		fqdnOriginal = fqdn;
		
		if (argCount == 3 && args[2].equals("-t"))
				tracingOn = true;
		
		// Start adding code here to initiate the lookup
		
		generateQuery(fqdn);
		
	}
	
	private static int generateRandom() {
		// Generate Query ID
		Random rng = new Random();
		int randomInteger = rng.nextInt(65336);
	//	System.out.println(Integer.toString(randomInteger));
		return randomInteger;
	}

	private static void generateQuery(String fqdn) throws IOException, SocketException {
		
		int randomInteger = generateRandom();
		
		byte[] qID = new byte[] { (byte) ((randomInteger >> 8) ), (byte) (randomInteger & 0x00ff) };
		
		byte[] outputBuffer = generateQuery(fqdn, qID);
		
		sendQuery(fqdn, randomInteger, outputBuffer);
		
	}

	private static void sendQuery(String fqdn, int randomInteger, byte[] outputBuffer)
			throws SocketException, IOException {
		
		byte[] inputBuffer = sendPacket(outputBuffer);
		
		DNSResponse response = new DNSResponse(inputBuffer, inputBuffer.length, randomInteger, fqdnOriginal);
		
		int reQuery = response.dumpResponse(rootNameServer, fqdn, tracingOn);
		
		if (reQuery == 1) {
			String[] nextQuery = response.getNextQuery(1);
			rootNameServer = InetAddress.getByName(nextQuery[4]);
			generateQuery(fqdn);
		}
		
		else if (reQuery == 2) {
			String[] nextQuery = response.getNextQuery(2);
			rootNameServer = rootNameServerOriginal;
			fqdn = nextQuery[4];
			generateQuery(fqdn);
		}
		
		else if (reQuery == 3) {
			String[] nextQuery = response.getNextQuery(3);
			rootNameServer = rootNameServerOriginal;
			fqdn = nextQuery[4];
			generateQuery(fqdn);
		}
		
	}

	private static byte[] sendPacket(byte[] outputBuffer) throws SocketException, IOException {
		
		DatagramSocket socket = new DatagramSocket();
		
		DatagramPacket packet = new DatagramPacket(outputBuffer, outputBuffer.length, 
												   rootNameServer, portNumber);
		
		socket.connect(rootNameServer, portNumber);
		socket.send(packet);
		
		byte[] inputBuffer = new byte[1024];
		DatagramPacket response = new DatagramPacket(inputBuffer, inputBuffer.length);
		
		socket.setSoTimeout(5000);
		socket.receive(response);
		
	//	System.out.println(Arrays.toString(inputBuffer));
		
		while((outputBuffer[0] != inputBuffer[0]) && (outputBuffer[1] != inputBuffer[1])) {
			
			response = new DatagramPacket(inputBuffer, inputBuffer.length);
			socket.setSoTimeout(5000);
			socket.receive(response);
			
		}
		
		socket.close();
		
		return inputBuffer;
		
	}

	private static byte[] generateQuery(String fqdn, byte[] qID) throws IOException {
		// Header Section
		
		// The rest of the header
		byte[] rest = new byte[] { (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x00, 
								  (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00 };
		
		byte[] header = new byte[qID.length + rest.length];
		System.arraycopy(qID, 0, header, 0, qID.length);
		System.arraycopy(rest, 0, header, qID.length, rest.length);
		
	//	System.out.println(Arrays.toString(header));

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
		
	//	System.out.println(Arrays.toString(query));
		
		return query;
	}
	
	public static String getFQDN() {
		return fqdnOriginal;
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


