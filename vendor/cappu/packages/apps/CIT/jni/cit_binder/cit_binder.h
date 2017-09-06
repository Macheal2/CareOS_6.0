/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */


#include <utils/KeyedVector.h>
#include <utils/RefBase.h>
#include <binder/IInterface.h>
#include <binder/Parcel.h>
#include <utils/String16.h>
#include <utils/threads.h>

#include <sys/socket.h>
#include <sys/un.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>
#include <pthread.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <utils/Log.h>

#include<sys/mount.h>
//#include "../../../../kernel/include/mtd/mtd-abi.h"
#include "../../../../kernel-3.18/include/uapi/mtd/mtd-abi.h"
#include <utils/String16.h>
#include <cutils/atomic.h>
#include <utils/Errors.h>
#include <binder/IServiceManager.h>
#include <utils/String16.h>
#include <binder/IPCThreadState.h>
#include <binder/ProcessState.h>
#include <utils/Vector.h>

/*		static final int TRANSACTION_calisetPsCali_valid = (IBinder.FIRST_CALL_TRANSACTION + 7);
		static final int TRANSACTION_calisetGsCali_x = (IBinder.FIRST_CALL_TRANSACTION + 8);
		static final int TRANSACTION_calisetGsCali_y = (IBinder.FIRST_CALL_TRANSACTION + 9);
		static final int TRANSACTION_calisetGsCali_z = (IBinder.FIRST_CALL_TRANSACTION + 10);
		static final int TRANSACTION_caliGsCali = (IBinder.FIRST_CALL_TRANSACTION + 11);
*/
using namespace android;
enum {
     TRANSACTION_getFlag = IBinder::FIRST_CALL_TRANSACTION,
TRANSACTION_SET_CIT_AUTOTEST_FLAG ,
    TRANSACTION_setPsCali_close,
    TRANSACTION_setPsCali_far,
    TRANSACTION_setPsCali_valid,
TRANSACTION_calisetGsCali_x,
TRANSACTION_calisetGsCali_y,
TRANSACTION_calisetGsCali_z,
TRANSACTION_caliGsCali,
TRANSACTION_ClrGsCali,
TRANSACTION_FirstPwrOnLed_getFlag,
TRANSACTION_FirstPwrOnLed_setFlag,
};
/*
    virtual int  calisetGsCali_x(int data)=0;
    virtual int  calisetGsCali_y(int data)=0;
    virtual int calisetGsCali_z(int data)=0;

*/

class ICitBinder:public IInterface {
public:
    DECLARE_META_INTERFACE(CitBinder);
       virtual int setCitAutoTestFlag(int data)=0;
    virtual unsigned char getFlag(int data)=0;
    virtual int  setPsCali_close(int data)=0;
    virtual int  setPsCali_far(int data)=0;
    virtual int  setPsCali_valid(int data)=0;
    virtual int  calisetGsCali_x(int data)=0;
    virtual int  calisetGsCali_y(int data)=0;
    virtual int calisetGsCali_z(int data)=0;
    virtual int caliGsCali()=0;
virtual int ClrGsCali()=0;
virtual unsigned int getFirstPwrOn_Flag()=0;
virtual int setFirstPwrOn_Flag()=0;
};


/*


    int  calisetGsCali_x(int data) {return 1;}
	int  calisetGsCali_x(int data) {return 1;}
	int  calisetGsCali_x(int data) {return 1;}
*/
class BpCitBinder: public android::BpInterface<ICitBinder>
{
public:
    BpCitBinder(const android::sp<android::IBinder>& impl)
	: android::BpInterface<ICitBinder>(impl)
        {
        }
   	 unsigned char getFlag(int data) {return 0;}
     int setCitAutoTestFlag(int data)  {return 1;}
    	int  setPsCali_close(int data) {return 1;}
	int  setPsCali_far(int data) {return 1;}
	int  setPsCali_valid(int data) {return 1;}
	int  calisetGsCali_x(int data) {return 1;}
	int  calisetGsCali_y(int data) {return 1;}
	int  calisetGsCali_z(int data) {return 1;}
    int caliGsCali() {return 1;}
    int ClrGsCali() {return 1;}
unsigned int getFirstPwrOn_Flag() {return 1;}
int setFirstPwrOn_Flag() {return 1;}
};

class BnCitBinder : public BnInterface<ICitBinder>
{
public:
    status_t onTransact(uint32_t code,
			const Parcel &data,
			Parcel *reply,
			uint32_t flags);
    
};

class CitBinder : public BnCitBinder
{

public:
    static  void instantiate();
    CitBinder();
    ~CitBinder() {}
     virtual int setCitAutoTestFlag(int data);
    virtual unsigned char getFlag(int data);
	virtual int  setPsCali_close(int data);
	virtual int  setPsCali_far(int data);
	virtual int  setPsCali_valid(int data);
    virtual int  calisetGsCali_x(int data);
    virtual int  calisetGsCali_y(int data);
    virtual int calisetGsCali_z(int data);
    virtual int caliGsCali();
    virtual int ClrGsCali();
virtual unsigned int getFirstPwrOn_Flag();
virtual int setFirstPwrOn_Flag();
};


IMPLEMENT_META_INTERFACE(CitBinder, "CitBinder")

