package interfaces;

/**
* interfaces/PhoneBookClientInterfaceHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from clientIDL.idl
* Quinta-feira, 16 de Outubro de 2014 17h06min47s GMT-03:00
*/

public final class PhoneBookClientInterfaceHolder implements org.omg.CORBA.portable.Streamable
{
  public interfaces.PhoneBookClientInterface value = null;

  public PhoneBookClientInterfaceHolder ()
  {
  }

  public PhoneBookClientInterfaceHolder (interfaces.PhoneBookClientInterface initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = interfaces.PhoneBookClientInterfaceHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    interfaces.PhoneBookClientInterfaceHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return interfaces.PhoneBookClientInterfaceHelper.type ();
  }

}
