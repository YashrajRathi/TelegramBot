import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.awt.AWTException;

public class TelegramHackerClient {

    static String mainAddress = "";
    static final char separator = '%';
    static SocketChannel instanceSocketChannel;
    static ServerSocket instanceOfServerSocket;
    static ServerSocketChannel serverSocketChannel;

    public static void main(String args[]){


        if(args.length == 0){
            mainAddress = System.getProperty("user.home") + File.separatorChar + "TelegramHacker" + File.separatorChar;
        }else if(args.length ==1){
            System.out.println(args[0]);
            mainAddress = args[0];
        }else if(args.length >1){
            System.out.println("You have entered more than 1 arguments which is not needed");
            return;
        }

        File ff = new File(mainAddress);
        if (ff.exists()){
            System.out.println("Your files would be saved to " + mainAddress);
        }
        else if (ff.mkdir()){
            System.out.println("Your files would be saved to " + mainAddress);
        }else{
            System.out.println("You have entered some wrong address...please check again...");
            return ;
        }

        while (true) {
            receiveFile();
        }

    }

    static boolean createOpenSocketChannel(int port){
        instanceSocketChannel = null;

        try {
            bindAddress(port);
            instanceSocketChannel = serverSocketChannel.accept();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    static void bindAddress(int port){
        if (instanceOfServerSocket == null){
            try{
                serverSocketChannel = ServerSocketChannel.open();
                instanceOfServerSocket = serverSocketChannel.socket();
                instanceOfServerSocket.bind(new InetSocketAddress(port));
                System.out.print("I have bound this address.");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    static void unBindAddress(){
        try{
            instanceOfServerSocket.close();
            instanceOfServerSocket = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    static void closeSocketChannel(){
        try{
            instanceSocketChannel.close();
            unBindAddress();
            System.out.println("Socket Channel closed from sender side");
            instanceSocketChannel = null;
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    static void receiveFile(){

        if (createOpenSocketChannel(9001)){
            String info[] = getInfo();
            System.out.println("Info length received is " + info.length);

            if (info.length < 3 ){
                System.out.println("Error!!!");
                return;
            }

            System.out.println("Reached here");
            new File(mainAddress).mkdir();

            String fileAddress = mainAddress + File.separatorChar + info[1];
            RandomAccessFile aFile = null;
            File ff = new File(fileAddress);

            try {
                ff.createNewFile();


                aFile = new RandomAccessFile(fileAddress,"rw");
                FileChannel inChannel = aFile.getChannel();

                ByteBuffer bb = ByteBuffer.allocate(8192);
                System.out.println("Going to read the file from sender");
                while(instanceSocketChannel.read(bb) != -1){
                    bb.flip();
                    inChannel.write(bb);
                    bb.clear();
                }

				try{sq.displayTray("" + ff.getName(),"File has been received!");}catch(AWTException AWTexp){AWTexp.printStackTrace();}
                aFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            closeSocketChannel();
        }

    }

    static String[] getInfo(){
        int ind = 2;
        String str = "";
        String ret[] = new String[3];
        try {
            ByteBuffer bb = ByteBuffer.allocate(1024);
            instanceSocketChannel.read(bb);
            bb.flip();
            byte readableInfo[] = bb.array();
            byte cc;

            System.out.println("My received info is " + new String(readableInfo));
            System.out.println("I take separator as " + separator);
            System.out.println("I take my value as " + (char)readableInfo[0]);
            if (readableInfo[0]==separator && readableInfo[1]==separator){

                while((cc = readableInfo[ind]) != separator){

                    str = str + (char)cc;
                    ind++;

                }

                ind++;
                ret[0] = str;
                str = "";
            }else{
                System.out.print("Exited getInfo from 2");
                return new String[1];
            }

            if (readableInfo[ind] == separator){

                ind++;
                while((cc = readableInfo[ind]) != separator){
                    str = str + (char)cc;
                    ind++;
                }
                ind++;
                ret[1] = str;
                str = "";
            }

            if (readableInfo[ind] == separator){

                ind++;
                while((cc = readableInfo[ind]) != separator){
                    str = str + (char)cc;
                    ind++;
                }

                ind++;
                ret[2] = str;
                str = "";
            }
            System.out.println((char)readableInfo[ind]);
            System.out.println((char)readableInfo[ind+1]);
            System.out.println((char)readableInfo[ind+2]);

            if (readableInfo[ind] == separator && readableInfo[ind + 1] == separator && readableInfo[ind + 2] == separator){

                ByteBuffer temp = ByteBuffer.allocate(1024);
                temp.put(("" + (char)separator+ (char)separator+ (char)separator+ (char)separator).getBytes());
                temp.flip();
                instanceSocketChannel.write(temp);
                temp.clear();
                System.out.println("Received info is :");
                int len = ret.length;
                for (int i=0;i<len;i++){
                    System.out.println(ret[i]);
                }
                System.out.print("Exited getInfo from 3");
                return ret;
            }

            return ret;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new String[2];
    }
}
