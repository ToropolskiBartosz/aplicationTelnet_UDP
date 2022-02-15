import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class Client {
    public static void main(String[] args){
        String message;
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        try{
            // creating socket and host search
            DatagramSocket sock = new DatagramSocket();
            InetAddress host = InetAddress.getByName("localhost");
            int port = 45000;
            while(true){
                //ustawienie timeouta
                sock.setSoTimeout(5000);
                try{
                    //create and send message
                    System.out.print("Wprowadź wiadomość: ");
                    message = (String)input.readLine();
                    DatagramPacket messagePacket = new DatagramPacket(message.getBytes(),
                            message.getBytes().length, host, port);
                    sock.send(messagePacket);
                    //receive reply form sever
                    byte[] buffer = new byte[65536];
                    DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
                    System.out.println("oczekiwanie na odpowiedz......");
                    sock.receive(incomingPacket);
                    //print reply info
                    byte[] incomingData = incomingPacket.getData();
                    String incomingMessage = new String(incomingData, 0, incomingPacket.getLength());
                    System.out.println("Odpowiedź od servera: " + incomingPacket.getAddress().getHostAddress() +
                            ":" + incomingPacket.getPort() + " Wiadomość:\n" + incomingMessage);

//                    if(incomingMessage.contains("exit")){
//                        System.out.println("Zamykanie klienta");
//                        break;
//                    }
                    System.out.println("##########################################################################");

                }catch(SocketTimeoutException e){
                    System.out.println("Nie można nawiązać połączenia z serwerem");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("IOException: " + e);
        }
    }
}