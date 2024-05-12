import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Server {

	static int sPort = 5106;    //The server will be listening on this port number

	public static void main(String[] args) throws Exception {
		
			System.out.println("The server is running."); 
        	ServerSocket listener = new ServerSocket(sPort);
			int clientNum = 1;
			
        	try {
            		while(true) {
                		new Handler(listener.accept(),clientNum).start();
						System.out.println("Client "  + clientNum + " is connected!");
						clientNum++;
            		}
        	} 
			finally {
            		listener.close();
        	} 
 
    	}

	/**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single client's requests.
     	*/
    	private static class Handler extends Thread {
			
        	private String message;    //message received from the client
			private String MESSAGE;    //uppercase message send to the client
			private Socket connection;
        	private ObjectInputStream in;	//stream read from the socket
        	private ObjectOutputStream out;    //stream write to the socket
			private int no;		//The index number of the client

        	public Handler(Socket connection, int no) {
				
            	this.connection = connection;
	    		this.no = no;
				System.out.println("Connection received from " + connection.getInetAddress().getHostName());
				
        	}

			public void run() {
				try{
					//initialize Input and Output streams
					out = new ObjectOutputStream(connection.getOutputStream());
					out.flush();
					in = new ObjectInputStream(connection.getInputStream());
					try{
						
						while(true){
							
							//receive the message sent from the client
							try{
								
								message = (String)in.readObject();
							}
							catch (Exception e){
								e.printStackTrace();
							}
							
							//the user wants to upload the file so we do this
							if(message.length()>6 && message.substring(0,6).equals("upload")){
								
								//we need to create a file name with new prefix when saving on server side
								String newName="new_"+message.substring(7);
								try{
									receiveFile(newName);
								}
								 catch (Exception e) {
									e.printStackTrace();
								}
							}
							
							//if the user wants to fetch a file we do this
							else if(message.length()>3 && message.substring(0,3).equals("get")){
								
								// we send the file name immediately so that it can name the file with new prefix, this can be included later also
								sendMessage(message.substring(4));
								
								try{
									//we fetch the file and send it
									sendFile(message.substring(4));
								}
								 catch (Exception e) {
									e.printStackTrace();
								}
							}
							
							else if(message.equals("stop")){
								
								System.out.println("Byee again!");
								break;
								
							}
							
							//this is for invalid commands
							else{
								System.out.println("WRONG COMMAND type again in client!!!");
							}
							
						}

					}
					catch(Exception classnot){ //catch(ClassNotFoundException classnot)
							System.err.println("Data received in unknown format");
					}
				}
				catch(IOException ioException){
					System.out.println("Disconnect with Client " + no);
				}
				finally{
					//Close connections
					try{
						in.close();
						out.close();
						connection.close();
					}
					catch(IOException ioException){
						System.out.println("Disconnect with Client " + no);
					}
				}
			}

			//send a message to the output stream
			void sendMessage(String msg){
				try{
					out.writeObject(msg);
					out.flush();
					System.out.println("Send message: " + msg);
				}
				catch(IOException ioException){
					ioException.printStackTrace();
				}
			}
			
			// file is sent with this
			 void sendFile(String sFile)throws Exception{
				 
				int bytes = 0;
				
				//it is assumed that file to be sent is in the same directory and hence we use just the name
				File file = new File(sFile);
				FileInputStream fileInputStream = new FileInputStream(file);
		 
				// this is out using socket
				out.writeLong(file.length());
				
			   // we break into 1K bytes
				byte[] buffer = new byte[1000];
				
				while ((bytes = fileInputStream.read(buffer))!= -1) {
				  // Send the file to Server  
					out.write(buffer, 0, bytes);
					out.flush();
				}
				// close the file here
				fileInputStream.close();
			}
			
			//recieve file is defined here 
			void receiveFile(String fileName) throws Exception {
				
				int bytes = 0;
				FileOutputStream fileOutputStream = new FileOutputStream(fileName);
		 
			// file size
				long size = in.readLong(); 
				byte[] buffer = new byte[1000];
				while (size > 0 && (bytes = in.read(buffer, 0,(int)Math.min(buffer.length ,size)))!= -1) {
					
					// writing the file 
					fileOutputStream.write(buffer, 0, bytes);
					
					// reading upto file size
					size -= bytes; 
				}
				
				// Here we received file
				System.out.println("File is uploaded!");
				fileOutputStream.close();
			}

    }


}
