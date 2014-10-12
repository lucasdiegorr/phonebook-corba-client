/**
 * 
 */
package controller;

import interfaces.PhoneBookServerInterface;
import interfaces.PhoneBookServerInterfaceHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.codec.binary.Base64;
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
public class PhoneBookClient implements Runnable{

	private TreeMap<String, Integer> listContacts;
	private PhoneBookServerInterface server;
	private String ipNameService;
	private String portNameService;
	private ORB orb;
	private boolean isChanged;
	/**
	 * @param ip 
	 * @param port 
	 * 
	 */
	public PhoneBookClient(String ip) {
		this.ipNameService = ip;
		this.portNameService = "900";
		this.setChanged(false);
		this.setListContacts(new TreeMap<String, Integer>());
	}

	public boolean init() {

		Properties properties = new Properties();
		properties.put("org.omg.CORBA.ORBInitialHost", ipNameService);
		properties.put("org.omg.CORBA.ORBInitialPort", portNameService);

		this.orb = ORB.init((String[])null, properties);

		Object objectInterface = seekReference();

		if (objectInterface == null) {
			return false;
		}

		this.server = PhoneBookServerInterfaceHelper.narrow(objectInterface);

		synchronizeList();

		return true;
	}

	private Object seekReference(){
		Object objectInterface = null;
		int index = 0;

		do {
			try {

				Object objectNameService = orb.resolve_initial_references("NameService");

				NamingContext naming = NamingContextHelper.narrow(objectNameService);
				NameComponent[] path = {new NameComponent("Server", "PhoneBook-"+index)};

				objectInterface = naming.resolve(path);

			} catch (InvalidName e) {
				index++;
				e.printStackTrace();
			} catch (NotFound e) {
				index++;
				e.printStackTrace();
			} catch (CannotProceed e) {
				index++;
				e.printStackTrace();
			} catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
				index++;
				e.printStackTrace();
			}
		} while ((objectInterface == null) && (index  < 3));

		return objectInterface;
	}

	public void insertContact(String contactName, int contactNumber) {
		this.getListContacts().put(contactName, contactNumber);
		this.server.insertContact(contactName, contactNumber);
	}

	/**
	 * @return the listContacts
	 */
	public TreeMap<String, Integer> getListContacts() {
		return listContacts;
	}

	/**
	 * @param listContacts the listContacts to set
	 */
	private void setListContacts(TreeMap<String, Integer> listContacts) {
		this.listContacts = listContacts;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (true) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronizeList();
		}
	}

	/**
	 * 
	 */
	private void synchronizeList() {
		try {
			ByteArrayInputStream byteArrayInput = new ByteArrayInputStream(Base64.decodeBase64(server.getListContact()));
			TreeMap<String, Integer> list = convertToTreeMap(byteArrayInput);
			if (!this.listContacts.equals(list)) {
				setListContacts(list);
				setChanged(true);
			}
		} catch (Exception e) {
			init();
			synchronizeList();
		}
	}

	@SuppressWarnings("unchecked")
	private TreeMap<String, Integer> convertToTreeMap(ByteArrayInputStream byteArrayInput) {
		TreeMap<String, Integer> list = null;
		try {
			list = (TreeMap<String, Integer>) new ObjectInputStream(byteArrayInput).readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * @return the isUpdated
	 */
	public boolean isChanged() {
		return isChanged;
	}

	/**
	 * @param isUpdated the isUpdated to set
	 */
	public void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}

	/**
	 * @param contact
	 */
	public void deleteContact(String contact) {
		this.server.deleteContact(contact);
	}

	/**
	 * @param text
	 * @param contactNumber
	 * @param valueAt
	 * @param valueAt2
	 */
	public void updateContact(String newName, int newNumber, String oldName, int oldNumber) {
		try {
			if (!newName.equals(oldName)) {
				this.server.updateContactName(newName, oldName);
			}else if(newNumber != oldNumber) {
				this.server.updateContactNumber(oldName, newNumber);
			}
		} catch (Exception e) {
			init();
			updateContact(newName, newNumber, oldName, oldNumber);
			e.printStackTrace();
		}
	}

}
