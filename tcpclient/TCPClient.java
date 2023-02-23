package tcpclient;
import java.net.*;
import java.io.*;
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
        System.out.printf("Starting new socket connection. ");
        timeOutTime = timeout;
        buffer_size = 64;
        amount_of_buffers = 0;
        this.bool = shutdown;
        this.datalimit = datalimit;
        //this.lastAccessMS = System.currentTimeMillis();
    }
        public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {
        byte[] fromServerBuffer = new byte[buffer_size];
        ByteArrayOutputStream received = new ByteArrayOutputStream();
        System.out.printf("Connecting to %s:%s on new socket.\n", hostname, port);
            Socket cSocket = new Socket(hostname, port);
            System.out.printf("Connected. Writing %s to outputstream\n", toServerBytes);
            //for (byte b : toServerBytes) {System.out.println(b);}
            int length = 0;

            if(timeOutTime != null)
                cSocket.setSoTimeout(timeOutTime);
            cSocket.getOutputStream().write(toServerBytes, 0, toServerBytes.length);
            if(bool) cSocket.shutdownOutput();

            do {
                try{
                    length = cSocket.getInputStream().read(fromServerBuffer);
                    if(length > 0){
                        //Returns incase the max limit has already been reached.
                        if(datalimit != null && length > datalimit - received.size()) length = datalimit- received.size();
                        received.write(Arrays.copyOf(fromServerBuffer, length));
                        amount_of_buffers++;}
                    if(datalimit != null && received.size() >= datalimit ){
                        System.out.println(received.size());
                        cSocket.close();
                        System.out.printf("Datalimit of %s reached, closing connection.", datalimit );
                        return received.toByteArray();
                    }
                }
                catch (SocketTimeoutException so){
                    cSocket.close();
                    return received.toByteArray();
                }


                // Returns if the last time access is too long ago. Basically if the answers are taking too long.
                // deprecated to try and fix 2
                /*if(timeOutTime != null && System.currentTimeMillis() > lastAccessMS + timeOutTime){
                    cSocket.close();
                    System.out.printf("Timelimit of %s reached, closing connection.", timeOutTime );
                    return received.toByteArray();
                }*/
            } while(!cSocket.isClosed() && length != -1 );
            //lastAccessMS = System.currentTimeMillis();
            if(timeOutTime != null)
                cSocket.close();

            System.out.printf("%d of buffers %d buffersize sent/received.\n", amount_of_buffers, buffer_size);
            System.out.println("\n___________________DATA_RECEIVED___________________\n");
        return received.toByteArray();
        }
    public byte[] askServer(String hostname, int port) throws IOException {
        return new byte[0];
    }
    public void setBuffer_size(int newSize){    buffer_size = newSize;  }

}
