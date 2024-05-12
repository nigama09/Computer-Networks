import java.io.*;
import java.net.*;
import java.util.*;

public class Chat {
	
    private static final int BUFFER_SIZE = 1024;
    
    //userName  is the user for which the console is running on 
    static String userName ;

    public static void main(String[] args) throws Exception {
    	
		//we take the user name and the port associated with them from console
		
		System.out.println("Enter the user name : ");
		Scanner sc = new Scanner(System.in);
		userName = sc.nextLine();
		System.out.println("Enter the port number to assign them : ");
		int portNo = sc.nextInt();

       // Create the ServerSocket which is used for server side implementation of user
		
        ServerSocket serverSocket = new ServerSocket(portNo); // We can also use 0 to take any available port and get that port info using the below code
        
        //use this to get port number that is connected -> .getLocalPort();
        
        System.out.println(userName +" is running");
        System.out.println("The server port for "+userName +" is : " + portNo);
        
        //This writing thread is like a client crogram itself where the main is like the server.
        try {
        	
           // start the reading thread which implements the client side 
		   new writingThread().start();

            // here we acccept connections from clients
            while (true) {
                Socket connection = serverSocket.accept();
                Thread.sleep(5000);

				//start the reading thread which implements the server side 
                new readingThread(connection).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	

 
    private static class writingThread extends Thread  {
				
		public void run(){
					
			try {
							
				// Get the port number to connect to
				System.out.print("Please enter the target port number: ");
							
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				int port = Integer.parseInt(br.readLine());

				// Connect to the server
				Socket socket = new Socket("localhost", port);
							
				
				OutputStream os = socket.getOutputStream();
				//using sleep to give some time to type on other side
				try {
					Thread.sleep(7000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.print("sleep failed in write block ");
								
				}
		    	//sending this user details i.e name to the other side 
				sendMessage(os,userName);
							
				while (true) {
								
					//read the message from this user console
					String message = br.readLine();

					//use this to exit program when user says stop or empty is given
					if (message == null || message.equals("stop")) {
						System.out.print("Exiting the chat! ");
						break;
					}
								
					//send the message through the output side of the socket 
					sendMessage(os,message);
			

					// the format is "transfer file" to send file
					if (message.startsWith("transfer")) {
										
						String filename = message.substring(9);
						sendFile(os, filename);
						
					}
									
					
				}

				// Close the socket and exit
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
			
	}
           


   private static class readingThread extends Thread  {
		
		Socket connection;
		
		public readingThread(Socket connection){
			this.connection=connection;
		}
		
		public void run(){
				
		    try {
                InputStream is = connection.getInputStream();
                BufferedReader bfr = new BufferedReader(new InputStreamReader(is));
                        
                //read the user name that we get from other side
                String user2 = bfr.readLine();
        		System.out.println("New connection from " + user2);
            			
                while (true) {
                   //	System.out.println("Say something! : ");
                    String message = bfr.readLine();
					
                    if (message == null || message.equals("stop")) {
						System.out.println("Closing the chat!");
                        break;
                    }
                    System.out.println(user2+" : "+message);
					
					if (message.startsWith("transfer ")) {
						//9 is our start index for given condition
                        String filename = message.substring(9);
                        receiveFile(is, filename);
						
                    }
                   
                }

           
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
					
		}
		
	}
           

    // We use this method to send file by breaking it into pieces and send the file
    private static void sendMessage(OutputStream output, String message) throws IOException {
					output.write(message.getBytes());
					output.write('\n');
					output.flush();
    }


    // We use this method to send file by breaking it into pieces and send the file
    private static void sendFile(OutputStream output, String filename) throws IOException {
        File file= new File(filename);
        
        if (!file.exists() || !file.isFile()) {
            System.err.println("Invalid file: " + filename);
            return;
        }
		//ObjectOutputStream out=new ObjectOutputStream(output);
		long i=file.length();
		String x= String.valueOf(i);
		sendMessage(output,x);
        byte[] buffer = new byte[BUFFER_SIZE];
        InputStream input = new FileInputStream(file);
        int bytesRead;
		
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
            output.flush();
        }
		System.out.println("sent!");
        input.close();
    }

    // use this method to recieve file from other side
    private static void receiveFile(InputStream input, String filename) throws IOException {

        File newFile = new File("new_"+filename);
		 //ObjectInputStream in= new ObjectInputStream(input);
        BufferedReader bfr = new BufferedReader(new InputStreamReader(input));
        String ii = bfr.readLine();
		//System.out.println(ii);
        long size=Long.parseLong(ii);  
		//System.out.println(size);
		
        byte[] buffer = new byte[BUFFER_SIZE];
        OutputStream output = new FileOutputStream(newFile);
        int bytesRead;
		
		//while ((bytesRead = input.read(buffer)) != -1)
        while (size>0 &&(bytesRead = input.read(buffer, 0,(int)Math.min(buffer.length ,size)))!= -1) {
            output.write(buffer, 0, bytesRead);
			size -= bytesRead; 
        }
		System.out.println("recieved!");
        output.close();
    }
}
