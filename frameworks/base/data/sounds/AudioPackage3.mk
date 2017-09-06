#
# Audio Package 3
# 
# Include this file in a product makefile to include these audio files
#
# This is a larger package of sounds than the 1.0 release for devices
# that have larger internal flash.
# 

LOCAL_PATH:= frameworks/base/data/sounds

PRODUCT_COPY_FILES += \
	$(LOCAL_PATH)/F1_MissedCall.ogg:system/media/audio/notifications/F1_MissedCall.ogg \
	$(LOCAL_PATH)/F1_New_MMS.ogg:system/media/audio/notifications/F1_New_MMS.ogg \
	$(LOCAL_PATH)/F1_New_SMS.ogg:system/media/audio/notifications/F1_New_SMS.ogg \
	$(LOCAL_PATH)/Alarm_Buzzer.ogg:system/media/audio/alarms/Alarm_Buzzer.ogg \
	$(LOCAL_PATH)/Alarm_Beep_01.ogg:system/media/audio/alarms/Alarm_Beep_01.ogg \
	$(LOCAL_PATH)/Alarm_Beep_02.ogg:system/media/audio/alarms/Alarm_Beep_02.ogg \
	$(LOCAL_PATH)/Alarm_Classic.ogg:system/media/audio/alarms/Alarm_Classic.ogg \
	$(LOCAL_PATH)/Alarm_Beep_03.ogg:system/media/audio/alarms/Alarm_Beep_03.ogg \
	$(LOCAL_PATH)/Alarm_Rooster_02.ogg:system/media/audio/alarms/Alarm_Rooster_02.ogg \
	$(LOCAL_PATH)/notifications/Beat_Box_Android.ogg:system/media/audio/notifications/Beat_Box_Android.ogg \
	$(LOCAL_PATH)/notifications/Heaven.ogg:system/media/audio/notifications/Heaven.ogg \
	$(LOCAL_PATH)/notifications/TaDa.ogg:system/media/audio/notifications/TaDa.ogg \
	$(LOCAL_PATH)/notifications/Tinkerbell.ogg:system/media/audio/notifications/Tinkerbell.ogg \
	$(LOCAL_PATH)/effects/Effect_Tick.ogg:system/media/audio/ui/Effect_Tick.ogg \
	$(LOCAL_PATH)/effects/KeypressStandard.ogg:system/media/audio/ui/KeypressStandard.ogg \
	$(LOCAL_PATH)/effects/KeypressSpacebar.ogg:system/media/audio/ui/KeypressSpacebar.ogg \
	$(LOCAL_PATH)/effects/KeypressDelete.ogg:system/media/audio/ui/KeypressDelete.ogg \
	$(LOCAL_PATH)/effects/KeypressInvalid.ogg:system/media/audio/ui/KeypressInvalid.ogg \
	$(LOCAL_PATH)/effects/KeypressReturn.ogg:system/media/audio/ui/KeypressReturn.ogg \
	$(LOCAL_PATH)/effects/VideoRecord.ogg:system/media/audio/ui/VideoRecord.ogg \
	$(LOCAL_PATH)/effects/camera_click.ogg:system/media/audio/ui/camera_click.ogg \
	$(LOCAL_PATH)/effects/LowBattery.ogg:system/media/audio/ui/LowBattery.ogg \
	$(LOCAL_PATH)/effects/Dock.ogg:system/media/audio/ui/Dock.ogg \
	$(LOCAL_PATH)/effects/Undock.ogg:system/media/audio/ui/Undock.ogg \
	$(LOCAL_PATH)/effects/Lock.ogg:system/media/audio/ui/Lock.ogg \
	$(LOCAL_PATH)/effects/Unlock.ogg:system/media/audio/ui/Unlock.ogg \
	$(LOCAL_PATH)/effects/ogg/Trusted.ogg:system/media/audio/ui/Trusted.ogg \
	$(LOCAL_PATH)/notifications/moonbeam.ogg:system/media/audio/notifications/moonbeam.ogg \
	$(LOCAL_PATH)/notifications/pixiedust.ogg:system/media/audio/notifications/pixiedust.ogg \
	$(LOCAL_PATH)/notifications/pizzicato.ogg:system/media/audio/notifications/pizzicato.ogg \
	$(LOCAL_PATH)/notifications/tweeters.ogg:system/media/audio/notifications/tweeters.ogg \
	$(LOCAL_PATH)/newwavelabs/CaffeineSnake.ogg:system/media/audio/notifications/CaffeineSnake.ogg

ifneq ($(MINIMAL_NEWWAVELABS),true)
PRODUCT_COPY_FILES += \
	$(LOCAL_PATH)/newwavelabs/CurveBall.ogg:system/media/audio/ringtones/CurveBall.ogg \
	$(LOCAL_PATH)/newwavelabs/DearDeer.ogg:system/media/audio/notifications/DearDeer.ogg \
	$(LOCAL_PATH)/newwavelabs/DontPanic.ogg:system/media/audio/notifications/DontPanic.ogg \
	$(LOCAL_PATH)/newwavelabs/Highwire.ogg:system/media/audio/notifications/Highwire.ogg \
	$(LOCAL_PATH)/newwavelabs/KzurbSonar.ogg:system/media/audio/notifications/KzurbSonar.ogg \
	$(LOCAL_PATH)/newwavelabs/OnTheHunt.ogg:system/media/audio/notifications/OnTheHunt.ogg \
	$(LOCAL_PATH)/newwavelabs/Voila.ogg:system/media/audio/notifications/Voila.ogg \
	
endif
