package org.smartregister.fp.util;

import android.view.View;
import android.widget.Button;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.powermock.reflect.Whitebox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.fp.R;
import org.smartregister.fp.activity.BaseUnitTest;
import org.smartregister.fp.common.domain.ButtonAlertStatus;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.repository.AllSharedPreferences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.emory.mathcs.backport.java.util.Collections;
import timber.log.Timber;

import static org.smartregister.fp.common.util.Utils.getKeyByValue;
import static org.smartregister.fp.common.util.Utils.hasPendingRequiredFields;
import static org.smartregister.fp.common.util.Utils.isEmptyMap;
import static org.smartregister.fp.common.util.Utils.processFollowupVisitButton;
import static org.smartregister.fp.common.util.Utils.reverseHyphenSeperatedValues;


/**
 * This allows integration of both powermock and robolectric
 * PowerMockIgnore annotations excludes the classes specified as params to avoid having duplicates
 */
@PowerMockRunnerDelegate(RobolectricTestRunner.class)
@PowerMockIgnore({
        "org.powermock.*", "org.mockito.*", "org.robolectric.*", "android.*", "androidx.*",
        "javax.xml.parsers.*", "com.sun.org.apache.xerces.internal.jaxp.*"
})
public class UtilsTest extends BaseUnitTest {
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @PrepareForTest({CoreLibrary.class, Context.class})
    @Test
    public void testGetNameWithNullPreferences() {
        CoreLibrary coreLibrary = PowerMockito.mock(CoreLibrary.class);
        Context context = PowerMockito.mock(Context.class);

        PowerMockito.mockStatic(CoreLibrary.class);
        PowerMockito.mockStatic(Context.class);

        PowerMockito.when(CoreLibrary.getInstance()).thenReturn(coreLibrary);
        PowerMockito.when(coreLibrary.context()).thenReturn(context);
        PowerMockito.when(context.allSharedPreferences()).thenReturn(null);

        String name = Utils.getPrefferedName();
        Assert.assertNull(name);

    }

    @PrepareForTest({CoreLibrary.class, Context.class})
    @Test
    public void testGetName() {
        String username = "userName1";

        CoreLibrary coreLibrary = PowerMockito.mock(CoreLibrary.class);
        Context context = PowerMockito.mock(Context.class);
        AllSharedPreferences allSharedPreferences = PowerMockito.mock(AllSharedPreferences.class);

        PowerMockito.mockStatic(CoreLibrary.class);
        PowerMockito.mockStatic(Context.class);

        PowerMockito.when(CoreLibrary.getInstance()).thenReturn(coreLibrary);
        PowerMockito.when(coreLibrary.context()).thenReturn(context);
        PowerMockito.when(context.allSharedPreferences()).thenReturn(allSharedPreferences);

        PowerMockito.when(allSharedPreferences.fetchRegisteredANM()).thenReturn(username);
        Assert.assertNotNull(allSharedPreferences);

        Utils.getPrefferedName();

        Mockito.verify(allSharedPreferences).getANMPreferredName(username);
        Mockito.verify(allSharedPreferences).fetchRegisteredANM();

    }

    @PrepareForTest({StringUtils.class, CoreLibrary.class, Context.class})
    @Test
    public void testGetUserInitialsWithTwoNames() {
        String username = "userName2";
        String preferredName = "Anc Reference";

        CoreLibrary coreLibrary = PowerMockito.mock(CoreLibrary.class);
        Context context = PowerMockito.mock(Context.class);
        AllSharedPreferences allSharedPreferences = Mockito.mock(AllSharedPreferences.class);

        PowerMockito.mockStatic(StringUtils.class);
        PowerMockito.mockStatic(CoreLibrary.class);
        PowerMockito.mockStatic(Context.class);

        PowerMockito.when(CoreLibrary.getInstance()).thenReturn(coreLibrary);
        PowerMockito.when(coreLibrary.context()).thenReturn(context);
        PowerMockito.when(context.allSharedPreferences()).thenReturn(allSharedPreferences);

        PowerMockito.when(allSharedPreferences.fetchRegisteredANM()).thenReturn(username);
        PowerMockito.when(allSharedPreferences.getANMPreferredName(username)).thenReturn(preferredName);

        PowerMockito.when(StringUtils.isNotBlank(preferredName)).thenReturn(true);

        Assert.assertNotNull(allSharedPreferences);
        Assert.assertNotNull(username);
        Assert.assertNotNull(preferredName);

        String initials = Utils.getUserInitials();
        Assert.assertEquals("AR", initials);

        Mockito.verify(allSharedPreferences).getANMPreferredName(username);
        Mockito.verify(allSharedPreferences).fetchRegisteredANM();
    }

