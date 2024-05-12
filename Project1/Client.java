

import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class Client {
	
	Socket requestSocket;           //socket connect to the server
	
	ObjectOutputStream out;         //stream write to the socket
	ObjectInputStream in;          //stream read from the socket
	
	String message;                //message send to the server
	String MESSAGE;    				//message read from the server
	
	public void Client() {}
	
	void run(int portNumber){
		
		try{
			
			//create a socket to connect to the server
			requestSocket = new Socket("localhost", portNumber);
			System.out.println("Connected to localhost in port "+portNumber);
			
			//initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			//to get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new 
			InputStreamReader(System.in));
			
	//********* Main code starts here************//	
	
			while(true){
				
				System.out.println("Say something!");
				
				//get what the user wants to perform 
				message = bufferedReader.readLine();
				
				//we send the entire command recieved so that server knows what to do like fetch the file or upload the file and also keep the file name		
				sendMessage(message);
				
				//this is if the user wants to upload the file
				if(message.length()>6 && message.substring(0,6).equals("upload")){
					
					//we send the file using this
					sendFile(message.substring(7));
					
				}
				
				//this is if a user wants to get a file name
				else if(message.length()>3 && message.substring(0,3).equals("get")){

					try{
						//receive the message sent from the client, here it is the file name for now 
						MESSAGE = (String)in.readObject();
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					
					String newName="new_"+MESSAGE;
				
					receiveFile(newName);
					
				}
				
				//this is to stop
				else if(message.equals("stop")){			
					System.out.println("Byee!");
					break;
					
				}
				
				//this is for invalid commands
				else{
					System.out.println("WRONG COMMAND!!!");
				}
								
			}	
 
		}
		
		catch (ConnectException e) {
		    System.err.println("Connection refused. You need to initiate a server first.");
		} 
		catch ( ClassNotFoundException e ) {
		    System.err.println("Class not found");
		} 
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		
		catch(IOException ioException){
			ioException.printStackTrace();
		}
		catch (Exception e) {
            e.printStackTrace();
        }
		
		finally{
		//Close connections	
			try{
				in.close();
				out.close();
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
		
	}


	//send a message to the output stream
	void sendMessage(String s){
		try{
			//stream write the message
			out.writeObject(s);
			out.flush();
			System.out.println("Send message: " + s);
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
			
			
			System.out.println("File is downloaded!");
			fileOutputStream.close();
	}
	


	//main method
	public static void main(String args[]) throws IOException{
		
			// input Port number
			String portNumber = "";		
			System.out.println("Please input the \"ftpclient <ServerPortNumber>\"");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			
			String[] read = bufferedReader.readLine().split(" ");

		
			
			if(read.length<3){
				String ftpclient = read[0];
				portNumber = read[1];
			}

		// begin Client
		Client client = new Client();
		client.run(Integer.parseInt(portNumber));
	
	}

}
