import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class Server {

    //clients list
    static ArrayList<Client> clients = new ArrayList<>();

    public static class Client extends Thread {
        String address;
        Queue<String> commands = new LinkedList<>();
        Process process;
        BufferedWriter write;
        BufferedReader reader;
        InetAddress clientAddress;
        int clientPort;

        Thread thread = new Thread(()->{
            try {
                //socket for reply to server
                DatagramSocket sock = new DatagramSocket();
                do{
                    if(commands.size()>0){
                        //write command to process cmd
                        write.write(commands.remove() + '\n');
                        write.flush();

                        Thread.sleep(500);
                        //read command result
                        StringBuilder result = new StringBuilder();
                        while(reader.ready()){
                            char out = (char) reader.read();
                            result.append(out);
//                            System.out.print(out);
                        }
                        String replyMessage = result.toString();

                        //reply to Client
                        DatagramPacket replyPacket = new DatagramPacket(replyMessage.getBytes(),
                                replyMessage.getBytes().length, clientAddress, clientPort);
                        sock.send(replyPacket);
                    }
//                    Thread.sleep(100);
                }while (!process.waitFor(200, TimeUnit.MILLISECONDS));

                if(process.exitValue()==0){
                    System.out.println("Zakończono proces, usuwam klienta z listy");
                    clients.remove(this);
                    Thread.currentThread().interrupt();
                }else{
                    System.out.println(process.exitValue());
                }

            } catch (IOException | InterruptedException e) {
                System.err.println("Exception: " + e);
            }
        });
        public Client(String address, Process process, InetAddress clientAddress, int clientPort){
            this.clientAddress = clientAddress;
            this.clientPort = clientPort;
            this.address = address;
            this.process = process;
            this.write = new BufferedWriter(
                    new OutputStreamWriter(this.process.getOutputStream()));
            this.reader = new BufferedReader(
                    new InputStreamReader(this.process.getInputStream()));
            this.thread.start();
        }
        //add command to queue method
        public void setCommand(String command){
            this.commands.add(command);
        }
    }

    public static void main(String[] args)throws SocketException {
        try{

            //Creating server socket, port 45000
            DatagramSocket sock = new DatagramSocket(45000);
            System.out.println("Serwer Utworzony...");

            //communication loop
            while(true){
                //buffer and datagrampacket to receive incoming data
                byte[] buffer = new byte[65536];DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
                //receive packet
                sock.receive(incomingPacket);
                byte[] incomingData = incomingPacket.getData();
                String incomingMessage = new String(incomingData, 0, incomingPacket.getLength());

                //print message info
                System.out.println("Klient: " + incomingPacket.getAddress().getHostAddress() +
                        ":" + incomingPacket.getPort() + " Message: " + incomingMessage);

                //check if clients contains incomingPacket client
                boolean foundClient = false;
                if(!clients.isEmpty()){
                    for(int i=0;i<clients.size();i++){
                        if(clients.get(i).address.equals(incomingPacket.getSocketAddress().toString())){
                            System.out.println("Znaleziono klienta:" + clients.get(i).address);
                            foundClient = true;
                            clients.get(i).setCommand(incomingMessage);
                            break;
                        }
                    }
                }else{
                    System.out.println("Dodawanie klienta do listy: " +
                            incomingPacket.getSocketAddress().toString());
                    Process process = Runtime.getRuntime().exec("cmd");
                    Client client = new Client(incomingPacket.getSocketAddress().toString(),
                            process, incomingPacket.getAddress(), incomingPacket.getPort());
                    client.setCommand(incomingMessage);
                    clients.add(client);
                    foundClient = true;
                }
                if(!foundClient){
                    System.out.println("Dodawanie klienta do listy: " + incomingPacket.getSocketAddress().toString());
                    Process process = Runtime.getRuntime().exec("cmd");
                    Client client = new Client(incomingPacket.getSocketAddress().toString(),
                            process, incomingPacket.getAddress(), incomingPacket.getPort());
                    client.setCommand(incomingMessage);
                    clients.add(client);
                }
            }
        } catch (IOException e) {
            System.err.println("Exception: " + e);
        }
    }
}