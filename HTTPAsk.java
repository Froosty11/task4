import tcpclient.TCPClient;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class HTTPAsk {
    static int buffer_size = 2048;
    static int amount_of_buffers = 0;
    static String hostname = "";
    static Integer data_limit;
    static Integer time_limit;
    static Integer port;
    static String sendString;
    static boolean shutdown = false;
    public static void main( String[] args) throws IOException {

        ServerSocket ss = null;
        try {
            ss = new ServerSocket(Integer.parseInt(args[0]));
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
            while(ss != null){
                Socket sock = ss.accept();
                //GET URL FROM HTTP REQUEST
                byte[] request = receiveRequest(sock);
                String s = new String(request, StandardCharsets.UTF_8);
                String[] lines = s.split("\n");
                //trim the string to remove ask and http ending info
                 s = lines[0];
                 lines = s.split(" ");

                if(lines[1].length() > 5 && lines[0].equals("GET") && lines[1].substring(0,5).contains("/ask?") && lines[2].equals("HTTP/1.1\r")){
                    setParameters(lines[1].substring(5));
                    if(hostname == null || port == null) break;
                    TCPClient tcpClient = new TCPClient(shutdown, time_limit, data_limit);
                    if(sendString != null)
                        tcpClient.askServer(hostname, port, sendString.getBytes());
                    else
                        tcpClient.askServer(hostname, port, null);

                 }
                else {
                    //OUTPUT
                    OutputStream ouStream = sock.getOutputStream();
                    String httpResponse = "HTTP/1.1 400 200 OK\r\n\r\n";
                    ouStream.write(httpResponse.getBytes(StandardCharsets.UTF_8));
                }

            }

    }

    /**
     * Could've used URL parsing that already exists but want to give my self a challenge
     * @param parameters
     */
    private static void setParameters(String parameters){
        String[] par = parameters.split("&");
        for (String s:
             par) {
            if(s.contains("=")){
                String[] oneParameter= s.split("=");
                System.out.println(s);
                switch (oneParameter[0]){
                    case "hostname":
                        hostname = oneParameter[1];
                    break;
                    case "limit":
                        data_limit = Integer.parseInt(oneParameter[1]);
                        break;
                    case "port":
                        port = Integer.parseInt(oneParameter[1]);
                        break;
                    case "string":
                        sendString= oneParameter[1];
                        break;
                    case "shutdown":
                        shutdown = Boolean.parseBoolean(oneParameter[1]);
                        break;
                    case "timeout":
                        time_limit = Integer.parseInt(oneParameter[1]);
                        break;
                }
            }
        }
    }
    /**
     * Basically stolen from my task 1. Simply returns the byte array that the http request is. From this I can parse URI information
     * to apply the correct options to my later request.
     * @param cSocket socket that will be checked.
     * @return bytes that come in.
     * @throws IOException can throw ioexception incase socket is closed or data limited or similar.
     */
    private static byte[] receiveRequest(Socket cSocket) throws IOException{
        byte[] fromServerBuffer = new byte[buffer_size];
        ByteArrayOutputStream received = new ByteArrayOutputStream();
        //for (byte b : toServerBytes) {System.out.println(b);}
        int length = 0;
        do {
            length = cSocket.getInputStream().read(fromServerBuffer);
            if(length > 0){
                received.write(Arrays.copyOf(fromServerBuffer, length));
                amount_of_buffers++;}
        } while(length != -1 && !cSocket.isClosed());
        return received.toByteArray();
    }
}

