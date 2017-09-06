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



#include "cit_binder.h"
#include <cutils/properties.h>

#include <CFG_PRODUCT_INFO_File.h>
#include <Custom_NvRam_LID.h>
//#include "../../../../../mediatek/external/nvram/libnvram/libnvram.h"
//#include "../../../../../mediatek/external/nvram/libfile_op/libfile_op.h"
#include "../../../../../mediatek/proprietary/external/nvram/libnvram/libnvram.h"
#include "../../../../../mediatek/proprietary/external/nvram/libfile_op/libfile_op.h"
#include <CFG_Custom1_File.h>
#include <CFG_HWMON_File.h>
#include <CFG_file_lid.h>
//#include <CFG_Custom2_File.h>

#include <jni.h>
#include <utils/Log.h>
#include <fcntl.h>
#include <linux/sensors_io.h>
#define	GSENSOR_NAME "/dev/gsensor"
#define LOG_TAG "CitBinder"
bool setresult ;
int x;
int y;
int z;
void CitBinder::instantiate() {
 /*   defaultServiceManager()->addService(
	descriptor, new CitBinder());
       if(ret != 0){
       //LOGE(" serviceManager not ready");
       exit(1);
   }
   LOGV("Haman, serviceManager work well");
}
*/

       //LOGI("instantiate()------.sleep(20)");
        sleep(20); 
    while(true){
      //  status_t ret = defaultServiceManager()->addService(
        //    descriptor, new CitBinder());
    status_t ret = defaultServiceManager()->addService(String16("CitBinder"), new CitBinder());
        if(ret == 0){
            ////LOGI("[CitBinder ]register OK.");
            break;
        }

        //LOGE("[CitBinder]register FAILED. retrying in 5sec.");

        
        sleep(5); 
    } 
}

CitBinder::CitBinder() {
    //LOGI("CitBinder created");
    //NVM_AddBackupFileNum(AP_CFG_REEB_PRODUCT_INFO_LID);
}

