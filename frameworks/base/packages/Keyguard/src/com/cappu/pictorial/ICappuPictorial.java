/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /home/hmq/code/35_65C/pictorial_user/alps/vendor/magcomm/packages/apps/CappuPictorial/app/src/main/aidl/com/cappu/pictorial/ICappuPictorial.aidl
 */
package com.cappu.pictorial;
// Declare any non-default types here with import statements

public interface ICappuPictorial extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.cappu.pictorial.ICappuPictorial
{
private static final java.lang.String DESCRIPTOR = "com.cappu.pictorial.ICappuPictorial";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.cappu.pictorial.ICappuPictorial interface,
 * generating a proxy if needed.
 */
public static com.cappu.pictorial.ICappuPictorial asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.cappu.pictorial.ICappuPictorial))) {
return ((com.cappu.pictorial.ICappuPictorial)iin);
}
return new com.cappu.pictorial.ICappuPictorial.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_showCappuPictorialImg:
{
data.enforceInterface(DESCRIPTOR);
this.showCappuPictorialImg();
reply.writeNoException();
return true;
}
case TRANSACTION_getCappuPictorialPath:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _result = this.getCappuPictorialPath();
reply.writeNoException();
reply.writeString(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.cappu.pictorial.ICappuPictorial
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
@Override public void showCappuPictorialImg() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_showCappuPictorialImg, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public java.lang.String getCappuPictorialPath() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getCappuPictorialPath, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_showCappuPictorialImg = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getCappuPictorialPath = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
}
/**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
public void showCappuPictorialImg() throws android.os.RemoteException;
public java.lang.String getCappuPictorialPath() throws android.os.RemoteException;
}
