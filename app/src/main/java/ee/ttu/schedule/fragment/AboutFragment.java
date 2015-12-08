package ee.ttu.schedule.fragment;


import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.vadimstrukov.ttuschedule.R;

public class AboutFragment extends PreferenceFragment {
    private static final String BUILD_VERSION = "build_version";
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.preferences);
        final Activity activity = getActivity();
        try {
            final PackageInfo packageInfo =
                    activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            findPreference(BUILD_VERSION).setSummary(packageInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            findPreference(BUILD_VERSION).setSummary("?");
        }
    }
}