status_t BnCitBinder::onTransact(uint32_t code,
			       const Parcel &data,
			       Parcel *reply,
			       uint32_t flags) {

    //LOGI("OnTransact   (%u,%u)", code, flags);
        
    switch(code) {

 case TRANSACTION_SET_CIT_AUTOTEST_FLAG: {
	//LOGI("setCitAutoTestFlag\n");
	data.enforceInterface (descriptor);
	reply->writeInt32 (setCitAutoTestFlag(data.readInt32()));
	return NO_ERROR;
    } break;

    case TRANSACTION_getFlag: {
	//LOGI("getFlag\n");
	data.enforceInterface (descriptor);
	reply->writeInt32 (getFlag(data.readInt32()));
	//LOGI("getFlag done\n");
	return NO_ERROR;
    } break;
    case TRANSACTION_setPsCali_close: {
	//LOGI("setPsCali_close\n");
	data.enforceInterface (descriptor);
	reply->writeInt32 (setPsCali_close(data.readInt32()));
	//LOGI("setPsCali_close done\n");
	return NO_ERROR;
    } break;
    case TRANSACTION_setPsCali_far: {
	//LOGI("setPsCali_far\n");
	data.enforceInterface (descriptor);
	reply->writeInt32 (setPsCali_far(data.readInt32()));
	//LOGI("setPsCali_far done\n");
	return NO_ERROR;
    } break;
   case TRANSACTION_setPsCali_valid: {
	//LOGI("setPsCali_valid\n");
	data.enforceInterface (descriptor);
	reply->writeInt32 (setPsCali_valid(data.readInt32()));
	//LOGI("setPsCali_valid done\n");
	return NO_ERROR;
    } break;

 case TRANSACTION_calisetGsCali_x: {
	//LOGI("setGsCali_x\n");
	data.enforceInterface (descriptor);
	reply->writeInt32 (calisetGsCali_x(data.readInt32()));
	//LOGI("setGsCali_x done x= %d\n",x);
	return NO_ERROR;
    } break;
    case TRANSACTION_calisetGsCali_y: {
	//LOGI("calisetGsCali_y\n");
	data.enforceInterface (descriptor);
	reply->writeInt32 (calisetGsCali_y(data.readInt32()));
	//LOGI("calisetGsCali_y done y =%d\n",y);
	return NO_ERROR;
    } break;
    case TRANSACTION_calisetGsCali_z: {
	//LOGI("calisetGsCali_z\n");
	data.enforceInterface (descriptor);
	reply->writeInt32 (calisetGsCali_z(data.readInt32()));
	//LOGI("calisetGsCali_z done z=%d\n",z);
	return NO_ERROR;
    } break;
   case TRANSACTION_caliGsCali: {
	//LOGI("_caliGsCali\n");
	data.enforceInterface (descriptor);
	reply->writeInt32 (caliGsCali());
	//LOGI("_caliGsCali done\n");
	return NO_ERROR;
    } break;
   case TRANSACTION_ClrGsCali: {
	//LOGI("_ClrGsCali\n");
	data.enforceInterface (descriptor);
	reply->writeInt32 (ClrGsCali());
	//LOGI("_ClrGsCali done\n");
	return NO_ERROR;
    } break;
   case TRANSACTION_FirstPwrOnLed_getFlag: {
	//LOGI("_getFirstPwrOn_Flag\n");
	data.enforceInterface (descriptor);
	reply->writeInt32 (getFirstPwrOn_Flag());
	//LOGI("_getFirstPwrOn_Flag done\n");
	return NO_ERROR;
    } break;

case TRANSACTION_FirstPwrOnLed_setFlag: {
	//LOGI("_setFirstPwrOn_Flag\n");
	data.enforceInterface (descriptor);
	reply->writeInt32 (setFirstPwrOn_Flag());
	//LOGI("_setFirstPwrOn_Flag done\n");
	return NO_ERROR;
    } break;

    default:
	return BBinder::onTransact(code, data, reply, flags);
    }
    return NO_ERROR;
}


unsigned char CitBinder::getFlag(int data)
{
   int result = 0;
  //LOGI("data= %d",data);
   F_ID sncit_nvram_fd;
   int file_lid = AP_CFG_REEB_PRODUCT_INFO_LID;
   int i = 0,rec_sizem,rec_size,rec_num ;
   unsigned char ret=0 , num = 1;
   PRODUCT_INFO snCitParam;
   memset(&snCitParam,0,sizeof(snCitParam));

   sncit_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISREAD);

   result = read(sncit_nvram_fd.iFileDesc, &snCitParam , rec_size*rec_num);
//   if(result)
 //LOGI("SNcit.flag read success result = %d",result);
//   else
   	//LOGE("sn cit setflag fail");

   NVM_CloseFileDesc(sncit_nvram_fd);
for(i = 0;i <data ;i++)    {   
   //LOGI("i= %d",i);
 if(snCitParam.barcode[i]==1) {
   //LOGI("SNcit.flag  snCitParam.barcode[%d]= %d",i,snCitParam.barcode[i]);
  int j =0; 
   unsigned char  num = 1;
   for( j;j<data-i-1;j++)  {

       num = num *2 ;
   }
   //LOGI("SNcit.flag num= %d",num);
   ret = ret +num;

   //LOGI("SNcit.flag  ret= %d",ret);
}

}/*

   NVM_CloseFileDesc(sncit_nvram_fd);
   if(snCitParam.barcode[0]==1)
     	ret=1;
   if(snCitParam.barcode[1]==1)
     	ret=2+ret;
   if(snCitParam.barcode[2]==1)
     	ret=4+ret; 	
   if(snCitParam.barcode[3]==1)
     	ret=8+ret;
	//LOGI("ret =%d\n",ret);
   return ret;*/
	//LOGI("ret =%d\n",ret);

   return ret;
}


