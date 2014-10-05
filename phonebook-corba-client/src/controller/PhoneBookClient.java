/**
 * 
 */
package controller;

import interfaces.PhoneBookServerInterface;
import interfaces.PhoneBookServerInterfaceHelper;

import java.util.HashMap;
import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

/**
 * @author Lucas Diego Reboucas Rocha
 * @email lucas.diegorr@gmail.com
 * @year 2014
 */
public class PhoneBookClient {
	
	private HashMap<String, Integer> listContacts;
	private PhoneBookServerInterface server;
	private String ipNameService;
	private String portNameService;
	private ORB orb;
	
	/**
	 * @param ip 
	 * @param port 
	 * 
	 */
	public PhoneBookClient(String ip, String port) {
		this.ipNameService = ip;
		this.portNameService = port;
		this.setListContacts(new HashMap<String, Integer>());
	}
	
	public void init() {
		
		Properties properties = new Properties();
		properties.put("org.omg.CORBA.ORBInitialHost", ipNameService);
		properties.put("org.omg.CORBA.ORBInitialPort", portNameService);
		
		this.orb = ORB.init((String[])null, properties);
		System.out.println("Iniciado o ORB");
		try {
			Object objectNameService = orb.resolve_initial_references("NameService");
			
			NamingContext naming = NamingContextHelper.narrow(objectNameService);
			NameComponent[] path = {new NameComponent("Server", "PhoneBook")};
			
			Object objectInterface = naming.resolve(path);
			System.out.println("Obtido a interface");
			this.server = PhoneBookServerInterfaceHelper.narrow(objectInterface);
			
		} catch (InvalidName e) {
			e.printStackTrace();
		} catch (NotFound e) {
			e.printStackTrace();
		} catch (CannotProceed e) {
			e.printStackTrace();
		} catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
			e.printStackTrace();
		}
	}
	
	public void insertContact(String contactName, int contactNumber) {
		this.getListContacts().put(contactName, contactNumber);
		this.server.insertContact(contactName, contactNumber);
	}

	/**
	 * @return the listContacts
	 */
	public HashMap<String, Integer> getListContacts() {
		return listContacts;
	}

	/**
	 * @param listContacts the listContacts to set
	 */
	public void setListContacts(HashMap<String, Integer> listContacts) {
		this.listContacts = listContacts;
	}
}
