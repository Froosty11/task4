import tcpclient.TCPClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class RunnableAsk implements Runnable{
    Socket sock;
    int buffer_size = 2048;
    int amount_of_buffers = 0;
    String hostname = "";
    Integer data_limit;
    Integer time_limit;
    Integer port;
    String sendString;
    boolean shutdown = false;
    public RunnableAsk(Socket sock){
        this.sock = sock;
        hostname = null;
        data_limit = null;
        port = null;
        time_limit = null;
        sendString = null;
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

            if (s.startsWith("GET /ask?")) {
                s = s.substring(9);
                if (s.length() > 5 && s.substring(s.length()-9).equals("HTTP/1.1\r")) {
                    setParameters(s.substring(0, s.length()-10));
                    if (hostname != null && port != null) {
                        TCPClient tcpClient = new TCPClient(shutdown, time_limit, data_limit);
                        if (sendString == null) sendString = "";
                        try {
                            received = tcpClient.askServer(hostname, port, sendString);
                            recString = new String(received, StandardCharsets.UTF_8);
                            recString = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length:" + recString.length() + "\r\n\r\n" + recString;
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
            } else if(s.startsWith("GET")) {
                notFoundResponse(sock, "\n");
            }
            else{
                badRequestResponse(sock);
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
    private byte[] receiveRequest(Socket cSocket) throws IOException{
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
    private void notFoundResponse(Socket sock, String msg) throws IOException{
        //OUTPUT incase not asking
        OutputStream ouStream = sock.getOutputStream();
        String s = "404 Not found. This file could not be found. /ask correctly " + msg;
        String httpResponse = "HTTP/1.1 404 Not Found\r\nContent-Type: text/html\r\nContent-Length:" + s.length() + "\r\n\r\n" + s;
        ouStream.write(httpResponse.getBytes(StandardCharsets.UTF_8));
        ouStream.flush();
        ouStream.close();
    }
    /**
     * Premade bad req. respnse. Responds with a quick error for lots of reasons.
     * @param sock Socket that should be sent back on
     * @throws IOException Incase the socket is closed etc.
     */
    private void badRequestResponse(Socket sock)throws IOException{
        OutputStream ouStream = sock.getOutputStream();
        String s = "400 Bad request. /ask format incorrect. Please read documentation.";
        String httpResponse = "HTTP/1.1 400 Bad request\r\nContent-Type: text/html\r\nContent-Length:" + s.length() + "\r\n\r\n" + s;
        ouStream.write(httpResponse.getBytes(StandardCharsets.UTF_8));
        ouStream.flush();
        ouStream.close();
    }

    /**
     * Could've used URL parsing that already exists but want to give my self a challenge
     * @param parameters
     */
    private void setParameters(String parameters) {
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
