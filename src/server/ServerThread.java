package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ServerThread extends Thread
{
	private Socket socket;
	
	BufferedReader ReadFromClient=null;
	PrintStream WriteToClient=null;
	String[] Data=null;
	
	//��ʼ��
	public ServerThread(Socket socket)
	{
		this.socket=socket;
	}
	
	//���ͻ��˷��������ݽ�����һ���ַ���
   String[] DivideLine(String line)
		{
			String[] Data=line.split("/");
			return Data;
		}
		
		//�����ݻ�ԭ���ַ���
	String ReverData(String Data[])
		{
			String line="";
			for(int i=0;i<Data.length;i++)
			{
				if(i != Data.length-1)
				{
					line+=Data[i]+"/";
				}
				else
				{
					line+=Data[i];
				}
			}
			return line;
		}
	
	//��ʼ�����ݿ������
	Statement initDatabaseLink() throws ClassNotFoundException, SQLException
	{
		String driver="com.mysql.jdbc.Driver";
		String url="jdbc:mysql://127.0.0.1:3306/mobileserverdatabase";
		String name="Root";
		String databasepass="123456";
		 
		
		Class.forName(driver);
	
		Connection connection=DriverManager.getConnection(url,name,databasepass);
		Statement stmt=connection.createStatement();
			
	
		return stmt;
	}
		
	
	public void run()
	{
		try
		{
			ReadFromClient=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			WriteToClient=new PrintStream(socket.getOutputStream());
			String line=null;
			Statement statement=initDatabaseLink();
			
			while((line=ReadFromClient.readLine()) != null)
			{
				Data=DivideLine(line);
				
				if(Data[0].equals("ClientLogin"))
				{
					//0.�ͻ��˵�½ʱ���͹����ĵ�½��Ϣ�����֮
					//���������ݿ������û��Ƿ����,�����Ƿ����
					if(Data[1].endsWith("U"))
					{
						//������û���¼������User�в���
						ResultSet rs=statement.executeQuery("select * from User where User_ID = "+"'"+Data[1]+"'");
						rs.next();
						if(rs.getString(4).equals(Data[2])&&!Server.clients.map.containsKey(Data[1]))
						{
							//���������������ϣ����û�������Ӧ��������ClinetMap
							Server.clients.put(Data[1], WriteToClient);
							System.out.println("�ͻ������ӵ�������,�ÿͻ��������ǣ�"+Data[1]);
							
							WriteToClient.println("LoginInfo/OK");
						}
						else if(rs.next() == false)
						{
							WriteToClient.println("LoginInfo/Wrong name or Wrong password");
						}
					}
					else if(Data[1].endsWith("P"))
					{
						//����Ǵ����ߵ�½������Player�в���
						ResultSet rs=statement.executeQuery("select * from Player where Player_ID = "+"'"+Data[1]+"'");
						rs.next();
						if(rs.getString(4).equals(Data[2])&&!Server.clients.map.containsKey(Data[1]))
						{
							//���������������ϣ����û�������Ӧ��������ClinetMap
							Server.clients.put(Data[1], WriteToClient);
							System.out.println("�ͻ������ӵ�������,�ÿͻ��������ǣ�"+Data[1]);
							
							WriteToClient.println("LoginInfo/OK");
						}
						else if(rs.next() == false)
						{
							WriteToClient.println("LoginInfo/Wrong name or Wrong password");
						}
						
					}
					
				}
				
				else if(Data[0].equals("PlayInformation"))
				{
					//1.�����߷���������Ϣ�㲥�����е��û�
					
					//�����ݻ�ԭ��ԭ���ݱ�
					String PlayInformation=ReverData(Data);
					for(PrintStream clientPS:Server.clients.valueSet())
					{
						//�ж�HashMap����û����ų������ߣ�
						if(Server.clients.getKeyByValue(clientPS).endsWith("U"))
						{
							clientPS.println(PlayInformation);
						}
					}
				}
				
				else if(Data[0].equals("OrderFromPlayer"))
				{
					//2.	���������ɶ������ض��û�
					String OrderFromPlayer=ReverData(Data);
					for(PrintStream clientPS:Server.clients.valueSet())
					{
						//�������ݱ�������HashMap����ϵ��û����ҵ���Ӧ������������ͬ
						if(Server.clients.getKeyByValue(clientPS).endsWith("U")&&Server.clients.getKeyByValue(clientPS).startsWith(Data[1]))
						{
							clientPS.println(OrderFromPlayer);
						}
					}
				}
				
				else if(Data[0].equals("OrderToPlayer"))
				{
					//3.	�û�ȷ�ϴ��������ɵĶ��������͸�������
					String OrderToPlayer=ReverData(Data);
					for(PrintStream clientPS:Server.clients.valueSet())
					{
						if(Server.clients.getKeyByValue(clientPS).endsWith("P")&&Server.clients.getKeyByValue(clientPS).startsWith(Data[1]))
						{
							clientPS.println(OrderToPlayer);
						}
					}
					//��������Ϣ�������ݿ�
					statement.executeUpdate("insert into orders "
							+ "values ("+"'"+Data[2]+"'"+","+"'"+Data[3]+"'"+","+"'"+Data[4]+"'"+","+"'"+Data[6]+"'"+","+"'"+Data[5]+"'"+")");
				}
				
				else if(Data[0].equals("OrderFinished"))
				{
					//4.	��������ɶ��������û�������Ϣ
					String OrderFinished=ReverData(Data);
					for(PrintStream clientPS:Server.clients.valueSet())
					{
						if(Server.clients.getKeyByValue(clientPS).endsWith("U")&&Server.clients.getKeyByValue(clientPS).startsWith(Data[1]))
						{
							clientPS.println(OrderFinished);
						}
					}
					//�������ݿ������
					statement.executeQuery(" update orders "
							+"set Order_Finished = 'OK'"+"where User_ID ="+"'"+Data[1]+"'");//�˴���һ���߼�����
				}
				
				else if(Data[0].equals("EvaluationFromUser"))
				{
					//5.	�û������������
					String EvaluationFromUser=ReverData(Data);
					for(PrintStream clientPS:Server.clients.valueSet())
					{
						if(Server.clients.getKeyByValue(clientPS).endsWith("P")&&Server.clients.getKeyByValue(clientPS).startsWith(Data[1]))
						{
							clientPS.println(EvaluationFromUser);
						}
					}
					//�����۵����ݴ������ݿ�
					statement.executeUpdate("insert into Evaluation "+
					"values ("+"'"+Data[1]+"'"+","+"'"+Data[2]+"'"+","+"'"+Data[3]+"'"+")");
				}
				
				else if(Data[0].equals("MessageToPlayer"))
				{
					//6.	�û��������������
					String MessageToPlayer=ReverData(Data);
					for(PrintStream clientPS:Server.clients.valueSet())
					{
						if(Server.clients.getKeyByValue(clientPS).endsWith("P")&&Server.clients.getKeyByValue(clientPS).startsWith(Data[1]))
						{
							clientPS.println(MessageToPlayer);
						}
					}
					//�����Դ������ݿ�
					statement.executeUpdate("update Message "+
					"values ("+"'"+Data[1]+"'"+","+"'"+Data[2]+"'"+","+"'"+Data[3]+"'"+")");
				}
				
							
				else if(Data[0].equals("ClientRegist"))
				{
					//7.	�û��������ߣ�ע���������������Ϣ
					if(Data[1].endsWith("U"))
					{
						//������û�ע�ᣬ����User�����
						
						//���ж����ݿ����Ƿ���ڸ��û�
						ResultSet rs=statement.executeQuery("select * from User where User_ID ="+"'"+Data[1]+"'");
						rs.next();
						if(rs.next() == false)
						{
							//�����ڣ��ʹ����ݿ�
							
							statement.executeUpdate("insert into User "
									+"values("+"'"+Data[1]+"'"+","+"'"+Data[2]+"'"+","+"'"+Data[3]+"'"+","+"'"+Data[4]+"'"+")");
							
							WriteToClient.println("RegistInfo/OK");
						}
						else
						{
							//���ڣ��ͷ�����ʾ��Ϣ
							
							WriteToClient.println("RegistInfo/Client Exists!!");
						}
					}
					else if(Data[1].endsWith("P"))
					{
						//����Ǵ�����ע�ᣬ����Player�����
						
						//���ж����ݿ����Ƿ���ڸ��û�
						ResultSet rs=statement.executeQuery("select * from Player where Player_ID ="+"'"+Data[1]+"'");
						rs.next();
						if(rs.next() == false)
						{
							//�����ڣ��ʹ����ݿ�
							statement.executeUpdate("insert into Player "
									+"values("+"'"+Data[1]+"'"+","+"'"+Data[2]+"'"+","+"'"+Data[3]+"'"+","+"'"+Data[4]+"'"+")");
							WriteToClient.println("RegistInfo/OK");
						}
						else
						{
							//���ڣ��ͷ�����ʾ��Ϣ
							WriteToClient.println("RegistInfo/Client Exists!!");
						}
					}
					
				}
				
			}
		}
		catch (IOException e)
		{
			//�������������������쳣����ͻ��˳������⣬ֱ��ɾ����Ӧ����
			Server.clients.removeByValue(WriteToClient);
			
			try
			{
				if(ReadFromClient != null)
				{
					ReadFromClient.close();
				}
				if(WriteToClient != null)
				{
					WriteToClient.close();
				}
				if(socket != null)
				{
					socket.close();
				}
				
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
			
		} 
		catch (ClassNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
