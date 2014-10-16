/**
 * 
 */
package com.ldrr.controller;

import interfaces.PhoneBookClientInterfacePOA;
import interfaces.PhoneBookServerInterface;
import interfaces.PhoneBookServerInterfaceHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Properties;
import java.util.TreeMap;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.codec.binary.Base64;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

/**
 * All source code and required libraries are found at the following link:
 * https://github.com/lucasdiegorr/phonebook-corba-client
 * branch: master
 */

/**
 * @author Lucas Diego Reboucas Rocha
 * @email lucas.diegorr@gmail.com
 * @year 2014
 */
public class PhoneBookClient extends PhoneBookClientInterfacePOA implements Runnable{

	private TreeMap<String, Integer> listContacts;
	private PhoneBookServerInterface server;
	private String ipNameService;
	private String portNameService;
	private ORB orb;
	private boolean isChanged;
	private NamingContext naming;
	private NameComponent[] paths;
	private DefaultTableModel model;
	
	/**
	 * @param ip 
	 * @param model 
	 * @param port 
	 * 
	 */
	public PhoneBookClient(String ip, DefaultTableModel model) {
		this.setIpNameService(ip);
		this.setPortNameService("900");
		this.setChanged(false);
		this.setListContacts(new TreeMap<String, Integer>());
		this.model = model;
	}

	/**
	 * Inicia a conexão com o servidor
	 * @return boolean informando de a conexão com o servidor foi realizada
	 */
	public boolean init() {

		Properties properties = new Properties();
		properties.put("org.omg.CORBA.ORBInitialHost", getIpNameService());
		properties.put("org.omg.CORBA.ORBInitialPort", getPortNameService());

		this.setOrb(ORB.init((String[])null, properties));

		Object objectInterface = seekReference();

		if (objectInterface == null) {
			return false;
		}

		this.server = PhoneBookServerInterfaceHelper.narrow(objectInterface);

		registryInServer();
		
		
		synchronizeList();

		return true;
	}

	private void registryInServer() {
		
		try {
			Object objectPOA = getOrb().resolve_initial_references("RootPOA");
			POA rootPOA = POAHelper.narrow(objectPOA);
			
			Object objectNameService = getOrb().resolve_initial_references("NameService");
			setNaming(NamingContextHelper.narrow(objectNameService));
			
			Object objectReference = rootPOA.servant_to_reference(this);
			int index = seekReferenceToIndex(getNaming());
			
			setPaths(new NameComponent[] {new NameComponent("Client", "PhoneBookClient-"+index)});
			getNaming().rebind(getPaths(), objectReference);
			
		} catch (InvalidName e) {
			e.printStackTrace();
		} catch (ServantNotActive e) {
			e.printStackTrace();
		} catch (WrongPolicy e) {
			e.printStackTrace();
		} catch (NotFound e) {
			e.printStackTrace();
		} catch (CannotProceed e) {
			e.printStackTrace();
		} catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
			e.printStackTrace();
		}
	}

	private int seekReferenceToIndex(NamingContext naming2) {
		int index = -1;
		boolean verify = true;
		do {
			try {
				index++;
				NameComponent[] path = {new NameComponent("Client", "PhoneBookClient-"+index)};
				Object objectInterface = naming.resolve(path);

			} catch (NotFound e) {
				verify = false;
			} catch (CannotProceed e) {
				verify = false;
			} catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
				verify = false;
			}
		} while (verify);

		return index;
	}

	/**
	 * Procura as referencias vivas no servidor de nomes. 
	 * Baseada nessa procura é retornada a referencia para o servidor atual
	 * @return Object CORBA
	 */
	private Object seekReference(){
		Object objectInterface = null;
		int index = 0;

		do {
			try {

				Object objectNameService = getOrb().resolve_initial_references("NameService");

				NamingContext naming = NamingContextHelper.narrow(objectNameService);
				NameComponent[] path = {new NameComponent("Server", "PhoneBook-"+index)};

				objectInterface = naming.resolve(path);

			} catch (InvalidName e) {
				index++;
			} catch (NotFound e) {
				index++;
			} catch (CannotProceed e) {
				index++;
			} catch (org.omg.CosNaming.NamingContextPackage.InvalidName e) {
				index++;
			}
		} while ((objectInterface == null) && (index  < 3));

		return objectInterface;
	}

	/**
	 * Insere um contato na lista
	 * @param contactName
	 * @param contactNumber
	 */
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
	 * Sincroniza a lista
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

	/**
	 * @param listContact
	 */
	public void updateListContact(String listContact) {
		ByteArrayInputStream byteArrayInput = new ByteArrayInputStream(Base64.decodeBase64(server.getListContact()));
		TreeMap<String, Integer> list = convertToTreeMap(byteArrayInput);
		setListContacts(list);
		int count = model.getRowCount();
		for (int i = count-1; i >= 0; i--) {
			model.removeRow(i);
		}
		for (String key : getListContacts().keySet()) {
			String [] values = {key, new String(getListContacts().get(key).toString().substring(0, 4)+"-"+getListContacts().get(key).toString().substring(4, 8))};

			model.addRow(values);
		}
	}

	public void removeReference() {
		
	}

	/**
	 * @return the ipNameService
	 */
	public String getIpNameService() {
		return ipNameService;
	}

	/**
	 * @param ipNameService the ipNameService to set
	 */
	public void setIpNameService(String ipNameService) {
		this.ipNameService = ipNameService;
	}

	/**
	 * @return the portNameService
	 */
	public String getPortNameService() {
		return portNameService;
	}

	/**
	 * @param portNameService the portNameService to set
	 */
	public void setPortNameService(String portNameService) {
		this.portNameService = portNameService;
	}

	/**
	 * @return the orb
	 */
	public ORB getOrb() {
		return orb;
	}

	/**
	 * @param orb the orb to set
	 */
	public void setOrb(ORB orb) {
		this.orb = orb;
	}

	/**
	 * @return the naming
	 */
	public NamingContext getNaming() {
		return naming;
	}

	/**
	 * @param naming the naming to set
	 */
	public void setNaming(NamingContext naming) {
		this.naming = naming;
	}

	/**
	 * @return the paths
	 */
	public NameComponent[] getPaths() {
		return paths;
	}

	/**
	 * @param paths the paths to set
	 */
	public void setPaths(NameComponent[] paths) {
		this.paths = paths;
	}

}
