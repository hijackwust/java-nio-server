package zhu.zhiwu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerSocket {
	public static ExecutorService executerService = Executors.newFixedThreadPool(2);

	public static void main(String[] args) throws Exception {
		int port = ReceiveAndSend.listenPort;
		
		//打开服务器套接字通道
		ServerSocketChannel ssc = ServerSocketChannel.open();
		//创建一个选择器
		Selector selector = Selector.open();
		//设置非阻塞模式
		ssc.configureBlocking(false);
		InetSocketAddress address = new InetSocketAddress(port);
		//绑定监听端口
		ssc.bind(address);
		//注册选择器，保持等待模式
		ssc.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("服务器已开启,端口："+port);
		while(true){
			selector.select();
			//返回此选择器的已选择键集
			Set<SelectionKey> keys=selector.selectedKeys();
			Iterator<SelectionKey> iterKey=keys.iterator();
			
			while(iterKey.hasNext()){
				SelectionKey sk=iterKey.next();
				//测试此键的通道是否已准备好接受新的套接字连接
				if(sk.isAcceptable()){
					SocketChannel sc=ssc.accept();
					System.out.println("服务端收到一个sc"+sc.toString());
					executerService.execute(new ServerThread(sc));
				}
			}
		}
	}
	
}
