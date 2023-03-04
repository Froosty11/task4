import tcpclient.TCPClient;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ConcHTTPAsk {

    public static void main( String[] args) throws IOException {

        ServerSocket ss = null;
        try {
            ss = new ServerSocket(Integer.parseInt(args[0]));
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
        int id = 0;
            while(ss != null){
                System.out.printf("Client %d", id++);
                Socket sock = ss.accept();
                (new Thread(new RunnableAsk(sock), String.valueOf(id))).start();

            }
    }

}

