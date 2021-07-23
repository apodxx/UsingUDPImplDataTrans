/*
@author apodx
 */
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.*;

class UDPClient {
    public static void main(String args[]) throws Exception
    {
        DatagramSocket clientSocket = new DatagramSocket();
//        String ip = args[0];
//        int port = Integer.parseInt(args[1]);
//        String fileName = args[2];
//        double d =Double.parseDouble(args[3]);
        String ip = "127.0.0.1";
        int port = 9876;
        String fileName = "TestFile.html";
        double d =0.5;
        InetAddress IPAddress = InetAddress.getByName(ip);
        byte[] sendData;
        byte[] receiveData = new byte[1024];

        String sentence = "GET "+fileName+" HTTP/1.0";

        sendData = sentence.getBytes();

        DatagramPacket sendPacket =
                new DatagramPacket(sendData, sendData.length, IPAddress, port);

        clientSocket.send(sendPacket);

        DatagramPacket receivePacket =
                new DatagramPacket(receiveData, receiveData.length);
        ArrayList<byte[]> byteList = new ArrayList<>();
        ArrayList<String> rawData = new ArrayList<>();
        try {
            while (receivePacket.getLength()!=0){

                clientSocket.receive(receivePacket);

                byte[]   data  =  receivePacket.getData();
                String modifiedSentence =
                        new String(data,0,data.length);
                byteList.add(new String(data,138,1024-138-1).getBytes());
                rawData.add(modifiedSentence);
                clientSocket.setSoTimeout(2000);

            }
//            while (true){
//                for(int i =0;i<receivePacket.getLength();i++){
//                    receivePacket[i]=0;
//                }
//                clientSocket.receive(receivePacket);
//
//                byte[]   data  =  receivePacket.getData();
//                String modifiedSentence =
//                        new String(data,0,data.length);
//                byteList.add(new String(data,138,1024-138-1).getBytes());
//                rawData.add(modifiedSentence);
//                clientSocket.setSoTimeout(2000);
//
//            }
        }catch (Exception e){

        }
        ArrayList<Integer> checkList = new ArrayList<>();
        for (String str :rawData) {
            int checkNum = Integer.parseInt(str.split("checkNum:")[1].substring(0,7));
            checkList.add(checkNum);
        }
        ArrayList<byte[]> damagePackets = Germlin(byteList,d);
//        for (byte[] b: damagePackets) {
//            System.out.println(dataCheck(b)+"");
//        }
        for(int i =0;i<byteList.size();i++){
            byte[] damagePacketData= damagePackets.get(i);
            int clientCheck = dataCheck(damagePacketData);
            int serverCheck = checkList.get(i);
//            System.out.print(clientCheck+"\t"+serverCheck+"\t");

            if(clientCheck==serverCheck){
                System.out.println("Congratulation!");
                System.out.println("The packet is correct");
                System.out.println("The sequence number of the current package is "+(i+1));
                System.out.println();
                System.out.println("The message content :"+new String(damagePacketData));
                System.out.println("--------------I'm the dividing line--------------------");
            }else {
                System.out.println("Sorry!");
                System.out.println("The packet is error");
                System.out.println("The sequence number of the current package is "+(i+1)+"");
                System.out.println();
                System.out.println("The message content :"+new String(damagePacketData));

                System.out.println("--------------I'm the dividing line--------------------");
            }
        }
        String fileName_receive = fileName.split("[.]")[0]+"_receive";
        String fileSuffix_receive=fileName.split("[.]")[1];
        InputStream is;
        OutputStream out = new FileOutputStream(fileName_receive+"."+fileSuffix_receive);
        for (byte[] b: damagePackets) {
            is = new ByteArrayInputStream(b);
            byte[] buff = new byte[1024];
            int len = 0;
            while((len=is.read(buff))!=-1){
                out.write(buff, 0, len);
            }

        }

        out.close();



    }
    public static int dataCheck(byte[] bytes){


        int i =0;
        for (int j = 0; j < bytes.length; j++) {
            i+=bytes[j];
        }

        return i;
    }
    /**
     * @autor apodx
     * @param bytesList packet
     * @param d  Probability of packet destruction
     */
    public static ArrayList<byte[]> Germlin(ArrayList<byte[]> bytesList,double d){
        int len = bytesList.size();
//       the number of all damage  packet
        int damagePacket = (int)(d*len);
//        damage One Byte Num
        int damageOneByteNum=(int)(d*len*0.5);
//        damage TwoByte Num
        int damageTwoByteNum=(int)(d*len*0.3);
//        damage Three Byte Num
        int damageThreeByteNum=(int)(d*len*0.2);

        Set<Integer> randomNum = new HashSet<Integer>();
//4 1 3 9
        while (randomNum.size()<damagePacket){
            Random random = new Random();
            int i = random.nextInt(len);
            randomNum.add(i);
        }
        int i = 1 ;
        Iterator<Integer> it = randomNum.iterator();

        while (it.hasNext()){

            byte[] packetContent = bytesList.get(it.next());

            int packetLength= packetContent.length;

            int random = randomInt(138,packetLength);

            if(i<=damageOneByteNum){
                packetContent[random] = 1;

            }
            if(i>damageOneByteNum&&i<=(damageOneByteNum+damageTwoByteNum)){
                if(random+1>packetLength-1){
                    random = random-1;
                }
                packetContent[random] = 2;
                packetContent[random+1] = 2;
            }
            if(i>(damageOneByteNum+damageTwoByteNum)&&i<=(damageOneByteNum+damageTwoByteNum+damageThreeByteNum)){
                if(random+2>packetLength-1){
                    random = random-2;
                }
                packetContent[random] = 3;
                packetContent[random+1] = 3;
                packetContent[random+2] = 3;

            }
            i++;
        }
        return bytesList;

    }
//    Generates a random number in the specified range
    private static int randomInt(int min, int max){
        return new Random().nextInt(max)%(max-min+1) + min;
    }
}
