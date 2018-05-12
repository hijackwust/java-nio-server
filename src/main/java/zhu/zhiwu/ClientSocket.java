package zhu.zhiwu;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * 注意传输的时候文件名不能为中文，并且长度不能过长, 否则请重新设置长度，目前
 */
public class ClientSocket {

	public static void main(String[] args){
		
		int port=ReceiveAndSend.listenPort;
		String ip = ReceiveAndSend.serverIp;
		//打开传输通道

		for (int i = 0 ; i< 3 ; i++) {
			new Thread() {
				public void run() {
					long tid =Thread.currentThread().getId();
					int tidMod = (int)tid%3;
					//tidMod=1;
					System.out.println("线程="+tid+", tid%3="+tidMod);

					try {
						SocketChannel sc = SocketChannel.open();
						//连接
						sc.connect(new InetSocketAddress(ip, port));

						File path = new File(ReceiveAndSend.clientPath + File.separator + tidMod);
						Map<String, Integer> maps = new LinkedHashMap<String, Integer>();
						if (path.exists()) {
							Random rand = new Random();
							File[] files = path.listFiles(new FileFilter() {
								@Override
								public boolean accept(File pathname) {
									return pathname.isFile();
								}
							});

							StringBuffer sb = new StringBuffer();
							sb.append(String.format("%05d", files.length) + ReceiveAndSend.splitChar);
							for (File file : files) {
								String fileName = file.getName();
								int printCount = rand.nextInt(20);
								maps.put(fileName, printCount);
								sb.append(ReceiveAndSend.splitChar + fileName + "*" + printCount);
							}

							/**
							 * target=0004300003##222.txt*16#hh222.png*2#test222.png*6
							 * 00043 表示 00003##222.txt*16#hh222.png*2#test222.png*6 头文件内容总长度为 43
							 * 00003 表示 传输3个文件
							 * ##分割符
							 * 222.txt*16 表示 文件名为 222.txt 打印 16份
							 * #分割符 文件与文件之间的分割符
							 */
							String fileNum_fileNamePrintCount = sb.toString();
							String target = String.format("%05d", fileNum_fileNamePrintCount.length()) + fileNum_fileNamePrintCount;
							System.out.println("target=" + target);

							//发送文件  2 为文件个数 先要发送文件的个数 占5位表示文件个数
							//sc.write(ByteBuffer.wrap("0002600002##node.txt*11#3.jpg*3".getBytes("UTF-8")));
							sc.write(ByteBuffer.wrap(target.getBytes("UTF-8")));

							for (Map.Entry<String, Integer> entry : maps.entrySet()) {
								ReceiveAndSend.sendFile(sc, ReceiveAndSend.clientPath + File.separator + tidMod, entry.getKey());
							}

							String serverEcho=ReceiveAndSend.readStr(sc, 10);
							System.out.println("得到服务器端响应消息="+serverEcho);
						}else{
							System.out.println("文件路径不存在path="+path);
						}

						//关闭传输通道
						sc.close();
					}catch (Exception e){
						e.printStackTrace();
					}

				}
			}.start();
		}
		
	}
}
