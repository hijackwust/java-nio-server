package zhu.zhiwu;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class ServerThread implements Runnable {

    private SocketChannel sc;

    public ServerThread(SocketChannel sc) {
        this.sc = sc;
    }

    @Override
    public void run() {
        long tid =Thread.currentThread().getId();
        //int tidMod = (int)tid%3;

        String tidMod=System.currentTimeMillis()+
                String.format("%03d",ReceiveAndSend.rand.nextInt(20)); //以时间戳+随机数为目录
        System.out.println("线程="+tid+", tid%3="+tidMod);
        try {
            String headLenStr = ReceiveAndSend.readStr(sc, 5);

            int headlength =new Integer(headLenStr);
            String headContent = ReceiveAndSend.readStr(sc, headlength);

            String [] fileNum_fileNamePrintNum = headContent.split(ReceiveAndSend.splitChar+ReceiveAndSend.splitChar);
            int temp =new Integer(fileNum_fileNamePrintNum[0]);
            String fileNamePrintNum = fileNum_fileNamePrintNum[1];

            System.out.println("fileNamePrintNum="+fileNamePrintNum);

            for (int i=0; i<temp; i++){
                ReceiveAndSend.receiveFile(sc, ReceiveAndSend.serverPath+ File.separator+tidMod);
            }

            sc.write(ByteBuffer.wrap("OKOKOKOKOK".getBytes("UTF-8")));
            sc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