int CitBinder::setCitAutoTestFlag(int data)
{
 int result = 0;
 F_ID sncit_nvram_fd;
   int file_lid = AP_CFG_REEB_PRODUCT_INFO_LID;
   int i = 0,rec_sizem,rec_size,rec_num,loacation;
   loacation = data;
   PRODUCT_INFO snCitParam;
 //LOGI("setCitAutoTestFlag  loacation = %d",loacation);
   
   memset(&snCitParam,0,sizeof(snCitParam));
   sncit_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISREAD);
   result = read(sncit_nvram_fd.iFileDesc, &snCitParam , rec_size*rec_num);
   if(result)
    {
   		snCitParam.barcode[loacation]=1;
        //LOGI("setCitAutoTestFlag  succeseed");
  	}
  	else
  	{
  		//LOGE("setCitAutoTestFlag fail");
    }
   //LOGI("SNcit.flag  snCitParam.barcode[]= %d",snCitParam.barcode[loacation]);
    NVM_CloseFileDesc(sncit_nvram_fd);
   sncit_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISWRITE);
   result = write(sncit_nvram_fd.iFileDesc, &snCitParam , rec_size*rec_num);
         NVM_CloseFileDesc(sncit_nvram_fd);
//   FileOp_BackupToBinRegion_All();
	//LOGI("setCitAutoTestFlag done %d\n",loacation);
   return result;
}


///////////////////////////////////////ps_cali add start//////////////////////////////
#if 0//defined (SUPPORT_PS_CALI)
File_Custom1_Struct g_ps_nvram = {};
int CitBinder::setPsCali_close(int data)
{

   	int result = 0;
	F_ID ps_nvram_fd = {0};
	int rec_size = 0;
	int rec_num = 0;
	LOGV("setPsCali_close data: %d,%d,%d\n",data );
	ps_nvram_fd = NVM_GetFileDesc(AP_CFG_CUSTOM_FILE_CUSTOM1_LID, &rec_size, &rec_num, ISWRITE);
//	//LOGE("[LONG][ALS/PS]read:FD %d rec_size %d rec_num %d\n", ps_nvram_fd.iFileDesc, rec_size, rec_num);

	g_ps_nvram.offset[0]=data;
	//g_ps_nvram.offset[1]=ps_cali_temp->far_away;
	//g_ps_nvram.offset[2]=ps_cali_temp->valid;

////LOGE("[LONG][ALS/PS]read: %d,%d,%d\n", g_ps_nvram.offset[0],g_ps_nvram.offset[1],g_ps_nvram.offset[2]);

	if (rec_size != write(ps_nvram_fd.iFileDesc, &g_ps_nvram, rec_size)){
		//LOGE("[LONG][ALS/PS]write to NVRAM ERR\r\n");
		return -1;
	}	
	//LOGE("[LONG][ALS/PS]read: %d,%d,%d\n", g_ps_nvram.offset[0],g_ps_nvram.offset[1],g_ps_nvram.offset[2]);
	NVM_CloseFileDesc(ps_nvram_fd);

//	FileOp_BackupToBinRegion_All();	
	//LOGE("setPsCali_close  FileOp_BackupToBinRegion_All");
  	 return result;

}

