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

package com.mediatek.FMRadio;

interface IFMRadioService {
    boolean openDevice();
    boolean closeDevice();
    boolean isDeviceOpen();
    boolean powerUp(float frequency);
    boolean powerDown();
    boolean isPowerUp();
    boolean tune(float frequency);
    float seek(float frequency, boolean isUp);
    int[] startScan();
    boolean stopScan();
    int setRDS(boolean on);
    int readRDS();
    String getPS();
    String getLRText();
    int activeAF();
    int activeTA();
    int deactiveTA();
    int setMute(boolean mute);
    int isRDSSupported();
    void useEarphone(boolean use);
    boolean isEarphoneUsed();
    void initService(int iCurrentStation);
    boolean isServiceInit();
    void enablePSRT(boolean enable);
    void enableAF(boolean enable);
    void enableTA(boolean enable);
    boolean isPSRTEnabled();
    boolean isAFEnabled();
    boolean isTAEnabled();
    int getFrequency();
    void resumeFMAudio();
    int readRssi();
    int readCapArray();

    boolean getStereoMono();
    boolean setStereoMono(boolean isMono);
    int switchAntenna(int antenna);
    int readRdsBler();
    void startRecording();
    void stopRecording();
    void saveRecording(String newName);
    void startPlayback();
    void stopPlayback();
    int getRecorderState();
    long getRecordTime();
    void setRecordingMode(boolean isRecording);
    boolean getRecordingMode();
    int getPlaybackPosition();
    String getRecordingName();
    boolean isFMOverBTActive();
    boolean getResumeAfterCall();
    boolean isSIMCardIdle();
    int[] getHardwareVersion();
    void setStopPressed(boolean isStopPressed);
}