    @PrepareForTest({StringUtils.class, CoreLibrary.class, Context.class})
    @Test
    public void testGetUserInitialsWithOneNames() {
        String username = "UserNAME3";
        String preferredName = "Anc";

        CoreLibrary coreLibrary = PowerMockito.mock(CoreLibrary.class);
        Context context = PowerMockito.mock(Context.class);
        AllSharedPreferences allSharedPreferences = Mockito.mock(AllSharedPreferences.class);

        PowerMockito.mockStatic(StringUtils.class);
        PowerMockito.mockStatic(CoreLibrary.class);
        PowerMockito.mockStatic(Context.class);

        PowerMockito.when(CoreLibrary.getInstance()).thenReturn(coreLibrary);
        PowerMockito.when(coreLibrary.context()).thenReturn(context);
        PowerMockito.when(context.allSharedPreferences()).thenReturn(allSharedPreferences);

        PowerMockito.when(allSharedPreferences.fetchRegisteredANM()).thenReturn(username);
        PowerMockito.when(allSharedPreferences.getANMPreferredName(username)).thenReturn(preferredName);

        PowerMockito.when(StringUtils.isNotBlank(preferredName)).thenReturn(true);

        Assert.assertNotNull(allSharedPreferences);
        Assert.assertNotNull(username);
        Assert.assertNotNull(preferredName);


        String initials = Utils.getUserInitials();
        Assert.assertEquals("A", initials);

        Mockito.verify(allSharedPreferences).getANMPreferredName(username);
        Mockito.verify(allSharedPreferences).fetchRegisteredANM();
    }

    @PrepareForTest({CoreLibrary.class, Context.class})
    @Test
    public void testGerPreferredNameWithNullSharePreferences() {
        CoreLibrary coreLibrary = PowerMockito.mock(CoreLibrary.class);
        Context context = PowerMockito.mock(Context.class);

        PowerMockito.mockStatic(CoreLibrary.class);
        PowerMockito.mockStatic(Context.class);

        PowerMockito.when(CoreLibrary.getInstance()).thenReturn(coreLibrary);
        PowerMockito.when(coreLibrary.context()).thenReturn(context);
        PowerMockito.when(context.allSharedPreferences()).thenReturn(null);

        String name = Utils.getPrefferedName();
        Assert.assertNull(name);
    }

    @PrepareForTest(StringUtils.class)
    @Test
    public void testDobStringToDateTime() {
        String dobString = "2019-01-23";

        PowerMockito.mockStatic(StringUtils.class);
        PowerMockito.when(StringUtils.isNotBlank(dobString)).thenReturn(true);

        DateTime dobStringToDateTime = Utils.dobStringToDateTime(dobString);
        Assert.assertNotNull(dobStringToDateTime);
    }

    @PrepareForTest(StringUtils.class)
    @Test
    public void testDobStringToDateTimeWithNullStringDate() {
        PowerMockito.mockStatic(StringUtils.class);
        PowerMockito.when(StringUtils.isBlank(null)).thenReturn(true);

        DateTime dobStringToDateTime = Utils.dobStringToDateTime(null);
        Assert.assertEquals(dobStringToDateTime, dobStringToDateTime);
    }

    @Test
    public void testGetListFromString() {
        String stringList = "[30, 34, 36, 38, 40, 41]";
        Assert.assertEquals(Utils.getListFromString("").size(), 0);
        Assert.assertEquals(Utils.getListFromString(stringList).size(), 6);
        Assert.assertEquals(Utils.getListFromString(null).size(), 0);
        Assert.assertEquals(Utils.getListFromString(stringList).get(2), "36");
    }

    @Test
    public void tetGetKeyByValue() {
        Map<String, String> map = new HashMap<>();
        map.put("Key1", "Val1");
        map.put("Key2", "Val2");
        map.put("Key3", "Val3");
        Assert.assertEquals("Key1", getKeyByValue(map, "Val1"));
    }

    @Test
    public void testGetKeyByValueWithEmptyMap() {
        Map<String, String> map = new HashMap<>();
        Assert.assertNull(getKeyByValue(map, "val1"));
    }

