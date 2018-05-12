package zhu.zhiwu;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Random;

public class ReceiveAndSend {
	public static final String serverIp="localhost";
	public static final int listenPort=20000;
	public static final String splitChar="#";
	public static final String clientPath;
	public static final String serverPath;
	static{
		if(SystemUtils.IS_OS_WINDOWS){
			clientPath="C:\\Users\\ZhuZhiwu\\client";
			serverPath="C:\\Users\\ZhuZhiwu\\server";
		}else{
			clientPath="/Users/jessezhu/client";
			serverPath="/Users/jessezhu/server";
		}
	}


	public static final Random rand =new Random();
	/**
	 * 接收文件
	 * @param sc
	 * @throws IOException
	 */
	public static void receiveFile(SocketChannel sc, String path) throws Exception{
		System.out.println("receiving file...");

		File pathFile = new File(path);
		if(!pathFile.exists()){
			pathFile.mkdirs();
		}

		//获取文件信息
		String fileInfo=ReceiveAndSend.readStr(sc, 32);

		String[] strInfo=fileInfo.split("\\|");
		System.out.println("文件："+strInfo[0]+"--大小："+strInfo[1]);

		//获取保存文件
		File file=new File(path+File.separator+strInfo[0]);
		FileOutputStream fos=new FileOutputStream(file);
		//获取通道
		FileChannel foc = fos.getChannel();

		long fileLength =Long.valueOf(strInfo[1]);

		long recieveLen =0;
		//设置接收消息体缓冲区
		boolean isMinBuffer =false;
		ByteBuffer bb=ByteBuffer.allocateDirect(1024);
		if(fileLength <= 1024L){
			bb=ByteBuffer.allocateDirect(1);
			isMinBuffer=true;
		}
		int read=sc.read(bb);
		recieveLen +=read;
//                           10000       10001
		while(read!=-1 && recieveLen <fileLength){
			bb.flip();   //  10030       10001
			//写入到输出通道
			foc.write(bb);
			bb.clear();

			if(!isMinBuffer && (fileLength-recieveLen) <=1024L ){
				bb=ByteBuffer.allocateDirect(1);
			}
			read=sc.read(bb);
			recieveLen +=read;
		}

		bb.flip();
		foc.write(bb);
		bb.clear();

		foc.close();
		fos.close();

//		return slice2;
	}
	/**
	 * 发送文件
	 * @param sc
	 * @param path
	 * @param fileName
	 * @throws IOException
	 */
	public static void sendFile(SocketChannel sc,String path,String fileName) throws Exception{
		File pathFile = new File(path);
		if(!pathFile.exists()){
			pathFile.mkdirs();
		}

		File file=new File(path+File.separator+fileName);
		FileInputStream fis = new FileInputStream(file);
		
		FileChannel fic = fis.getChannel();
		
		ByteBuffer bb = ByteBuffer.allocateDirect(1024);
		ByteBuffer headbb = ByteBuffer.allocateDirect(32);
		int read=0;
		long fileSize = file.length();
		long sendSize=0;
		System.out.println("文件大小："+fileSize);
		//拼接头信息
		String head=fileName+"|"+fileSize+"|;";
		//将头信息写入缓冲区
		headbb.put(head.getBytes(Charset.forName("UTF-8")));
		int c=headbb.capacity()-headbb.position();
		//填满头信息
		for (int i = 0; i < c; i++) {
			headbb.put(";".getBytes(Charset.forName("UTF-8")));
		}
		headbb.flip();
		//将头信息写入到通道
		//sc.write(ByteBuffer.wrap("123456abcdefg".getBytes()));
		sc.write(headbb);
		do{
			//将文件写入到缓冲区
			read = fic.read(bb);
			sendSize+=read;
			bb.flip();
			//将文件写入到通道
			sc.write(bb);
			bb.clear();
			System.out.println("已传输/总大小："+sendSize+"/"+fileSize);
		}while(read!=-1&&sendSize<fileSize);

		//sc.write(ByteBuffer.wrap("jjjjjjjjj".getBytes()));

		System.out.println("文件传输成功");
		fic.close();
		fis.close();
	}


	/**
	 * 从通道里读取一定长度 返回字符串
	 * @param sc
	 * @param readLen
	 * @return
	 * @throws IOException
	 */
	public static String readStr(SocketChannel sc, int readLen) throws IOException {
		byte[] handshakeB=readByteArray(sc, readLen);
		return new String(handshakeB,Charset.forName("UTF-8"));
	}

	public static byte[] readByteArray(SocketChannel sc, int readLen) throws IOException {

		ByteBuffer handshakeByte=ByteBuffer.allocateDirect(readLen);
		byte[] handshakeB=new byte[readLen];
		sc.read(handshakeByte);
		handshakeByte.flip();
		for (int i = 0; handshakeByte.hasRemaining(); i++) {
			handshakeB[i]=handshakeByte.get();
		}
		handshakeByte.clear();

		return handshakeB;
	}
}
