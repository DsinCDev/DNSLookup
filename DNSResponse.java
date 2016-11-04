
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;



// Lots of the action associated with handling a DNS query is processing 
// the response. Although not required you might find the following skeleton of
// a DNSreponse helpful. The class below has bunch of instance data that typically needs to be 
// parsed from the response. If you decide to use this class keep in mind that it is just a 
// suggestion and feel free to add or delete methods to better suit your implementation as 
// well as instance variables.



public class DNSResponse {
    
	private int byteNo = 0;
	private int queryID;
	private int responseID;
	
	private ArrayList<String> answerList = new ArrayList<String>();
    
    private String aaFQDN;
	
	// Variables for decoding
	public static boolean isResponse;
	public static boolean isAuthoritative;
    public static boolean isRecursionCapable;
    public static int errorFound;
    public static int queryCount;
    public static int answerCount;
    public static int extraInfoCount;
    public static int nsCount; 
    
    private boolean decoded = false;      // Was this response successfully decoded
    private int additionalCount = 0;      // number of additional (alternate) response records
    private boolean authoritative = false;// Is this an authoritative record

    // Note you will almost certainly need some additional instance variables.

    // When in trace mode you probably want to dump out all the relevant information in a response

	void dumpResponse(InetAddress rootNameServer, String fqdn, boolean tracingOn) {
		
		if (tracingOn) {
			
		System.out.println("\n\nQuery ID     " + queryID + " " + fqdn + " --> " + rootNameServer);
		System.out.println("Response ID: " + responseID + " Authoritative " + isAuthoritative);
		System.out.println("Answers (" + answerCount + ")");
		}

	}

    // The constructor: you may want to add additional parameters, but the two shown are 
    // probably the minimum that you need.

	public DNSResponse (byte[] data, int len, int randomInteger) {
		
		queryID = randomInteger;
		
		byte[] responseHeader = new byte[12];
		int responseLength = data.length;
		int bodyCounter = 0;
		byte[] responseBody = new byte[responseLength - 12];
		
		for (int i = 0; i < 12; i++) {
			responseHeader[i] = data[i];
		}
		
		for (int i = 12; i < responseLength; i++) {
			responseBody[bodyCounter] = data[i];
			bodyCounter++;
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
        extraInfoCount = (((int) additionalRecordBytes[0]) * 16) + (int)additionalRecordBytes[1];
        nsCount = (((int) nameServerBytes[0]) * 16) + (int)nameServerBytes[1];

	    // Extract list of answers, name server, and additional information response 
	    // records
        
        aaFQDN = getFQDN(responseBody);
        byteNo += 4; // Ignore Qtype and Qclass
    //  System.out.println(responseBody[byteNo]);
        
        getAnswerRecord(responseBody);
        
	}
	
	private void getAnswerRecord(byte[] responseBody) {
		
		for (int i = 0; i < answerCount; i++) {
			answerList.add(getAnswers(responseBody));
			
			int type = (responseBody[byteNo++] << 8) &0xFFFF | responseBody[byteNo++];
			System.out.println("Type: " + type);
			
			int rclass = responseBody[byteNo++] << 8 | responseBody[byteNo++];
			System.out.println("Rclass: " + rclass);
			
			int ttl = 0;
			for (int j= 0; j < 4; j++) {
				System.out.println(responseBody[byteNo]);
				ttl = ttl << 8;
				ttl |= (responseBody[byteNo++] & 0xFF);
			}
			System.out.println("TTL: " + ttl);
			
			int rdlength = responseBody[byteNo++] << 8 | responseBody[byteNo++];
			System.out.println("RDLength: " + rdlength);
		}
		
	}
	
	private String getAnswers(byte[] responseBody) {
		return getFQDN(responseBody);
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

				for (int i = 0; i < cnt; i++) {
					fqdn = fqdn + (char) data[offset++];
				}

			}
		} catch (Exception e) {
			System.out.println("Something is wrong");
		}

		return fqdn;
	}

	private String getFQDN(byte[] responseBody) {
		String fqdn = new String();
		boolean firstTime = true;
		try {
			for (int cnt = (responseBody[byteNo++] &0xff); cnt != 0; cnt = (responseBody[byteNo++] & 0xff)) {
				
		//		System.out.println(cnt);
				if (!firstTime) { 
					fqdn += '.';
				} 
				
				else {
					firstTime = false;
				}

				
				if ((cnt & 0xC0) > 0) {
					cnt = (cnt &0x3f) << 8 | responseBody[byteNo++] & 0xff;
					fqdn = getCompressedFQDN(fqdn, responseBody, cnt - 12);
					break;
				}
				
				else {
					for (int i = 0; i < cnt; i++) {
						fqdn += (char) responseBody[byteNo++];
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception");
		}
		
		System.out.println(fqdn);
		return fqdn;
	}

    // You will probably want a methods to extract a compressed FQDN, IP address
    // cname, authoritative DNS servers and other values like the query ID etc.


    // You will also want methods to extract the response records and record
    // the important values they are returning. Note that an IPV6 reponse record
    // is of type 28. It probably wouldn't hurt to have a response record class to hold
    // these records. 

}

