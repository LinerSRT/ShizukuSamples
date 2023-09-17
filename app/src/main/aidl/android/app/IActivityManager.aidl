package android.app;
import android.app.ActivityManager;
import android.content.pm.UserInfo;

interface IActivityManager {
    UserInfo getCurrentUser();
    int getCurrentUserId();
    List<ActivityManager.RunningTaskInfo> getTasks(int maxNum);
    boolean removeTask(int taskId);
    void killUid(int appId, int userId, in String reason);
    void killApplication(in String pkg, int appId, int userId, in String reason);
    void killApplicationProcess(in String processName, int uid);
    void killBackgroundProcesses(in String packageName, int userId);
    void killAllBackgroundProcesses();
    void getMemoryInfo(out ActivityManager.MemoryInfo outInfo);
}