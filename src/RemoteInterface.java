import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Hashtable;

/**
 * Created by 
 */
public interface RemoteInterface extends Remote{
	
	
	public String giveResponse(String message) throws InterruptedException, NotBoundException, RemoteException;

	public void announce(String nodeID) throws RemoteException;


	public Hashtable<String, String> getDetails()  throws RemoteException;

	public String getCoordinatorID()  throws RemoteException;
}