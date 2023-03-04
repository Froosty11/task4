import tcpclient.TCPClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RunnableAsk implements Runnable{
    Socket sock;
    static int buffer_size = 2048;
    static int amount_of_buffers = 0;
    static String hostname = "";
    static Integer data_limit;
    static Integer time_limit;
    static Integer port;
    static String sendString;
    static boolean shutdown = false;
    public RunnableAsk(Socket sock){
        this.sock = sock;
    }

    @Override
    public void run() {
        //GET URL FROM HTTP REQUEST
        try {
            byte[] received;
            String recString;
            byte[] request = receiveRequest(this.sock);
            String s = new String(request, StandardCharsets.UTF_8);
            String[] lines = s.split("\n");
            //trim the string to remove ask and http ending info
            s = lines[0];
            lines = s.split(" ");
            if (lines[1].contains("/ask?")) {
                if (lines.length == 3 && lines[1].length() > 5 && lines[0].equals("GET") && lines[1].substring(0, 5).contains("/ask?") && lines[2].equals("HTTP/1.1\r")) {
                    setParameters(lines[1].substring(5));
                    if (hostname != null || port != null) {
                        TCPClient tcpClient = new TCPClient(shutdown, time_limit, data_limit);
                        if (sendString == null) sendString = "";
                        try {
                            received = tcpClient.askServer(hostname, port, sendString.getBytes(StandardCharsets.UTF_8));
                            recString = new String(received, StandardCharsets.UTF_8);
                            recString = "HTTP/1.1 200 OK\r\n\r\n" + recString;
                            OutputStream ouStream = sock.getOutputStream();
                            ouStream.write(recString.getBytes(StandardCharsets.UTF_8));
                        } catch (IOException ioe) {
                            notFoundResponse(sock, "\nThe host could not be contacted. Please make sure your internet is working and url is correct.");
                        }
                    } else {
                        badRequestResponse(sock);
                    }
                } else {
                    badRequestResponse(sock);
                }
            } else {
                notFoundResponse(sock, "\n");
            }
            if (!sock.isClosed())
                sock.close();
        }
        catch (IOException ioe){
            ioe.printStackTrace();
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
        //TODO: FIX THIS FOR MORE THAN ONE PACKET
        //do {
        length = cSocket.getInputStream().read(fromServerBuffer);
        if(length > 0){
            received.write(Arrays.copyOf(fromServerBuffer, length));
            amount_of_buffers++;}
        //} while(length != -1 && !cSocket.isClosed());
        return received.toByteArray();
    }
    /**
     * Not found response. This happens incase not /ask? is defined and/or if the website requested doesnt respond.
     * @param sock Socket used to contact.
     * @throws IOException dw.
     */
    private static void notFoundResponse(Socket sock, String msg) throws IOException{
        //OUTPUT incase not asking
        OutputStream ouStream = sock.getOutputStream();
        String httpResponse = "HTTP/1.1 404 Not found\r\n\r\n 404 Not found. This file could not be found. /ask correctly" + msg;
        ouStream.write(httpResponse.getBytes(StandardCharsets.UTF_8));
        ouStream.flush();
        ouStream.close();
    }
    /**
     * Premade bad req. respnse. Responds with a quick error for lots of reasons.
     * @param sock Socket that should be sent back on
     * @throws IOException Incase the socket is closed etc.
     */
    private static void badRequestResponse(Socket sock)throws IOException{
        OutputStream ouStream = sock.getOutputStream();
        String httpResponse = "HTTP/1.1 400 Bad request\r\n\r\n 400 Bad request. /ask format incorrect. Please read documentation.";
        ouStream.write(httpResponse.getBytes(StandardCharsets.UTF_8));
        ouStream.flush();
        ouStream.close();
    }

    /**
     * Could've used URL parsing that already exists but want to give my self a challenge
     * @param parameters
     */
    private static void setParameters(String parameters) {
        String[] par = parameters.split("&");
        for (String s :
                par) {
            if (s.contains("=")) {
                String[] oneParameter = s.split("=");
                switch (oneParameter[0]) {
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
                        sendString = oneParameter[1];
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
}
