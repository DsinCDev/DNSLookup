import java.net.InetAddress;
import java.util.ArrayList;



// Lots of the action associated with handling a DNS query is processing 
// the response. Although not required you might find the following skeleton of
// a DNSreponse helpful. The class below has bunch of instance data that typically needs to be 
// parsed from the response. If you decide to use this class keep in mind that it is just a 
// suggestion and feel free to add or delete methods to better suit your implementation as 
// well as instance variables.



public class DNSResponse {
    
	private int byteNo = 12;
	private int queryID;
	private int responseID;
	private String fqdnOriginal;
	
	private String aaFQDN;
	
	private ArrayList<String[]> answerList = new ArrayList<String[]>();
	private ArrayList<String[]> nsList = new ArrayList<String[]>();
	private ArrayList<String[]> additionalList = new ArrayList<String[]>();
	
	// Variables for decoding
	public static boolean isResponse;
	public static boolean isAuthoritative;
    public static boolean isRecursionCapable;
    public static int errorFound;
    public static int queryCount;
    public static int answerCount;
    public static int additionalCount;
    public static int nsCount; 
    
    private boolean decoded = false;      // Was this response successfully decoded

    // Note you will almost certainly need some additional instance variables.

    // When in trace mode you probably want to dump out all the relevant information in a response

	int dumpResponse(InetAddress rootNameServer, String fqdn, boolean tracingOn) {
		
		if (tracingOn) {
			
			System.out.println("\n\nQuery ID     " + queryID + " " + fqdn + " --> " + rootNameServer.toString().substring(1));
			System.out.println("Response ID: " + responseID + " Authoritative " + isAuthoritative);
			
			System.out.println("  Answers (" + answerCount + ")");
			for (int i = 0; i < answerCount; i++) {
				String[] answerArray = answerList.get(i);
				String recordType = getRecordType(answerArray[1]);
				System.out.format("       %-30s %-10s %-4s %s\n", 
								  answerArray[0], answerArray[3], 
								  recordType, answerArray[4]);
			}
			
			System.out.println("  Nameservers (" + nsCount + ")");
			for (int i = 0; i < nsCount; i++) {
				String[] nsArray = nsList.get(i);
				String recordType = getRecordType(nsArray[1]);
				System.out.format("       %-30s %-10s %-4s %s\n", 
								  nsArray[0], nsArray[3], 
								  recordType, nsArray[4]);
			}
			
			System.out.println("  Additional Information (" + additionalCount + ")");
			for (int i = 0; i < additionalCount; i++) {
				String[] additionalArray = additionalList.get(i);
				String recordType = getRecordType(additionalArray[1]);
				System.out.format("       %-30s %-10s %-4s %s\n", 
									additionalArray[0], additionalArray[3], 
									recordType, additionalArray[4]);
				
			}
			
		
		}
		
		if (answerCount == 0 && additionalCount == 0) {
			return 2; // reQuery with NS;
		}
		
		else if (answerCount == 0) {
			
			return 1; // Standard reQuery
		}
		
		else {
			String[] answerArray = answerList.get(0);
			if (getRecordType(answerArray[1]).contains("CN")) {
				return 3; // reQuery with CName
			}
		}
		
		// Final print
		
		for (int i = 0; i < answerCount; i++) {
			String[] answerArray = answerList.get(i);
			System.out.println(fqdnOriginal + " " + answerArray[3] + " " + answerArray[4]);
		}
		
		return 0; // Stop querying

	}
	
	public String[] getNextQuery(int value) {
		
		if (value == 1) {
			
			if (answerCount != 0) {
				return answerList.get(0);
			}
			
			else {
				return additionalList.get(0);
			}
		}
		
		else if (value == 2) {
			return nsList.get(0);
		}
		
		else {
			return answerList.get(0);
		}
	}
	
	private String getRecordType(String type) {
		int t = Integer.parseInt(type);
		if (t == 1) {
			return "A";
		}
		else if (t == 2) {
			return "NS";
		}
		else if (t == 5) {
			return "CN";
		}
		else if (t == 28) {
			return "AAAA";
		}
		else {
			return null;
		}
	}

    // The constructor: you may want to add additional parameters, but the two shown are 
    // probably the minimum that you need.

	public DNSResponse (byte[] data, int len, int randomInteger, String fqdnOriginal) {
		this.fqdnOriginal = fqdnOriginal;
		queryID = randomInteger;
		
		byte[] responseHeader = new byte[12];
		int responseLength = data.length;
		
		for (int i = 0; i < 12; i++) {
			responseHeader[i] = data[i];
		}
		
		// Variables for decoded bytes
		byte[] responseIDBytes = new byte[2];
	    byte[] indicationBytes = new byte[1];
	    byte[] rcodeBytes = new byte[1];
	    byte[] queryCountBytes = new byte[2];
	    byte[] answerCountBytes = new byte[2];
	    byte[] nameServerBytes = new byte[2];
	    byte[] additionalRecordBytes = new byte[2];
		
		responseIDBytes[0] = responseHeader[0];
		responseIDBytes[1] = responseHeader[1];
		
		responseID = responseIDBytes[1] & 0x00ff | (responseIDBytes[0] & 0x00ff) << 8;
		
		if (queryID != responseID) {
			System.out.println("The query ids do not match.");
			return;
		}
		
	//	System.out.println(Integer.toString(queryID));
		
		indicationBytes[0] = responseHeader[2];
        rcodeBytes[0] = responseHeader[3];
        queryCountBytes[0] = responseHeader[4];
        queryCountBytes[1] = responseHeader[5];
        answerCountBytes[0] = responseHeader[6];
        answerCountBytes[1] = responseHeader[7];
        nameServerBytes[0] = responseHeader[8];
        nameServerBytes[1] = responseHeader[9];
        additionalRecordBytes[0] = responseHeader[10];
        additionalRecordBytes[1] = responseHeader[11];
        
        isResponse = (((indicationBytes[0] & 0x80) >> 7) == 1);
        isAuthoritative = (((indicationBytes[0] & 0x4) >> 2) == 1);
        isRecursionCapable = (((rcodeBytes[0] & 0x80) >> 7) == 1);
        errorFound = (rcodeBytes[0] & 0xF);
        queryCount = (((int) queryCountBytes[0]) * 16) + (int)queryCountBytes[1];
        answerCount = (((int) answerCountBytes[0]) * 16) + (int)answerCountBytes[1];
        additionalCount = (((int) additionalRecordBytes[0]) * 16) + (int)additionalRecordBytes[1];
        nsCount = (((int) nameServerBytes[0]) * 16) + (int)nameServerBytes[1];

	    // Extract list of answers, name server, and additional information response 
	    // records
        
        aaFQDN = getFQDN(data);
        byteNo += 4; // Ignore Qtype and Qclass
    //  System.out.println(data[byteNo]);
        
        getResponseRecord(data);
        
        decoded = false;
        
	}
	
