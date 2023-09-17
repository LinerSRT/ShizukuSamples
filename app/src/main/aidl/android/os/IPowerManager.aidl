/* //device/java/android/android/os/IPowerManager.aidl
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package android.os;

import android.os.BatterySaverPolicyConfig;
import android.os.PowerSaveState;
import android.os.WorkSource;


interface IPowerManager{
       void acquireWakeLock(IBinder lock, int flags, String tag, String packageName, in WorkSource ws);
       void acquireWakeLockWithUid(IBinder lock, int flags, String tag, String packageName, int uidtoblame);
       void releaseWakeLock(IBinder lock, int flags);
       void updateWakeLockUids(IBinder lock, in int[] uids);
       oneway void powerHint(int hintId, int data);
       oneway void setPowerBoost(int boost, int durationMs);
       oneway void setPowerMode(int mode, boolean enabled);
       boolean setPowerModeChecked(int mode, boolean enabled);
       void updateWakeLockWorkSource(IBinder lock, in WorkSource ws, String historyTag);
       boolean isWakeLockLevelSupported(int level);
       void userActivity(long time, int event, int flags);
       void wakeUp(long time, int reason, String details, String opPackageName);
       void goToSleep(long time, int reason, int flags);
       void nap(long time);
       float getBrightnessConstraint(int constraint);
       boolean isInteractive();
       boolean isPowerSaveMode();
       PowerSaveState getPowerSaveState(int serviceType);
       boolean setPowerSaveModeEnabled(boolean mode);
       boolean setDynamicPowerSaveHint(boolean powerSaveHint, int disableThreshold);
       boolean setAdaptivePowerSavePolicy(in BatterySaverPolicyConfig config);
       boolean setAdaptivePowerSaveEnabled(boolean enabled);
       int getPowerSaveModeTrigger();
       boolean isDeviceIdleMode();
       boolean isLightDeviceIdleMode();
       void reboot(boolean confirm, String reason, boolean wait);
       void rebootSafeMode(boolean confirm, boolean wait);
       void shutdown(boolean confirm, String reason, boolean wait);
       void crash(String message);
       int getLastShutdownReason();
       int getLastSleepReason();
       void setStayOnSetting(int val);
       void boostScreenBrightness(long time);
       boolean isScreenBrightnessBoosted();
       void setAttentionLight(boolean on, int color);
       void setDozeAfterScreenOff(boolean on);
       boolean isAmbientDisplayAvailable();
       void suppressAmbientDisplay(String token, boolean suppress);
       boolean isAmbientDisplaySuppressedForToken(String token);
       boolean isAmbientDisplaySuppressed();
       boolean isAmbientDisplaySuppressedForTokenByApp(String token, int appUid);
       boolean forceSuspend();
}