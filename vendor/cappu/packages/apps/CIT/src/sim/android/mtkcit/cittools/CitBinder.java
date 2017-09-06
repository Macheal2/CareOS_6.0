package sim.android.mtkcit.cittools;

import java.lang.String;
import java.util.List;

import android.os.RemoteException;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Binder;
import android.os.Parcel;
import android.util.Log;

public interface CitBinder extends IInterface {
//	public static abstract class Stub extends Binder implements CitBinder {
//		private static final String DESCRIPTOR = "CitBinder";
//
//		public Stub() {
//			this.attachInterface(this, DESCRIPTOR);
//		}
//
//		public static CitBinder asInterface(IBinder obj) {
//			if ((obj == null)) {
//				return null;
//			}
//			IInterface iin = (IInterface) obj.queryLocalInterface(DESCRIPTOR);
//			if (((iin != null) && (iin instanceof CitBinder))) {
//				return ((CitBinder) iin);
//			}
//			return new CitBinder.Stub.Proxy(obj);
//		}
//
//		public IBinder asBinder() {
//			return this;
//		}

//		public boolean onTransact(int code, Parcel data, Parcel reply, int flags)
//				throws RemoteException {
//			return true;
//		}
//
//		private static class Proxy implements CitBinder {
//			private IBinder mRemote;
//
//			Proxy(IBinder remote) {
//				mRemote = remote;
//			}
//
//			public IBinder asBinder() {
//				return mRemote;
//			}
//
//			public java.lang.String getInterfaceDescriptor() {
//				return DESCRIPTOR;
//			}
//
//			public byte getFlag(int i) throws RemoteException {
//				Parcel _data = Parcel.obtain();
//				Parcel _reply = Parcel.obtain();
//				byte _result;
//				try {
//					Log.v("CitBinder", "getFlag  i = "+ i);
//
//					_data.writeInterfaceToken(DESCRIPTOR);
//					_data.writeInt(i);
//					mRemote.transact(Stub.TRANSACTION_getFlag, _data, _reply, 0);
//					_result = _reply.readByte();
//				} finally {
//					_reply.recycle();
//					_data.recycle();
//				}
//				return _result;
//			}
//
//			public boolean setCitAutoTestFlag(int i) throws RemoteException {
//				Log.v("CitBinder", "setCitAutoTestFlag  i = "+ i);
//				Parcel _data = Parcel.obtain();
//				Parcel _reply = Parcel.obtain();
//				boolean _result;
//				try {
//					_data.writeInterfaceToken(DESCRIPTOR);
//					_data.writeInt(i);
//					mRemote.transact(Stub.TRANSACTION_SET_CIT_AUTOTEST_FLAG,
//							_data, _reply, 0);
//					_result = (0 != _reply.readInt());
//				} finally {
//					_reply.recycle();
//					_data.recycle();
//				}
//				return _result;
//			}
//
//			public boolean setPsCali_close(int data) throws RemoteException {
//				Parcel _data = Parcel.obtain();
//				Parcel _reply = Parcel.obtain();
//				boolean _result;
//				try {
//					_data.writeInterfaceToken(DESCRIPTOR);
//					_data.writeInt(data);
//					Log.v("feng", "setPsCali_close_data=" + data);
//					mRemote.transact(TRANSACTION_setPsCali_close, _data,
//							_reply, 0);
//					_result = (0 != _reply.readInt());
//				} finally {
//					_reply.recycle();
//					_data.recycle();
//				}
//				return _result;
//			}
//
//			public boolean setPsCali_far(int data) throws RemoteException {
//				Parcel _data = Parcel.obtain();
//				Parcel _reply = Parcel.obtain();
//				boolean _result;
//				try {
//					_data.writeInterfaceToken(DESCRIPTOR);
//					_data.writeInt(data);
//					Log.v(DESCRIPTOR, "setPsCali_far_data=" + data);
//					mRemote.transact(TRANSACTION_setPsCali_far, _data, _reply,
//							0);
//					_result = (0 != _reply.readInt());
//				} finally {
//					_reply.recycle();
//					_data.recycle();
//				}
//				return _result;
//			}
//
//			public boolean setPsCali_valid(int data) throws RemoteException {
//				Parcel _data = Parcel.obtain();
//				Parcel _reply = Parcel.obtain();
//				boolean _result;
//				try {
//					_data.writeInterfaceToken(DESCRIPTOR);
//					_data.writeInt(data);
//					Log.v(DESCRIPTOR, "setPsCali_valid_data=" + data);
//					mRemote.transact(TRANSACTION_calisetPsCali_valid, _data,
//							_reply, 0);
//					_result = (0 != _reply.readInt());
//				} finally {
//					_reply.recycle();
//					_data.recycle();
//				}
//				return _result;
//			}
//
//			/**
//			 * set the g-sensor of x
//			 */
//			public boolean setGsCali_x(int data) throws RemoteException {
//				Parcel _data = Parcel.obtain();
//				Parcel _reply = Parcel.obtain();
//				boolean _result;
//				try {
//					_data.writeInterfaceToken(DESCRIPTOR);
//					_data.writeInt(data);
//					Log.v(DESCRIPTOR, "setGsCali_x=" + data);
//					mRemote.transact(TRANSACTION_calisetGsCali_x, _data,
//							_reply, 0);
//					_result = (0 != _reply.readInt());
//				} finally {
//					_reply.recycle();
//					_data.recycle();
//				}
//				return _result;
//			}
//
//			/**
//			 * set the g-sensor of y
//			 */
//			public boolean setGsCali_y(int data) throws RemoteException {
//				Parcel _data = Parcel.obtain();
//				Parcel _reply = Parcel.obtain();
//				boolean _result;
//				try {
//					_data.writeInterfaceToken(DESCRIPTOR);
//					_data.writeInt(data);
//					Log.v(DESCRIPTOR, "setGsCali_y=" + data);
//					mRemote.transact(TRANSACTION_calisetGsCali_y, _data,
//							_reply, 0);
//					_result = (0 != _reply.readInt());
//				} finally {
//					_reply.recycle();
//					_data.recycle();
//				}
//				return _result;
//			}
//
//			/**
//			 * set the g-sensor of z
//			 */
//			public boolean setGsCali_z(int data) throws RemoteException {
//				Parcel _data = Parcel.obtain();
//				Parcel _reply = Parcel.obtain();
//				boolean _result;
//				try {
//					_data.writeInterfaceToken(DESCRIPTOR);
//					_data.writeInt(data);
//					Log.v(DESCRIPTOR, "setGsCali_y=" + data);
//					mRemote.transact(TRANSACTION_calisetGsCali_z, _data,
//							_reply, 0);
//					_result = (0 != _reply.readInt());
//				} finally {
//					_reply.recycle();
//					_data.recycle();
//				}
//				return _result;
//			}
//
//			/**
//			 * set the g-sensor of z
//			 */
//			public int GsCali() throws RemoteException {
//				Parcel _data = Parcel.obtain();
//				Parcel _reply = Parcel.obtain();
//				int _result;
//				try {
//					_data.writeInterfaceToken(DESCRIPTOR);
//					Log.v(DESCRIPTOR, "GsCali");
//					mRemote.transact(TRANSACTION_caliGsCali, _data, _reply, 0);
//					_result=_reply.readInt();
//				} finally {
//					_reply.recycle();
//					_data.recycle();
//				}
//				return _result;
//
//			}
//
//			/**
//			 * clear the g-sensor
//			 */
//			public int ClrGsCali() throws RemoteException {
//				Parcel _data = Parcel.obtain();
//				Parcel _reply = Parcel.obtain();
//				int _result;
//				try {
//					_data.writeInterfaceToken(DESCRIPTOR);
//					Log.v(DESCRIPTOR, "ClrGsCali");
//					mRemote.transact(TRANSACTION_ClrGsCali, _data, _reply, 0);
//					_result = _reply.readInt();
//				} finally {
//					_reply.recycle();
//					_data.recycle();
//				}
//				return _result;
//
//			}
//		}
//
//		static final int TRANSACTION_getFlag = (IBinder.FIRST_CALL_TRANSACTION + 0);
//		static final int TRANSACTION_SET_CIT_AUTOTEST_FLAG = (IBinder.FIRST_CALL_TRANSACTION + 1);
//
//		static final int TRANSACTION_setPsCali_close = (IBinder.FIRST_CALL_TRANSACTION + 2);
//		static final int TRANSACTION_setPsCali_far = (IBinder.FIRST_CALL_TRANSACTION + 3);
//		static final int TRANSACTION_calisetPsCali_valid = (IBinder.FIRST_CALL_TRANSACTION + 4);
//		static final int TRANSACTION_calisetGsCali_x = (IBinder.FIRST_CALL_TRANSACTION + 5);
//		static final int TRANSACTION_calisetGsCali_y = (IBinder.FIRST_CALL_TRANSACTION + 6);
//		static final int TRANSACTION_calisetGsCali_z = (IBinder.FIRST_CALL_TRANSACTION + 7);
//		static final int TRANSACTION_caliGsCali = (IBinder.FIRST_CALL_TRANSACTION + 8);
//		static final int TRANSACTION_ClrGsCali = (IBinder.FIRST_CALL_TRANSACTION + 9);
//
//	}
//
//	public byte getFlag(int i) throws RemoteException;
//
//	public boolean setPsCali_close(int i) throws RemoteException;
//
//	public boolean setPsCali_far(int i) throws RemoteException;
//
//	public boolean setPsCali_valid(int i) throws RemoteException;
//
//	public boolean setGsCali_x(int x) throws RemoteException;
//
//	public boolean setGsCali_y(int y) throws RemoteException;
//
//	public boolean setGsCali_z(int z) throws RemoteException;
//
//	public int GsCali() throws RemoteException;
//
//	public int ClrGsCali() throws RemoteException;
//
//	public boolean setCitAutoTestFlag(int i) throws RemoteException;

}
