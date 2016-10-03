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
	
	//初始化
	public ServerThread(Socket socket)
	{
		this.socket=socket;
	}
	
	//将客户端发来的数据解析成一个字符串
   String[] DivideLine(String line)
		{
			String[] Data=line.split("/");
			return Data;
		}
		
		//将数据还原成字符串
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
	
	//初始化数据库的链接
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
					//0.客户端登陆时发送过来的登陆信息，检测之
					//首先在数据库里检测用户是否存在,密码是否符合
					if(Data[1].endsWith("U"))
					{
						//如果是用户登录，则在User中查找
						ResultSet rs=statement.executeQuery("select * from User where User_ID = "+"'"+Data[1]+"'");
						rs.next();
						if(rs.getString(4).equals(Data[2])&&!Server.clients.map.containsKey(Data[1]))
						{
							//如果存在且密码符合，将用户名和相应的流存入ClinetMap
							Server.clients.put(Data[1], WriteToClient);
							System.out.println("客户端连接到服务器,该客户端名字是："+Data[1]);
							
							WriteToClient.println("LoginInfo/OK");
						}
						else if(rs.next() == false)
						{
							WriteToClient.println("LoginInfo/Wrong name or Wrong password");
						}
					}
					else if(Data[1].endsWith("P"))
					{
						//如果是代练者登陆，则在Player中查找
						ResultSet rs=statement.executeQuery("select * from Player where Player_ID = "+"'"+Data[1]+"'");
						rs.next();
						if(rs.getString(4).equals(Data[2])&&!Server.clients.map.containsKey(Data[1]))
						{
							//如果存在且密码符合，将用户名和相应的流存入ClinetMap
							Server.clients.put(Data[1], WriteToClient);
							System.out.println("客户端连接到服务器,该客户端名字是："+Data[1]);
							
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
					//1.代练者发布代练信息广播给所有的用户
					
					//把数据还原成原数据报
					String PlayInformation=ReverData(Data);
					for(PrintStream clientPS:Server.clients.valueSet())
					{
						//判断HashMap里的用户（排除代练者）
						if(Server.clients.getKeyByValue(clientPS).endsWith("U"))
						{
							clientPS.println(PlayInformation);
						}
					}
				}
				
				else if(Data[0].equals("OrderFromPlayer"))
				{
					//2.	代练者生成订单给特定用户
					String OrderFromPlayer=ReverData(Data);
					for(PrintStream clientPS:Server.clients.valueSet())
					{
						//根据数据报，查找HashMap里符合的用户，找到对应的流，以下相同
						if(Server.clients.getKeyByValue(clientPS).endsWith("U")&&Server.clients.getKeyByValue(clientPS).startsWith(Data[1]))
						{
							clientPS.println(OrderFromPlayer);
						}
					}
				}
				
				else if(Data[0].equals("OrderToPlayer"))
				{
					//3.	用户确认代练者生成的订单并回送给带练者
					String OrderToPlayer=ReverData(Data);
					for(PrintStream clientPS:Server.clients.valueSet())
					{
						if(Server.clients.getKeyByValue(clientPS).endsWith("P")&&Server.clients.getKeyByValue(clientPS).startsWith(Data[1]))
						{
							clientPS.println(OrderToPlayer);
						}
					}
					//将订单信息存入数据库
					statement.executeUpdate("insert into orders "
							+ "values ("+"'"+Data[2]+"'"+","+"'"+Data[3]+"'"+","+"'"+Data[4]+"'"+","+"'"+Data[6]+"'"+","+"'"+Data[5]+"'"+")");
				}
				
				else if(Data[0].equals("OrderFinished"))
				{
					//4.	带练者完成订单后向用户发送信息
					String OrderFinished=ReverData(Data);
					for(PrintStream clientPS:Server.clients.valueSet())
					{
						if(Server.clients.getKeyByValue(clientPS).endsWith("U")&&Server.clients.getKeyByValue(clientPS).startsWith(Data[1]))
						{
							clientPS.println(OrderFinished);
						}
					}
					//更新数据库的内容
					statement.executeQuery(" update orders "
							+"set Order_Finished = 'OK'"+"where User_ID ="+"'"+Data[1]+"'");//此处有一个逻辑问题
				}
				
				else if(Data[0].equals("EvaluationFromUser"))
				{
					//5.	用户向带练者评价
					String EvaluationFromUser=ReverData(Data);
					for(PrintStream clientPS:Server.clients.valueSet())
					{
						if(Server.clients.getKeyByValue(clientPS).endsWith("P")&&Server.clients.getKeyByValue(clientPS).startsWith(Data[1]))
						{
							clientPS.println(EvaluationFromUser);
						}
					}
					//将评价的数据存入数据库
					statement.executeUpdate("insert into Evaluation "+
					"values ("+"'"+Data[1]+"'"+","+"'"+Data[2]+"'"+","+"'"+Data[3]+"'"+")");
				}
				
				else if(Data[0].equals("MessageToPlayer"))
				{
					//6.	用户向带代练者留言
					String MessageToPlayer=ReverData(Data);
					for(PrintStream clientPS:Server.clients.valueSet())
					{
						if(Server.clients.getKeyByValue(clientPS).endsWith("P")&&Server.clients.getKeyByValue(clientPS).startsWith(Data[1]))
						{
							clientPS.println(MessageToPlayer);
						}
					}
					//将留言存入数据库
					statement.executeUpdate("update Message "+
					"values ("+"'"+Data[1]+"'"+","+"'"+Data[2]+"'"+","+"'"+Data[3]+"'"+")");
				}
				
							
				else if(Data[0].equals("ClientRegist"))
				{
					//7.	用户（代练者）注册向服务器发送信息
					if(Data[1].endsWith("U"))
					{
						//如果是用户注册，则在User中添加
						
						//先判断数据库中是否存在该用户
						ResultSet rs=statement.executeQuery("select * from User where User_ID ="+"'"+Data[1]+"'");
						rs.next();
						if(rs.next() == false)
						{
							//不存在，就存数据库
							
							statement.executeUpdate("insert into User "
									+"values("+"'"+Data[1]+"'"+","+"'"+Data[2]+"'"+","+"'"+Data[3]+"'"+","+"'"+Data[4]+"'"+")");
							
							WriteToClient.println("RegistInfo/OK");
						}
						else
						{
							//存在，就返回提示信息
							
							WriteToClient.println("RegistInfo/Client Exists!!");
						}
					}
					else if(Data[1].endsWith("P"))
					{
						//如果是代练者注册，则在Player中添加
						
						//先判断数据库中是否存在该用户
						ResultSet rs=statement.executeQuery("select * from Player where Player_ID ="+"'"+Data[1]+"'");
						rs.next();
						if(rs.next() == false)
						{
							//不存在，就存数据库
							statement.executeUpdate("insert into Player "
									+"values("+"'"+Data[1]+"'"+","+"'"+Data[2]+"'"+","+"'"+Data[3]+"'"+","+"'"+Data[4]+"'"+")");
							WriteToClient.println("RegistInfo/OK");
						}
						else
						{
							//存在，就返回提示信息
							WriteToClient.println("RegistInfo/Client Exists!!");
						}
					}
					
				}
				
			}
		}
		catch (IOException e)
		{
			//如果出现了输入输出的异常，则客户端出现问题，直接删除相应的流
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