    @Test
    public void testReverseHyphenSeparatedValues() {
        Assert.assertEquals("", reverseHyphenSeperatedValues("", "-"));
        Assert.assertEquals("", reverseHyphenSeperatedValues(null, "-"));
        Assert.assertEquals("16-05-2019", reverseHyphenSeperatedValues(" 2019-05-16", "-"));
        Assert.assertEquals("16-05-2019", reverseHyphenSeperatedValues("2019-05-16 ", "-"));
        Assert.assertEquals("16-05-2019", reverseHyphenSeperatedValues("2019-05-16", "-"));
    }

    @Test
    public void testIsEmptyMap() {
        Assert.assertTrue(isEmptyMap(new HashMap<>()));
    }

    @Test
    public void testCreateExpansionPanelValues() throws Exception {
        List list = Collections.unmodifiableList(Arrays.asList("Hepatitis C test:Done today",
                "Hep C test date:28-05-2019",
                "Hep C test type:Anti-HCV rapid diagnostic test (RDT)",
                "Anti-HCV rapid diagnostic test (RDT):Positive"));

        JSONObject mainObject = getMainJsonObject("json_test_forms/expansion_panel_json_array");
        JSONArray expansionValues = mainObject.getJSONArray("expansion_values");
        com.vijay.jsonwizard.utils.Utils utils = new com.vijay.jsonwizard.utils.Utils();
        List<String> result = utils.createExpansionPanelChildren(expansionValues);
        //Compare returned list sizes and the second and last values
        Assert.assertEquals(4, result.size());
        Assert.assertEquals(list.get(1), result.get(1));
        Assert.assertEquals(list.get(3), result.get(3));
    }

    @Test
    public void testHasPendingRequiredFields() throws Exception {
        //Checks if there are required fields that don't have value
        JSONObject mainObject = getMainJsonObject("json_test_forms/test_checkbox_filter_json_form");
        Assert.assertTrue(hasPendingRequiredFields(mainObject));
    }

    @Test
    public void testGetGestationAgeFromEDDateWhenDateisZero() {
        int gestAge = Utils.getGestationAgeFromEDDate("0");
        Assert.assertEquals(0, gestAge);
    }

    @Test
    public void testGetGestationAgeFromEDDateToThrowException() {
        int gestAge = Utils.getGestationAgeFromEDDate("10-12-2020");
        Assert.assertEquals(0, gestAge);
    }

    @Test
    public void testGetGestationAgeFromEDDate() {
        int gestAge = Utils.getGestationAgeFromEDDate("2020-08-01");
        Assert.assertThat(gestAge, Matchers.greaterThanOrEqualTo(6));
    }

    @Test
    public void testGetInProgressDisplayTemplateOnRegister() {
        try {
            String displayTemplate = Whitebox.invokeMethod(Utils.class, "getDisplayTemplate", RuntimeEnvironment.application, ConstantsUtils.AlertStatusUtils.IN_PROGRESS, false);
            Assert.assertEquals("FOLLOW UP %1$s\n IN PROGRESS", displayTemplate);
        } catch (Exception e) {
            Timber.e(e, " --> testGetDisplayTemplate");
        }
    }

    @Test
    public void testGetInProgressDisplayTemplateOnProfile() {
        try {
            String displayTemplate = Whitebox.invokeMethod(Utils.class, "getDisplayTemplate", RuntimeEnvironment.application, ConstantsUtils.AlertStatusUtils.IN_PROGRESS, true);
            Assert.assertEquals("FOLLOW UP %1$s · IN PROGRESS", displayTemplate);
        } catch (Exception e) {
            Timber.e(e, " --> testGetDisplayTemplate");
        }
    }

    @Test
    public void testGetNotDueDisplayTemplateOnRegister() {
        try {
            String displayTemplate = Whitebox.invokeMethod(Utils.class, "getDisplayTemplate", RuntimeEnvironment.application, ConstantsUtils.AlertStatusUtils.NOT_DUE, false);
            Assert.assertEquals("FOLLOW UP %1$d\n DUE \n %2$s", displayTemplate);
        } catch (Exception e) {
            Timber.e(e, " --> testGetDisplayTemplate");
        }
    }

