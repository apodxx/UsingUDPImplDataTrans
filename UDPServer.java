import java.io.*;

import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
/*
@author apodx
 */
class UDPServer {
    public static void main(String args[]) throws Exception
    {
        DatagramSocket serverSocket = new DatagramSocket(9876);

        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket =
                new DatagramPacket(receiveData, receiveData.length);
        serverSocket.receive(receivePacket);
        String sentence = new String(receivePacket.getData());
        InetAddress IPAddress = receivePacket.getAddress();
        int port = receivePacket.getPort();
        String fileName = sentence.split(" ")[1];
        sendMessage("src/"+fileName,IPAddress,port,serverSocket);
//        sendMessage(fileName,IPAddress,port,serverSocket);
    }

    public static void sendMessage(String fileName,InetAddress IPAddress,int port,DatagramSocket serverSocket) throws IOException {
        FileInputStream fis = null;
        File file = new File(fileName);
        int fileLength= (int) file.length();
        byte[] sendData ;

        try {
            fis = new FileInputStream(file);
            byte[] buf = new byte[1024-138-1];
            byte[] last = new byte[]{0};
            int length = 0;
            int i =1;

            while ((length = fis.read(buf)) != -1) {
                System.out.println("Sending "+ i+" packet...");
                StringBuilder sb=new StringBuilder();
                sb.append("HTTP/1.0 200 Document Follows\\r\\n");
                sb.append("Content-Type: text/plain\\r\\n");
                sb.append("Content-Length: "+String.format("%04d", length)+"\\r\\n");
                sb.append("seq:"+String.format("%04d", i)+"\\r\\n");
                sb.append("sum:"+String.format("%04d", (fileLength/1000)+1)+"\\r\\n");
                sb.append("checkNum:"+String.format("%07d",dataCheck(buf))+"\\r\\n");
                sb.append("data:\\r\\n");
                sb.append(new String(buf,0,buf.length));
                sb.append(new String(last,0,1));
                sendData = sb.toString().getBytes();

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress,port);
                serverSocket.setSoTimeout(1000);
                serverSocket.send(sendPacket);
                i++;
                System.out.println("The "+i+" packet was sent successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    public static int dataCheck(byte[] bytes){


            int i =0;
            for (int j = 0; j < bytes.length; j++) {
                i+=bytes[j];
            }

        return i;
    }




}