int CitBinder::setPsCali_far(int data)
{
   	int result = 0;
	F_ID ps_nvram_fd = {0};
	int rec_size = 0;
	int rec_num = 0;
	LOGV("setPsCali_far data: %d,%d,%d\n",data );
#if 0
	ps_nvram_fd = NVM_GetFileDesc(AP_CFG_CUSTOM_FILE_CUSTOM1_LID, &rec_size, &rec_num, ISREAD);
	//printf("[LONG][ALS/PS]read:FD %d rec_size %d rec_num %d\n", ps_nvram_fd.iFileDesc, rec_size, rec_num);


	if (rec_size != read(ps_nvram_fd.iFileDesc, &g_ps_nvram, rec_size)){
		return -1;
	}
	//LOGE("[LONG][ALS/PS]read: %d,%d,%d\n", g_ps_nvram.offset[0],g_ps_nvram.offset[1],g_ps_nvram.offset[2]);
	NVM_CloseFileDesc(ps_nvram_fd);
#endif 


	ps_nvram_fd = NVM_GetFileDesc(AP_CFG_CUSTOM_FILE_CUSTOM1_LID, &rec_size, &rec_num, ISWRITE);
//	//LOGE("[LONG][ALS/PS]read:FD %d rec_size %d rec_num %d\n", ps_nvram_fd.iFileDesc, rec_size, rec_num);

	g_ps_nvram.offset[1]=data;
	//g_ps_nvram.offset[1]=ps_cali_temp->far_away;
	//g_ps_nvram.offset[2]=ps_cali_temp->valid;

////LOGE("[LONG][ALS/PS]read: %d,%d,%d\n", g_ps_nvram.offset[0],g_ps_nvram.offset[1],g_ps_nvram.offset[2]);

	if (rec_size != write(ps_nvram_fd.iFileDesc, &g_ps_nvram, rec_size)){
		//LOGE("[LONG][ALS/PS]write to NVRAM ERR\r\n");
		return -1;
	}	
	NVM_CloseFileDesc(ps_nvram_fd);
	//LOGE("setPsCali_far   FileOp_BackupToBinRegion_All");
//	FileOp_BackupToBinRegion_All();	

  	 return result;
}


int CitBinder::setPsCali_valid(int data)
{
   	int result = 0;
	F_ID ps_nvram_fd = {0};
	int rec_size = 0;
	int rec_num = 0;

	ps_nvram_fd = NVM_GetFileDesc(AP_CFG_CUSTOM_FILE_CUSTOM1_LID, &rec_size, &rec_num, ISWRITE);

	g_ps_nvram.offset[2]=data;
	if (rec_size != write(ps_nvram_fd.iFileDesc, &g_ps_nvram, rec_size)){
		//LOGE("[LONG][ALS/PS]write to NVRAM ERR\r\n");
		return -1;
	}	
	NVM_CloseFileDesc(ps_nvram_fd);

	//LOGE("setPsCali_vali  FileOp_BackupToBinRegion_All");
//	FileOp_BackupToBinRegion_All();	

  	 return result;
}
#else

int CitBinder::setPsCali_close(int data)
{

	return 0;
}

int CitBinder::setPsCali_far(int data)
{
	return 0;
}

int CitBinder::setPsCali_valid(int data)
{
	return 0;
}
#endif

///////////////////////////////////////ps_cali add end//////////////////////////////

///////////////////////////////////////gs_cali add start//////////////////////////////
int gs_save_cali(int x, int y, int z)
{
	 //LOGI("gs_save_calix=%d,y=%d,z=%d",x,y,z);

	int file_lid = AP_CFG_RDCL_HWMON_ACC_LID;//iFileACCCaliID;
	F_ID nvram_fid;
	NVRAM_HWMON_ACC_STRUCT cali;
	int rec_size;
	int rec_num;
	int Ret = 0;
	/*
	cali.offset[0] = x;
	cali.offset[1] = y;
	cali.offset[2] = z;
*/
   cali.offset[0] = x*65536/9807;
    cali.offset[1] = y*65536/9807 ;
    cali.offset[2] = z*65536/9807; 
	 //LOGI("gcali.offset[0] =%d,  cali.offset[1] =%d,   cali.offset[2]=%d",cali.offset[0] , cali.offset[1] ,cali.offset[2]);
	nvram_fid = NVM_GetFileDesc(file_lid, &rec_size, &rec_num,ISWRITE);

	if (nvram_fid.iFileDesc < 0) {
		 //LOGE("Unable to open NVRAM file!");
		return -1;
	}
	
	 //LOGI("open NVRAM success!, rec_num=%d, rec_size=%d",rec_size,rec_num);

	Ret = write(nvram_fid.iFileDesc, &cali, rec_size*rec_num);//write NVRAM
	
	if (Ret < 0) {
		 //LOGE("write NVRAM error!");
		return -1;
	}

      //LOGI("write NVRAM success! Ret=%d",Ret);
	NVM_CloseFileDesc(nvram_fid);


#if 0
	nvram_fid = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, 1);
	if (nvram_fid.iFileDesc < 0) {
		//LOGE("Unable to open NVRAM file!");
		return -1;
	}
	
	Ret = read(nvram_fid.iFileDesc, &cali, rec_size*rec_num);//read NVRAM
	if (Ret < 0) {
		//LOGE("write NVRAM error!");
		return -1;
	}
	//LOGI("read NVRAM success!, cali x/y/z: %d %d %d",cali.offset[0],cali.offset[1],cali.offset[2]);
	NVM_CloseFileDesc(nvram_fid);
