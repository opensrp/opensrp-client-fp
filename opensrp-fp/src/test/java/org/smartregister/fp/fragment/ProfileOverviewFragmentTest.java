package org.smartregister.fp.fragment;

import android.app.Activity;
import android.content.Intent;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.smartregister.fp.activity.BaseActivityUnitTest;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.features.profile.presenter.ProfilePresenter;
import org.smartregister.fp.features.profile.view.ProfileActivity;
import org.smartregister.fp.features.profile.view.ProfileOverviewFragment;

import java.util.HashMap;

import static org.robolectric.shadows.ShadowInstrumentation.getInstrumentation;

public class ProfileOverviewFragmentTest extends BaseActivityUnitTest {
    private ProfileActivity profileActivity;
    private ActivityController<ProfileActivity> controller;
    private ProfileOverviewFragment profileOverviewFragment;
    private ProfileActivity spyActivity;


    @Mock
    private ProfilePresenter presenter;

    @Before
    public void setUp() {
        super.setUp();
    }

    @Override
    protected Activity getActivity() {
        return profileActivity;
    }

    @Override
    protected ActivityController getActivityController() {
        return controller;
    }

    @Test
    public void testFragmentInstance() {
        Intent testIntent = new Intent();
        testIntent.putExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID, DUMMY_BASE_ENTITY_ID);
        HashMap<String, String> map = new HashMap<>();
        map.put(DBConstantsUtils.KeyUtils.NEXT_CONTACT, "2");
        testIntent.putExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP, map);

        controller = Robolectric.buildActivity(ProfileActivity.class, testIntent).create().start().resume();
        profileActivity = controller.get();

        spyActivity = Mockito.spy(profileActivity);
        Whitebox.setInternalState(profileActivity, "presenter", presenter);
        profileOverviewFragment = ProfileOverviewFragment.newInstance(spyActivity.getIntent().getExtras());
        startFragment(profileOverviewFragment);
        Assert.assertNotNull(profileOverviewFragment);
    }


    @Test
    public void testFragmentInstanceWithNullBundle() {
        Intent testIntent = new Intent();
        testIntent.putExtra(ConstantsUtils.IntentKeyUtils.BASE_ENTITY_ID, DUMMY_BASE_ENTITY_ID);
        HashMap<String, String> map = new HashMap<>();
        map.put(DBConstantsUtils.KeyUtils.NEXT_CONTACT, "3");
        testIntent.putExtra(ConstantsUtils.IntentKeyUtils.CLIENT_MAP, map);

        controller = Robolectric.buildActivity(ProfileActivity.class, testIntent).create().start().resume();
        profileActivity = controller.get();

        spyActivity = Mockito.spy(profileActivity);
        Whitebox.setInternalState(profileActivity, "presenter", presenter);
        profileOverviewFragment = ProfileOverviewFragment.newInstance(null);
        startFragment(profileOverviewFragment);
        Assert.assertNotNull(profileOverviewFragment);
    }

    private void startFragment(Fragment fragment) {
        FragmentManager fragmentManager = spyActivity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(fragment, null);
        fragmentTransaction.commit();

        getActivity().runOnUiThread(() -> spyActivity.getSupportFragmentManager().executePendingTransactions());

        getInstrumentation().waitForIdleSync();
    }
}
