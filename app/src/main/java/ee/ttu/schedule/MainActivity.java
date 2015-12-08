package ee.ttu.schedule;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.vadimstrukov.ttuschedule.R;

import ee.ttu.schedule.fragment.AboutFragment;
import ee.ttu.schedule.fragment.ChangeScheduleFragment;
import ee.ttu.schedule.fragment.ScheduleFragment;

public class MainActivity extends AppCompatActivity implements Drawer.OnDrawerItemClickListener {

    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ttu_schedule_ic);
        setSupportActionBar(toolbar);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(R.id.flFragments, new ScheduleFragment()).commit();
        }
        new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withHeader(R.layout.drawer_header)
                .withSelectedItemByPosition(1)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.app_name).withIcon(GoogleMaterial.Icon.gmd_home).withIdentifier(0),
                        new SectionDrawerItem().withName(R.string.drawer_item_settings),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_update).withIcon(GoogleMaterial.Icon.gmd_refresh).withIdentifier(1),
                        new SectionDrawerItem().withName(R.string.drawer_item_about),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_additional).withIcon(GoogleMaterial.Icon.gmd_mail_send).withIdentifier(2)
                ).withOnDrawerItemClickListener(this).build();
    }

    @Override
    public boolean onItemClick(View view, int position, final IDrawerItem drawerItem) {
        if (drawerItem != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    switch (drawerItem.getIdentifier()) {
                        case 0:
                            getFragmentManager().beginTransaction().replace(R.id.flFragments, new ScheduleFragment()).commit();
                            break;
                        case 1:
                            getFragmentManager().beginTransaction().replace(R.id.flFragments, new ChangeScheduleFragment()).commit();
                            break;
                        case 2:
                            getFragmentManager().beginTransaction().replace(R.id.flFragments, new AboutFragment()).commit();
                            break;
                    }
                }
            });
        }
        return false;
    }
}
