/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.content.pm;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.DebugUtils;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class UserInfo implements Parcelable {
    public static final String USER_TYPE_FULL_SECONDARY = "android.os.usertype.full.SECONDARY";
    public static final String USER_TYPE_FULL_GUEST = "android.os.usertype.full.GUEST";
    public static final String USER_TYPE_FULL_DEMO = "android.os.usertype.full.DEMO";
    public static final String USER_TYPE_FULL_RESTRICTED = "android.os.usertype.full.RESTRICTED";
    public static final String USER_TYPE_PROFILE_MANAGED = "android.os.usertype.profile.MANAGED";
    public static final String USER_TYPE_PROFILE_CLONE = "android.os.usertype.profile.CLONE";
    public static final int FLAG_PRIMARY = 0x00000001;
    public static final int FLAG_ADMIN = 0x00000002;
    @Deprecated
    public static final int FLAG_GUEST = 0x00000004;
    @Deprecated
    public static final int FLAG_RESTRICTED = 0x00000008;
    public static final int FLAG_INITIALIZED = 0x00000010;
    @Deprecated
    public static final int FLAG_MANAGED_PROFILE = 0x00000020;
    public static final int FLAG_DISABLED = 0x00000040;
    public static final int FLAG_QUIET_MODE = 0x00000080;
    public static final int FLAG_EPHEMERAL = 0x00000100;
    @Deprecated
    public static final int FLAG_DEMO = 0x00000200;
    public static final int FLAG_FULL = 0x00000400;
    public static final int FLAG_SYSTEM = 0x00000800;
    public static final int FLAG_PROFILE = 0x00001000;
    public static final int FLAG_EPHEMERAL_ON_CREATE = 0x00002000;

    /**
     * @hide
     */
    @IntDef(flag = true, value = {
            FLAG_PRIMARY,
            FLAG_ADMIN,
            FLAG_GUEST,
            FLAG_RESTRICTED,
            FLAG_INITIALIZED,
            FLAG_MANAGED_PROFILE,
            FLAG_DISABLED,
            FLAG_QUIET_MODE,
            FLAG_EPHEMERAL,
            FLAG_DEMO,
            FLAG_FULL,
            FLAG_SYSTEM,
            FLAG_PROFILE,
            FLAG_EPHEMERAL_ON_CREATE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface UserInfoFlag {
    }

    public static final int NO_PROFILE_GROUP_ID = -10000;

    public int id;

    public int serialNumber;

    public @Nullable String name;

    public String iconPath;

    public @UserInfoFlag int flags;

    public long creationTime;

    public long lastLoggedInTime;
    public String lastLoggedInFingerprint;
    public String userType;
    public int profileGroupId;
    public int restrictedProfileParentId;
    public int profileBadge;
    public boolean partial;
    public boolean guestToRemove;
    public boolean preCreated;
    public boolean convertedFromPreCreated;

    public UserInfo(int id, String name, int flags) {
        this(id, name, null, flags);
    }

    public UserInfo(int id, String name, String iconPath, int flags) {
        this(id, name, iconPath, flags, getDefaultUserType(flags));
    }

    public UserInfo(int id, String name, String iconPath, int flags, String userType) {
        this.id = id;
        this.name = name;
        this.flags = flags;
        this.userType = userType;
        this.iconPath = iconPath;
        this.profileGroupId = NO_PROFILE_GROUP_ID;
        this.restrictedProfileParentId = NO_PROFILE_GROUP_ID;
    }

    public static @NonNull String getDefaultUserType(@UserInfoFlag int userInfoFlag) {
        if ((userInfoFlag & FLAG_SYSTEM) != 0) {
            throw new IllegalArgumentException("Cannot getDefaultUserType for flags "
                    + Integer.toHexString(userInfoFlag) + " because it corresponds to a "
                    + "SYSTEM user type.");
        }
        final int supportedFlagTypes = FLAG_GUEST | FLAG_RESTRICTED | FLAG_MANAGED_PROFILE | FLAG_DEMO;
        switch (userInfoFlag & supportedFlagTypes) {
            case 0:
                return USER_TYPE_FULL_SECONDARY;
            case FLAG_GUEST:
                return USER_TYPE_FULL_GUEST;
            case FLAG_RESTRICTED:
                return USER_TYPE_FULL_RESTRICTED;
            case FLAG_MANAGED_PROFILE:
                return USER_TYPE_PROFILE_MANAGED;
            case FLAG_DEMO:
                return USER_TYPE_FULL_DEMO;
            default:
                throw new IllegalArgumentException("Cannot getDefaultUserType for flags "
                        + Integer.toHexString(userInfoFlag) + " because it doesn't correspond to a "
                        + "valid user type.");
        }
    }

    public boolean isPrimary() {
        return (flags & FLAG_PRIMARY) == FLAG_PRIMARY;
    }

    public boolean isAdmin() {
        return (flags & FLAG_ADMIN) == FLAG_ADMIN;
    }

    public boolean isGuest() {
        return USER_TYPE_FULL_GUEST.equals(userType);
    }

    public boolean isRestricted() {
        return USER_TYPE_FULL_RESTRICTED.equals(userType);
    }

    public boolean isProfile() {
        return (flags & FLAG_PROFILE) != 0;
    }

    public boolean isManagedProfile() {
        return USER_TYPE_PROFILE_MANAGED.equals(userType);
    }

    public boolean isCloneProfile() {
        return USER_TYPE_PROFILE_CLONE.equals(userType);
    }

    public boolean isEnabled() {
        return (flags & FLAG_DISABLED) != FLAG_DISABLED;
    }

    public boolean isQuietModeEnabled() {
        return (flags & FLAG_QUIET_MODE) == FLAG_QUIET_MODE;
    }

    public boolean isEphemeral() {
        return (flags & FLAG_EPHEMERAL) == FLAG_EPHEMERAL;
    }

    public boolean isInitialized() {
        return (flags & FLAG_INITIALIZED) == FLAG_INITIALIZED;
    }

    public boolean isDemo() {
        return  USER_TYPE_FULL_RESTRICTED.equals(userType) || (flags & FLAG_DEMO) != 0;
    }

    public boolean isFull() {
        return (flags & FLAG_FULL) == FLAG_FULL;
    }


    public boolean isSystemOnly() {
        return isSystemOnly(id);
    }


    public static boolean isSystemOnly(int userId) {
        return userId == 0;
    }

    /**
     * @return true if this user can be switched to.
     **/
    public boolean supportsSwitchTo() {
        if (partial || !isEnabled()) {
            // Don't support switching to disabled or partial users, which includes users with
            // removal in progress.
            return false;
        }
        if (preCreated) {
            // Don't support switching to pre-created users until they become "real" users.
            return false;
        }
        return !isProfile();
    }

    /**
     * @return true if this user can be switched to by end user through UI.
     */
    public boolean supportsSwitchToByUser() {
        // Hide the system user when it does not represent a human user.
        boolean hideSystemUser = UserManager.isHeadlessSystemUserMode();
        return (!hideSystemUser || id != 0) && supportsSwitchTo();
    }

    // TODO(b/142482943): Make this logic more specific and customizable. (canHaveProfile(userType))
    /* @hide */
    public boolean canHaveProfile() {
        if (isProfile() || isGuest() || isRestricted()) {
            return false;
        }
        if ( UserManager.isHeadlessSystemUserMode()) {
            return id != 0;
        } else {
            return id == 0;
        }
    }

    @Deprecated
    public UserInfo() {
    }

    public UserInfo(UserInfo orig) {
        name = orig.name;
        iconPath = orig.iconPath;
        id = orig.id;
        flags = orig.flags;
        userType = orig.userType;
        serialNumber = orig.serialNumber;
        creationTime = orig.creationTime;
        lastLoggedInTime = orig.lastLoggedInTime;
        lastLoggedInFingerprint = orig.lastLoggedInFingerprint;
        partial = orig.partial;
        preCreated = orig.preCreated;
        convertedFromPreCreated = orig.convertedFromPreCreated;
        profileGroupId = orig.profileGroupId;
        restrictedProfileParentId = orig.restrictedProfileParentId;
        guestToRemove = orig.guestToRemove;
        profileBadge = orig.profileBadge;
    }


    @Override
    public String toString() {
        // NOTE:  do not change this string, it's used by 'pm list users', which in turn is
        // used and parsed by TestDevice. In other words, if you change it, you'd have to change
        // TestDevice, TestDeviceTest, and possibly others....
        return "UserInfo{" + id + ":" + name + ":" + Integer.toHexString(flags) + "}";
    }

    public String toFullString() {
        return "UserInfo[id=" + id
                + ", name=" + name
                + ", type=" + userType
                + (preCreated ? " (pre-created)" : "")
                + (convertedFromPreCreated ? " (converted)" : "")
                + (partial ? " (partial)" : "")
                + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(iconPath);
        dest.writeInt(flags);
        dest.writeString(userType);
        dest.writeInt(serialNumber);
        dest.writeLong(creationTime);
        dest.writeLong(lastLoggedInTime);
        dest.writeString(lastLoggedInFingerprint);
        dest.writeBoolean(partial);
        dest.writeBoolean(preCreated);
        dest.writeInt(profileGroupId);
        dest.writeBoolean(guestToRemove);
        dest.writeInt(restrictedProfileParentId);
        dest.writeInt(profileBadge);
    }

    public static final @NonNull Parcelable.Creator<UserInfo> CREATOR
            = new Parcelable.Creator<UserInfo>() {
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }

        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }
    };

    private UserInfo(Parcel source) {
        id = source.readInt();
        name = source.readString();
        iconPath = source.readString();
        flags = source.readInt();
        userType = source.readString();
        serialNumber = source.readInt();
        creationTime = source.readLong();
        lastLoggedInTime = source.readLong();
        lastLoggedInFingerprint = source.readString();
        partial = source.readBoolean();
        preCreated = source.readBoolean();
        profileGroupId = source.readInt();
        guestToRemove = source.readBoolean();
        restrictedProfileParentId = source.readInt();
        profileBadge = source.readInt();
    }
}