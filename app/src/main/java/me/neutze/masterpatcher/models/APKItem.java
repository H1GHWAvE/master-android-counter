package me.neutze.masterpatcher.models;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

import net.erdfelt.android.apk.AndroidApk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;
import me.neutze.masterpatcher.R;
import me.neutze.masterpatcher.utils.AdUtils;
import me.neutze.masterpatcher.utils.ModifiedUtils;
import me.neutze.masterpatcher.utils.OdexUtils;
import me.neutze.masterpatcher.utils.SDCardUtils;
import me.neutze.masterpatcher.utils.SharedPrefUtils;
import me.neutze.masterpatcher.utils.TimeUtils;

/**
 * Created by H1GHWAvE on 24/09/15.
 */
public class APKItem {

    private boolean ads;
    private boolean billing;
    private boolean custom;
    private boolean enable;
    private transient Drawable icon;
    private boolean lvl;
    private boolean modified;
    private String name;
    private boolean odex;
    private boolean on_sd;
    private String pkgName;
    private int stored;
    private boolean system;
    private int updatetime;
    private String applicationPath;
    private List<String> permissions;
    private List<String> advertisments;
    private boolean edited;

    private APKItem(Context context, String pkgName, PackageManager packageManager) {
        this.pkgName = pkgName;

        this.applicationPath = context.getResources().getString(R.string.app_folder) + pkgName + "/" + context.getResources().getString(R.string.base_apk);
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(applicationPath, 1);

        packageInfo.applicationInfo.sourceDir = applicationPath;
        packageInfo.applicationInfo.publicSourceDir = applicationPath;

        this.name = packageInfo.applicationInfo.loadLabel(packageManager).toString();
        this.icon = packageInfo.applicationInfo.loadIcon(packageManager);
        this.on_sd = SDCardUtils.isInstalledOnSdCard(context, applicationPath);
        this.enable = packageInfo.applicationInfo.enabled;

        this.name = packageInfo.applicationInfo.loadLabel(packageManager).toString();

        this.odex = OdexUtils.isOdex(applicationPath);

        this.updatetime = (int) (TimeUtils.getfirstInstallTime(packageInfo, this.pkgName) / 1000);

        this.modified = ModifiedUtils.isModified(this.pkgName);

        if (packageInfo.applicationInfo.flags == 1) {
            this.system = true;
        }

        if (packageInfo.activities != null) {
            advertisments = new ArrayList<>();

            for (int i = 0; i < packageInfo.activities.length; i++) {
                if (AdUtils.isAds(packageInfo.activities[i].name)) {
                    advertisments.add(packageInfo.activities[i].name);
                    this.ads = true;
                }
            }
        }

        try {
            AndroidApk apk = new AndroidApk(new File(applicationPath));
            permissions = apk.getPermissions();
            if (permissions != null) {
                for (String permission : apk.getPermissions()) {
                    if (permission.equals("com.android.vending.CHECK_LICENSE")) {
                        lvl = true;
                    }
                    if (permission.equals("com.android.vending.BILLING")) {
                        billing = true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        stored = 0;
        if (ads) {
            stored += 100;
        }
        if (billing) {
            stored += 10;
        }
        if (lvl) {
            stored += 1;
        }

        this.edited = false;
    }


    public static List<APKItem> getApplications(Context context) {
        List<APKItem> applicationsList = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();

        List<String> installedApplications = Shell.SU.run("ls " + context.getResources().getString(R.string.app_folder));
        for (String application : installedApplications) {
            APKItem apkItem = SharedPrefUtils.getAPKItem(context, application);

            if (apkItem == null) {
                Log.e(application, "new APKItem");
                apkItem = getApkItem(context, application, packageManager);
            } else {
                if (apkItem.isEdited()) {
                    Log.e(application, "update APKItem");
                    apkItem = getApkItem(context, application, packageManager);
                } else {
                    Log.e(application, "get Icon");
                    PackageInfo packageInfo = packageManager.getPackageArchiveInfo(apkItem.getApplicationPath(), 1);
                    apkItem.setIcon(packageInfo.applicationInfo.loadIcon(packageManager));
                }
            }
            applicationsList.add(apkItem);
        }

        return applicationsList;
    }

    private static APKItem getApkItem(Context context, String application, PackageManager packageManager) {
        APKItem apkItem = new APKItem(context, application, packageManager);
        SharedPrefUtils.saveAPKItem(context, apkItem);

        return apkItem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public boolean getLvl() {
        return lvl;
    }

    public boolean getBilling() {
        return billing;
    }

    public boolean getAds() {
        return ads;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public boolean isAds() {
        return ads;
    }

    public void setAds(boolean ads) {
        this.ads = ads;
    }

    public boolean isBilling() {
        return billing;
    }

    public void setBilling(boolean billing) {
        this.billing = billing;
    }

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isLvl() {
        return lvl;
    }

    public void setLvl(boolean lvl) {
        this.lvl = lvl;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public boolean isOdex() {
        return odex;
    }

    public void setOdex(boolean odex) {
        this.odex = odex;
    }

    public boolean isOn_sd() {
        return on_sd;
    }

    public void setOn_sd(boolean on_sd) {
        this.on_sd = on_sd;
    }

    public int getStored() {
        return stored;
    }

    public void setStored(int stored) {
        this.stored = stored;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public int getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(int updatetime) {
        this.updatetime = updatetime;
    }

    public String getApplicationPath() {
        return applicationPath;
    }

    public void setApplicationPath(String applicationPath) {
        this.applicationPath = applicationPath;
    }

    public List<String> getAdvertisments() {
        return advertisments;
    }

    public void setAdvertisments(List<String> advertisments) {
        this.advertisments = advertisments;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }
}