    @Test
    public void testGetNotDueDisplayTemplateOnProfile() {
        try {
            String displayTemplate = Whitebox.invokeMethod(Utils.class, "getDisplayTemplate", RuntimeEnvironment.application, ConstantsUtils.AlertStatusUtils.NOT_DUE, true);
            Assert.assertEquals("FOLLOW UP %1$d · DUE · %2$s", displayTemplate);
        } catch (Exception e) {
            Timber.e(e, " --> testGetDisplayTemplate");
        }
    }

    @Test
    public void testGetDefaultDisplayTemplateOnRegister() {
        try {
            String displayTemplate = Whitebox.invokeMethod(Utils.class, "getDisplayTemplate", RuntimeEnvironment.application, ConstantsUtils.AlertStatusUtils.DUE, false);
            Assert.assertEquals("FOLLOW UP %1$s\n%2$s", displayTemplate);
        } catch (Exception e) {
            Timber.e(e, " --> testGetDisplayTemplate");
        }
    }

    @Test
    public void testGetDefaultDisplayTemplateOnProfile() {
        try {
            String displayTemplate = Whitebox.invokeMethod(Utils.class, "getDisplayTemplate", RuntimeEnvironment.application, ConstantsUtils.AlertStatusUtils.DUE, true);
            Assert.assertEquals("START · FOLLOW UP %1$s · %2$s", displayTemplate);
        } catch (Exception e) {
            Timber.e(e, " --> testGetDisplayTemplate");
        }
    }

    @Test
    public void testAllProcessFollowupVisitButton() {
        android.content.Context context = RuntimeEnvironment.application.getApplicationContext();
        Button button = new Button(context);
        ButtonAlertStatus buttonAlertStatus = new ButtonAlertStatus();
        //Default button alert configuration
        buttonAlertStatus.nextContactDate = "26 MAY 2020";

        //Process  status not due
        buttonAlertStatus.buttonAlertStatus = ConstantsUtils.AlertStatusUtils.NOT_DUE;
        processFollowupVisitButton(context, button, buttonAlertStatus, null, null);
        Assert.assertTrue(assertFollowUpButtonStatus(context, button, ConstantsUtils.AlertStatusUtils.NOT_DUE, buttonAlertStatus));
        //Process  status  due
        buttonAlertStatus.buttonAlertStatus = ConstantsUtils.AlertStatusUtils.DUE;
        processFollowupVisitButton(context, button, buttonAlertStatus, null, null);
        Assert.assertTrue(assertFollowUpButtonStatus(context, button, ConstantsUtils.AlertStatusUtils.DUE, buttonAlertStatus));
        //Process  status overdue
        buttonAlertStatus.buttonAlertStatus = ConstantsUtils.AlertStatusUtils.OVERDUE;
        processFollowupVisitButton(context, button, buttonAlertStatus, null, null);
        Assert.assertTrue(assertFollowUpButtonStatus(context, button, ConstantsUtils.AlertStatusUtils.OVERDUE, buttonAlertStatus));
        //Process  status  expired
        buttonAlertStatus.buttonAlertStatus = ConstantsUtils.AlertStatusUtils.EXPIRED;
        processFollowupVisitButton(context, button, buttonAlertStatus, null, null);
        Assert.assertTrue(assertFollowUpButtonStatus(context, button, ConstantsUtils.AlertStatusUtils.EXPIRED, buttonAlertStatus));
    }

    private boolean assertFollowUpButtonStatus(android.content.Context context, Button button, String status, ButtonAlertStatus buttonAlertStatus) {
        boolean result;
        switch (status) {
            case ConstantsUtils.AlertStatusUtils.NOT_DUE:
                result = (button.getVisibility() == (View.VISIBLE)) &&
                        button.getText().toString().equals(context.getString(R.string.followup_date, buttonAlertStatus.nextContactDate));
                break;
            case ConstantsUtils.AlertStatusUtils.DUE:
                result = (button.getVisibility() == (View.VISIBLE)) &&
                        button.getText().toString().equals(context.getResources().getString(R.string.followup_due));
                break;
            case ConstantsUtils.AlertStatusUtils.OVERDUE:
                result = (button.getVisibility() == (View.VISIBLE)) &&
                        button.getText().toString().equals(context.getResources().getString(R.string.followup_overdue));
                break;
            case ConstantsUtils.AlertStatusUtils.EXPIRED:
            default:
                result = (button.getVisibility() == (View.GONE));
                break;
        }
        return result;
    }
}
