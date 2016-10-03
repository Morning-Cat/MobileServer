package server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
	//����������Ķ˿�
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
				System.out.println("���ӳɹ�");
			}	
		}
		catch(IOException ex)
		{
			System.out.println("����������ʧ��");
		}
	}
	
	public static void main(String[] args)
	{
		Server server=new Server();
		server.init();
	}

}
