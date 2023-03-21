package tcpclient;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Array;
import java.util.Arrays;

public class TCPClient {
    private static int buffer_size;
    private int amount_of_buffers;
    private long lastAccessMS;
    private Integer timeOutTime;
    private Integer datalimit;
    private boolean bool;


    public TCPClient(boolean shutdown, Integer timeout, Integer datalimit){
        timeOutTime = timeout;
        buffer_size = 64;
        amount_of_buffers = 0;
        this.bool = shutdown;
        this.datalimit = datalimit;
        //this.lastAccessMS = System.currentTimeMillis();
    }
    public byte[] askServer(String hostname, int port, String toserver) throws IOException {
        byte[] fromServerBuffer = new byte[buffer_size];
        ByteArrayOutputStream received = new ByteArrayOutputStream();
        Socket cSocket = new Socket(hostname, port);
        //for (byte b : toServerBytes) {System.out.println(b);}
        int length = 0;
        if(timeOutTime != null)
            cSocket.setSoTimeout(timeOutTime);
        OutputStream ou = cSocket.getOutputStream();
        ou.write(toserver.getBytes(StandardCharsets.UTF_8));
        if(bool) cSocket.shutdownOutput();
        do {
            try{
                //fuck it i added this
                if (received.toString().endsWith("\n")) {
                    break;
                }
                InputStream input = cSocket.getInputStream();
                length = input.read(fromServerBuffer);
                if(length > 0){
                    //Returns incase the max limit has already been reached.
                    if(datalimit != null && length > datalimit - received.size()) length = datalimit- received.size();
                    received.write(Arrays.copyOf(fromServerBuffer, length));
                    amount_of_buffers++;}
                if(datalimit != null && received.size() >= datalimit ){
                    cSocket.close();
                    return received.toByteArray();
                }
            }
            catch (SocketTimeoutException so){
                cSocket.close();
                return received.toByteArray();
            }
            catch (IOException ioe){
                ioe.printStackTrace();
            }
        } while(length != -1 );
        //lastAccessMS = System.currentTimeMillis();
        if(timeOutTime != null)
            cSocket.close();
        return received.toByteArray();
    }
    public byte[] askServer(String hostname, int port) throws IOException {
        return new byte[0];
    }
    public void setBuffer_size(int newSize){    buffer_size = newSize;  }

}
