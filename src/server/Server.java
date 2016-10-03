package server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
	//定义服务器的端口
	private static final int SERVER_PORT=30000;
	
	public static ClientMap<String,PrintStream> clients=new ClientMap<String,PrintStream>();
	
	public void init()
	{
		try
		{
			ServerSocket serversocket=new ServerSocket(SERVER_PORT);
			while(true)
			{
				Socket socket=serversocket.accept();
				new ServerThread(socket).start();
				System.out.println("连接成功");
			}	
		}
		catch(IOException ex)
		{
			System.out.println("服务器启动失败");
		}
	}
	
	public static void main(String[] args)
	{
		Server server=new Server();
		server.init();
	}

}
