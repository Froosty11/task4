import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class HTTPAsk {
    public static void main( String[] args) throws IOException {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(Integer.parseInt(args[0]));
        }catch(IOException ioException){
            ioException.printStackTrace();
        }
            while(ss != null){
                Socket sock = ss.accept();
                System.out.println(sock.getInetAddress());
                //OUTPUT
                OutputStream ouStream = sock.getOutputStream();
                String httpResponse = """
                        HTTP/1.1 200 OK\r
                        \r
                        Hello world""";
                ouStream.write(httpResponse.getBytes(StandardCharsets.UTF_8));
                sock.close();
            }

    }
}

