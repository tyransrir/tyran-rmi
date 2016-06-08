import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream.PutField;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;
import java.util.Map;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 
 */
public class Bully extends UnicastRemoteObject implements RemoteInterface {

	private static final long serialVersionUID = 1L;
	int port=5000;
    String nodeID;
    String nodeIP;

    String coordinatorID;

    Registry registry;

    boolean coordinator;    // Yes - If this node is the coordinator; No - otherwise
    boolean criticalSectionInUse = false;

    static Hashtable<String,String> peers = new Hashtable<String, String>(); // Key - "NodeID" ::: Value - "IP;Port"
    
    

    Bully(String id)
            throws AlreadyBoundException, RemoteException, UnknownHostException {
        //this.port = port;
        this.nodeID = id;
        this.nodeIP = InetAddress.getLocalHost().toString();

        //this.coordinatorID = coordId;

        //peers.put(coordId,coordIP);
        this.coordinator = false;

        this.setupConnection();
        
        System.setProperty("sun.rmi.transport.proxy.connectTimeout", "1000");
 
    }

    public void setupConnection() throws RemoteException, AlreadyBoundException {
        registry = LocateRegistry.createRegistry(port);
        registry.bind("" + this.nodeID,this);
    }
    
    
    
    public static long startTime;
    
    //Initiating Election
    public void initiateElection() throws NotBoundException, InterruptedException{
    	//
    	Registry registry;
    	RemoteInterface ri;
    	
    	boolean isBigger = false;
    	for(Map.Entry<String,String> entry: peers.entrySet()){
    		
    		if(nodeID.compareTo(entry.getKey()) < 0){
	    		try {
	    			String s = null;
					registry = LocateRegistry.getRegistry(""+entry.getValue(),5000);
					ri = (RemoteInterface)registry.lookup(""+entry.getKey());
					//start time
					startTime = System.currentTimeMillis();
					s = ri.giveResponse(nodeID);
					//stop time
					//long stopTime = System.currentTimeMillis();
				    //long elapsedTime = stopTime - startTime;
				    //System.out.println("Czas wyslania i odebrania wiadomosci: " +elapsedTime);
				      
					System.out.println("Odpowiedz: " + s);
					
					if(s != null){
						System.out.println("Jest proces o wyzszym id, on przejmuje elekcje");
						isBigger = true;
						//break;
					} 
					
				} catch (RemoteException e1) {
					System.out.println("Error in initiateElection");
					e1.printStackTrace();
				} catch (InterruptedException e) {
					System.out.println("Error in initiateElection");
					e.printStackTrace();
				}	    		
    		}
    	}
    	
    	if(!isBigger){
    		//System.out.println("Nodeid: " + nodeID + " zostaje liderem");
    		try {
				announceLeader();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    
    	
    }
    
  
    
    //Returning the coordinator
    public String getCoordinatorID(){
    	
    	return this.coordinatorID;
    	
    }
    
    //Get the details of the nodes in the network
    public Hashtable<String,String> getDetails(){
    	
    	return this.peers;
    }
    
    
    //Giving response to a client which has initiated the election and trying to elect itself 
    @Override
	public String giveResponse(String senderID) throws InterruptedException, NotBoundException, RemoteException {
		
    	if(nodeID.compareTo(senderID) > 0){
    		
    		/*Timer timer = new Timer();
    		timer.schedule(new TimerTask(){

				@Override
				public void run() {
					try {
						initiateElection();
					} catch (NotBoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}	
    		}, 1000);*/
    		initiateElection();
    		return "I'am bigger";
    	} else {
    		return null;
    	}
		
	
    }
    
    //Announcing that the current process is the leader to all the processes
    public void announceLeader() throws RemoteException, NotBoundException{
    	//peers.remove(this.coordinatorID);
    	coordinatorID = nodeID;
    	coordinator = true;
    	Registry registry;
    	RemoteInterface ri;
    	for(Map.Entry<String,String> entry: peers.entrySet()){
    		registry = LocateRegistry.getRegistry(""+entry.getValue(),port);
    		ri = (RemoteInterface)registry.lookup(""+entry.getKey());
    		ri.announce(nodeID);
    	}
    }
  
    //Change the coordinator id and remove the previous coordinator from the coordinator list
    public void announce(String node){
    	//peers.remove(this.coordinatorID);
    	System.out.println("!!! NOWYL LIDER TO : " + node);
    	this.coordinatorID = node;
    	//stop time
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    //System.out.println("Czas ca³kowitej elekcji: " +elapsedTime);

    }
    
	

    public static void main(String[] args) throws AlreadyBoundException, IOException, NotBoundException {
        
    	//peers.put("2", "192.168.1.2");
    	//peers.put("4", "192.168.1.4");
    	//peers.put("5", "192.168.1.5");
    	
    	
    	
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	
    	//enter all ips in network
    	System.out.println("Podaj ID oraz adresy innych wezlow w sieci: (END - konczy wpisywanie)");
    	String s = br.readLine();
    	while(!s.equals("END")){
    		String key = s;
    		String val = br.readLine();
    		peers.put(key, val);
    		s = br.readLine();
    	}
    	
    	for(Map.Entry<String,String> entry: peers.entrySet()){
    		System.out.println(entry.getKey() + " " + entry.getValue());
    	}
    	
    	
    	
        //System.out.println("Hey! I'm a new node.. Let's set me up!");
      //  System.out.print("Enter the port number: ");
        //int port = br.read();

        System.out.print("Podaj ID aktualnego wezla: ");
        String nodeID = br.readLine();
/*
        System.out.print("Enter nodeID of the coordinator: ");
        String coordnodeId = br.readLine();
        System.out.print("\nEnter the IP address of the coordinator: ");
        String coordinatorIP = br.readLine();
       // System.out.print("\nEnter the port number of the coordinator");
        //String coordinatorPort = br.readLine(); */

        Bully aBully = new Bully(nodeID);
        //aBully.join();

        System.out.println("\nAktualnie pracuje na ip:  " + aBully.getIP() +
                " i oczekuje na porcie 5000");
        
        
        System.out.print("Rozpoczac elekcje? y/n ");
        String elect = br.readLine();
        
        if(elect.equals("y")){
        	try {
				aBully.initiateElection();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        /*
        Registry registry;
    	RemoteInterface ri;
    	for(Map.Entry<String,String> entry: peers.entrySet()){
    		registry = LocateRegistry.getRegistry(""+entry.getValue(),5000);
    		ri = (RemoteInterface)registry.lookup(""+entry.getKey());
    		String s = "";
			try {
				s = ri.giveResponse("NONE");
			} catch (InterruptedException e) {
				System.out.println("Error in main");
				e.printStackTrace();
			}
    		System.out.println("Given response: " + s);
    	}*/
    }

    private String getIP() {
        return this.nodeIP;
    }

	



	
}