	private void getResponseRecord(byte[] data) {
		
		for (int i = 0; i < answerCount; i++) {
			answerList.add(getResult(data));
		}
			
		for (int j = 0; j < nsCount; j++) {
			nsList.add(getResult(data));
		}
		
		for (int k = 0; k < additionalCount; k++) {
			additionalList.add(getResult(data));
		}
		
		decoded = true;
				
	}
		

	private String[] getResult(byte[] data) {
		String[] result = new String[5];
		result[0] = getFQDN(data);
		
		int type = (data[byteNo++] << 8) &0xFFFF | data[byteNo++];
		result[1] = Integer.toString(type);
		//	System.out.println("Type: " + type);
		
		int rclass = data[byteNo++] << 8 | data[byteNo++];
		result[2] = Integer.toString(rclass);
		//	System.out.println("Rclass: " + rclass);
		
		int ttl = 0;
		for (int j= 0; j < 4; j++) {
		//	System.out.println(data[byteNo]);
			ttl = ttl << 8;
			ttl |= (data[byteNo++] & 0xFF);
		}
		result[3] = Integer.toString(ttl);
	//	System.out.println("TTL: " + ttl);
		
		int rdlength = data[byteNo++] << 8 | data[byteNo++];
	//	System.out.println("RDLength: " + rdlength);
		
		
		result[4] = generateResult(data, rclass, type);
		
		return result;
	}

	private String generateResult(byte[] data, int rclass, int type) {
		InetAddress ipAddress = null;
		
		if (rclass == 1) {
			
			if (type == 1) { // result is IPV4 address
				
				byte ip4Address[] = new byte[4];
				
				for (int i = 0; i < 4; i++) {
					ip4Address[i] = data[byteNo++];
				}
				
				try {
					ipAddress = InetAddress.getByAddress(ip4Address);
				} catch (Exception e) {
					System.out.println("Address conversion failed");
				}
				
				return ipAddress.toString().substring(1);
				
			}
			
			else if ((type == 2) || (type == 5)) { // result is a FQDN for Nameserver and Cname
				
				String result = new String();
				result = getFQDN(data);
				return result;
			}
			
			else if (type == 28) { // result is IPV6 address
				
				byte ip6Address[] = new byte[16];
				
				for (int i = 0; i < 16; i++) {
					ip6Address[i] = data[byteNo++];
				}
				
				try {
					ipAddress = InetAddress.getByAddress(ip6Address);
				} catch (Exception e) {
					System.out.println("Address conversion failed");
				}
				
				return ipAddress.toString().substring(1);
				
			}
			
		}
		
		return null;
	}

	private String getCompressedFQDN(String fqdn, byte[] data, int offset) {
		boolean firstTime = true;

		try {
			for (int cnt = (data[offset++] &0xff); cnt != 0; cnt = (data[offset++] &0xff)) {
		//		System.out.println(cnt);
				
				if (!firstTime) {
					fqdn += '.';
				} 
				
				else {
					firstTime = false;
				}
				
				if ((cnt & 0xC0) > 0) {
					cnt = (cnt &0x3f) << 8 | data[offset++] & 0xff;
					fqdn = getCompressedFQDN(fqdn, data, cnt);
					break;
				}
				
				else {
					for (int i = 0; i < cnt; i++) {
						fqdn = fqdn + (char) data[offset++];
					}
				}

			}
		} catch (Exception e) {
			System.out.println("Error");
		}

		return fqdn;
	}

	private String getFQDN(byte[] data) {
		String fqdn = new String();
		boolean firstTime = true;
		try {
			for (int cnt = (data[byteNo++] &0xff); cnt != 0; cnt = (data[byteNo++] & 0xff)) {
				
		//		System.out.println(cnt);
				if (!firstTime) { 
					fqdn += '.';
				} 
				
				else {
					firstTime = false;
				}

				
				if ((cnt & 0xC0) > 0) {
					cnt = (cnt &0x3f) << 8 | data[byteNo++] & 0xff;
					fqdn = getCompressedFQDN(fqdn, data, cnt);
					break;
				}
				
				else {
					for (int i = 0; i < cnt; i++) {
						fqdn += (char) data[byteNo++];
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception");
		}
		
	//	System.out.println(fqdn);
		return fqdn;
	}
	

    // You will probably want a methods to extract a compressed FQDN, IP address
    // cname, authoritative DNS servers and other values like the query ID etc.


    // You will also want methods to extract the response records and record
    // the important values they are returning. Note that an IPV6 reponse record
    // is of type 28. It probably wouldn't hurt to have a response record class to hold
    // these records. 

}