#endif 
//        FileOp_BackupToBinRegion_All();	
	//LOGI("FileOp_BackupToBinRegion_All()");
	return 0;
}

int gs_set_cali(int x, int y, int z)
{	//LOGI("gs_set_cali x=%d,y=%d,z=%d",x,y,z);
	int fd;
	int ret;
	SENSOR_DATA gs_data;
	gs_data.x = x;
	gs_data.y = y;
	gs_data.z = z;
	
	fd = open(GSENSOR_NAME, O_RDONLY);
	if(fd<0)
	{
         //LOGE("open gsensor error, fd=%d",fd);
		close(fd);
		 return -1;
	}

    if ((ret = ioctl(fd, GSENSOR_IOCTL_SET_CALI, &gs_data))) {
         //LOGE("gs_set_cali error");
		close(fd);
        return ret;
    }

	 //LOGI("gs_set_cali success Ret=%d",ret);
	close(fd);
	return 0;
}

int gs_get_cali(void)
{	
	int fd;
	int ret;
	SENSOR_DATA gs_data;	
	
	fd = open(GSENSOR_NAME, O_RDONLY);
	if(fd<0)
	{
         //LOGE("open gsensor error, fd=%d",fd);
		close(fd);
		 return -1;
	}

    if ((ret = ioctl(fd, GSENSOR_IOCTL_GET_CALI, &gs_data))) {
         //LOGE("gs_set_cali error");
		close(fd);
        return ret;
    }

	x=gs_data.x ;
	y=gs_data.y ;
	z=gs_data.z ;
//LOGI("gs_get_cali x=%d,y=%d,z=%d",x,y,z);

	 //LOGI("gs_get_cali success Ret=%d",ret);
	close(fd);
	return 0;
}
int gs_clear_cali(void)
{	
	int fd;
	int ret;
	
	fd = open(GSENSOR_NAME, O_RDONLY);
	if(fd<0)
	{
         //LOGE("open gsensor error, fd=%d",fd);
		close(fd);
		 return -1;
	}

    if ((ret = ioctl(fd, GSENSOR_IOCTL_CLR_CALI, NULL))) {
         //LOGE("gs_set_cali error");
		close(fd);
        return ret;
    }
	close(fd);
	return 0;
}
/*  virtual int  calisetGsCali_x(int data)=0;
    virtual int  calisetGsCali_y(int data)=0;
    virtual int calisetGsCali_z(int data)=0;
    virtual int caliGsCali()=0;*/
int CitBinder::caliGsCali()
{
   	//LOGI("gs_cali x=%d,y=%d,z=%d",x,y,z);
	int ret;
	ret = gs_set_cali(x,y,z);
	if(ret<0)
	{	//LOGE("gs_set_cali fail");
		return -3;
	}

	ret = gs_get_cali();
	if(ret<0)
	{	//LOGE("gs_set_cali fail");
		return -4;
	}
	ret = gs_save_cali(x,y,z);
	if(ret<0)
	{	//LOGE("gs_save_cali fail");
		return -2;

	}

	//LOGI("gs_cali success");
	return 1; 
}

