import tcpclient.TCPClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RunnableAsk implements Runnable{
    Socket sock;
    int buffer_size = 2048;
    String hostname;
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
            String request = receiveRequest(this.sock);
            String[] lines = request.split("\n");
            //trim the string to remove ask and http ending info
            request = lines[0];

            if (request.startsWith("GET /ask?")) {
                request = request.substring(9);
                if (request.endsWith("HTTP/1.1\r")) {
                    setParameters(request.substring(0, request.length()-10));
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
            } else if(request.startsWith("GET")) {
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
    private String receiveRequest(Socket cSocket) throws IOException{
        InputStream in = cSocket.getInputStream();
        byte[] buffer = new byte[buffer_size];
        StringBuilder reply = new StringBuilder();
        int read;
        while (!cSocket.isClosed() && (read = in.read(buffer)) != -1)
        {
            reply.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
            if (reply.toString().endsWith("\n")) {
                break;
            }
        }
        return reply.toString();
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
     * @param parameters for the fucking thing
     */
    private void setParameters(String parameters) {
        String[] par = parameters.split("&");
        for (String s :
                par) {
            if (s.contains("=")) {
                String[] oneParameter = s.split("=");
                switch (oneParameter[0]) {
                    case "hostname": hostname = oneParameter[1]; break;
                    case "limit": data_limit = Integer.parseInt(oneParameter[1]); break;
                    case "port": port = Integer.parseInt(oneParameter[1]); break;
                    case "string": sendString = oneParameter[1]; break;
                    case "shutdown": shutdown = Boolean.parseBoolean(oneParameter[1]); break;
                    case "timeout": time_limit = Integer.parseInt(oneParameter[1]); break;
                }
            }
        }
    }
}
