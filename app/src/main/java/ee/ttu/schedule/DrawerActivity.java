package ee.ttu.schedule;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import ee.ttu.schedule.fragment.ScheduleFragment;
import ee.ttu.schedule.fragment.UpdateScheduleFragment;

/**
 * Created by vadimstrukov on 12/1/15.
 */
public class DrawerActivity extends AppCompatActivity {

    private Fragment tempFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ttu_schedule_ic);
        setSupportActionBar(toolbar);
        tempFragment = new ScheduleFragment();
        updateFragment();

        new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withHeader(R.layout.drawer_header)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.app_name).withIcon(GoogleMaterial.Icon.gmd_home).withIdentifier(0),
                        new SectionDrawerItem().withName(R.string.drawer_item_settings),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_update).withIcon(GoogleMaterial.Icon.gmd_refresh).withIdentifier(1),
                        new SectionDrawerItem().withName(R.string.drawer_item_about),
                        new SecondaryDrawerItem().withName(R.string.drawer_item_contact).withIcon(GoogleMaterial.Icon.gmd_mail_send).withIdentifier(2)
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null) {
                            switch (drawerItem.getIdentifier()) {
                                case 0:
                                    tempFragment = new ScheduleFragment();
                                    break;
                                case 1:
                                    tempFragment = new UpdateScheduleFragment();
                                    break;
                                case 2:
                                    tempFragment = new AboutFragment();
                                    break;
                            }
                        }
                        return false;
                    }
        }).withOnDrawerListener(new Drawer.OnDrawerListener() {

                    @Override
                    public void onDrawerOpened(View drawerView) {
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        updateFragment();
                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                    }
                }).build();
    }
    private void updateFragment(){
        if (tempFragment != null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.flFragments, tempFragment);
            transaction.commit();
        }
        tempFragment = null;
    }
}