int CitBinder::ClrGsCali()
{
	int ret;
	
	ret = gs_save_cali(0,0,0);  //clear nvram
	if(ret<0)
	{	//LOGE("clear_cali fail");
		return -3;
	}

	ret = gs_clear_cali();
	if(ret<0)
	{	//LOGE("clear_cali fail");
		return -4;
	}

	
	//LOGI("clear_cali success");
	return 1; 
}
int CitBinder::calisetGsCali_x(int data)
{       

        x=data;
	   	//LOGI("gs_cali x=",x);
	return x; 
}
int CitBinder::calisetGsCali_y(int data)
{       

        y=data;
	   	//LOGI("gs_cali y=",y);
	return y; 
}
int CitBinder::calisetGsCali_z(int data)
{       

        z=data;
	//LOGI("gs_cali z=",z);
	return z; 
}

unsigned int CitBinder::getFirstPwrOn_Flag()
{
   int result = 0;

  // int sncit_nvram_fd = 0;
   F_ID sncit_nvram_fd;
//   int file_lid = AP_CFG_CUSTOM_FILE_CUSTOM2_LID;
   int file_lid = 0;
   int i = 0,rec_sizem,rec_size,rec_num;
   unsigned int ret=0;
   //unsigned char barcode[64];
   int snCitParam;
   memset(&snCitParam,0,sizeof(snCitParam));

   sncit_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISREAD);

   result = read(sncit_nvram_fd.iFileDesc, &snCitParam , rec_size*rec_num);
   if(result)
{
	int temp = 0;
   //LOGI("getFirstPwrOn_Flag SNcit.flag = %d",snCitParam);
   //LOGI("getFirstPwrOn_Flag SNcit.flag result = %d",result);
   
}else
{ 
	int temp = 0;
  	//LOGE("sn cit setflag fail");
}
   NVM_CloseFileDesc(sncit_nvram_fd);

   if (snCitParam != 0) 
{
//LOGI("getFirstPwrOn_Flag 11111");
   ret =1;
}
else 
{
//LOGI("getFirstPwrOn_Flag 222222");
   ret = 0;
}
   return ret;
}

int CitBinder::setFirstPwrOn_Flag()
{
   int result = 0;

   //int sncit_nvram_fd = 0;
   F_ID sncit_nvram_fd;
//   int file_lid = AP_CFG_CUSTOM_FILE_CUSTOM2_LID;
   int file_lid = 0;
   int i = 0,rec_sizem,rec_size,rec_num;
   int snCitParam;
   memset(&snCitParam,0,sizeof(snCitParam));
   
   sncit_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISREAD);
   result = read(sncit_nvram_fd.iFileDesc, &snCitParam , rec_size*rec_num);
 //LOGI("setFirstPwrOn_Flag snCitParam = %d",snCitParam);
//LOGI("setFirstPwrOn_Flag result = %d",result);
   if(result)
    {
//LOGI("setFirstPwrOn_Flag 11111");
   		snCitParam=1;
  	}
  	else
  	{
//LOGI("setFirstPwrOn_Flag 222222");
  		//LOGE("sn cit setflag fail");
    }

 //LOGI("setFirstPwrOn_Flag snCitParam 2 = %d",snCitParam);
//LOGI("setFirstPwrOn_Flag result 2 = %d",result);
       NVM_CloseFileDesc(sncit_nvram_fd);

   sncit_nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, ISWRITE);
   result = write(sncit_nvram_fd.iFileDesc, &snCitParam , rec_size*rec_num);
       NVM_CloseFileDesc(sncit_nvram_fd);

//      setresult=FileOp_BackupToBinRegion_All();
   //LOGI("setFirstPwrOn_Flag--write resul= %d----FileOp_BackupToBinRegion_All %d",result,setresult);
   return result;
}


int
main(int argc, char *argv[])
{
    CitBinder::instantiate();
    ProcessState::self()->startThreadPool();
    //LOGI("CitBinder Service is now ready");
    IPCThreadState::self()->joinThreadPool();
    return(0);
}

