package interfaces;


/**
* interfaces/PhoneBookServerInterfaceOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from serverIDL.idl
* Quinta-feira, 2 de Outubro de 2014 17h40min05s GMT-03:00
*/

public interface PhoneBookServerInterfaceOperations 
{
  void insertContact (String contactName, int contactNumber);
  String getListContact ();
  void updateContactName (String contactNameUpdated, String contactNameOld);
  void updateContactNumber (String contactName, int contactNumberOld);
  void deleteContact (String contact);
} // interface PhoneBookServerInterfaceOperations