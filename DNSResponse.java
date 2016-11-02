
import java.net.InetAddress;



// Lots of the action associated with handling a DNS query is processing 
// the response. Although not required you might find the following skeleton of
// a DNSreponse helpful. The class below has bunch of instance data that typically needs to be 
// parsed from the response. If you decide to use this class keep in mind that it is just a 
// suggestion and feel free to add or delete methods to better suit your implementation as 
// well as instance variables.



public class DNSResponse {
    
	// Variables for decoded bytes
	public static byte[] queryIDBytes = new byte[2];
    public static byte[] indicationBytes = new byte[1];
    public static byte[] rcodeBytes = new byte[1];
    public static byte[] queryCountBytes = new byte[2];
    public static byte[] answerCountBytes = new byte[2];
    public static byte[] nameServerBytes = new byte[2];
    public static byte[] additionalRecordBytes = new byte[2];
	
	// Variables for decoding
	public static boolean isResponse;
	public static boolean isAuthoratative;
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

	void dumpResponse() {
		


	}

    // The constructor: you may want to add additional parameters, but the two shown are 
    // probably the minimum that you need.

	public DNSResponse (byte[] data, int len) {
		
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
		
		queryIDBytes[0] = responseHeader[0];
		queryIDBytes[1] = responseHeader[1];
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
        isAuthoratative = (((indicationBytes[0] & 0x4) >> 2) == 1);
        isRecursionCapable = (((rcodeBytes[0] & 0x80) >> 7) == 1);
        errorFound = (rcodeBytes[0] & 0xF);
        queryCount = (((int) queryCountBytes[0]) * 16) + (int)queryCountBytes[1];
        answerCount = (((int) answerCountBytes[0]) * 16) + (int)answerCountBytes[1];
        extraInfoCount = (((int) additionalRecordBytes[0]) * 16) + (int)additionalRecordBytes[1];
        nsCount = (((int) nameServerBytes[0]) * 16) + (int)nameServerBytes[1];

	    // Extract list of answers, name server, and additional information response 
	    // records
	}


    // You will probably want a methods to extract a compressed FQDN, IP address
    // cname, authoritative DNS servers and other values like the query ID etc.


    // You will also want methods to extract the response records and record
    // the important values they are returning. Note that an IPV6 reponse record
    // is of type 28. It probably wouldn't hurt to have a response record class to hold
    // these records. 
}


