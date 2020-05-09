package org.smartregister.fp.helper;

import com.vijay.jsonwizard.utils.FormUtils;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;
import org.smartregister.fp.activity.BaseUnitTest;
import org.smartregister.fp.common.helper.FPRulesEngineHelper;
import org.smartregister.fp.common.rule.AlertRule;
import org.smartregister.fp.common.rule.ScheduleRule;
import org.smartregister.fp.common.util.ConstantsUtils;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by ndegwamartin on 09/11/2018.
 */
public class FPRulesEngineHelperTest extends BaseUnitTest {

    private static final String ALERT_RULE_FIELD_TODAY_DATE = "todayDate";

    private FPRulesEngineHelper fpRulesEngineHelper;

    @Mock
    private Rules rules;

    @Mock
    private Facts facts;

    @Mock
    private RulesEngine inferentialRulesEngine;

    @Mock
    private RulesEngine defaultRulesEngine;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        fpRulesEngineHelper = new FPRulesEngineHelper(RuntimeEnvironment.application);
    }

    @Test
    public void testRulesEngineHelperConstructorCreatesValidInstance() {
        Assert.assertNotNull(fpRulesEngineHelper);
    }

    @Test
    public void testProcessInferentialRulesInvokesCorrectRulesEngineWithCorrectParameters() {

        Whitebox.setInternalState(fpRulesEngineHelper, "inferentialRulesEngine", inferentialRulesEngine);
        Whitebox.setInternalState(fpRulesEngineHelper, "defaultRulesEngine", defaultRulesEngine);

        fpRulesEngineHelper.processInferentialRules(rules, facts);

        Mockito.verify(inferentialRulesEngine).fire(rules, facts);
        Mockito.verify(defaultRulesEngine, Mockito.times(0)).fire(rules, facts);
    }

    @Test
    public void testProcessDefaultRulesInvokesCorrectRulesEngineWithCorrectParameters() {

        Whitebox.setInternalState(fpRulesEngineHelper, "inferentialRulesEngine", inferentialRulesEngine);
        Whitebox.setInternalState(fpRulesEngineHelper, "defaultRulesEngine", defaultRulesEngine);

        fpRulesEngineHelper.processDefaultRules(rules, facts);

        Mockito.verify(defaultRulesEngine).fire(rules, facts);
        Mockito.verify(inferentialRulesEngine, Mockito.times(0)).fire(rules, facts);
    }

    @Test
    public void testGetButtonAlertStatusReturnsNotDueForUndueDate() {

        //Not due
        AlertRule alertRule = new AlertRule(30, "2019-07-09");
        Whitebox.setInternalState(alertRule, ALERT_RULE_FIELD_TODAY_DATE, new LocalDate("2018-11-09"));

        String buttonAlertStatus = fpRulesEngineHelper.getButtonAlertStatus(alertRule, ConstantsUtils.RulesFileUtils.ALERT_RULES);

        assertEquals(ConstantsUtils.AlertStatusUtils.NOT_DUE, buttonAlertStatus);
    }


    @Test
    public void testGetButtonAlertStatusReturnsDueForADueDate() {
        //Due
        AlertRule alertRule = new AlertRule(30, "2018-11-09");
        Whitebox.setInternalState(alertRule, ALERT_RULE_FIELD_TODAY_DATE, new LocalDate("2018-11-09"));

        String buttonAlertStatus = fpRulesEngineHelper.getButtonAlertStatus(alertRule, ConstantsUtils.RulesFileUtils.ALERT_RULES);

        assertEquals(ConstantsUtils.AlertStatusUtils.DUE, buttonAlertStatus);
    }


    @Test
    public void testGetButtonAlertStatusReturnsOverDueForOverdueDate() {

        //OverDue
        AlertRule alertRule = new AlertRule(30, "2018-11-01");
        Whitebox.setInternalState(alertRule, ALERT_RULE_FIELD_TODAY_DATE, new LocalDate("2018-11-09"));

        String buttonAlertStatus = fpRulesEngineHelper.getButtonAlertStatus(alertRule, ConstantsUtils.RulesFileUtils.ALERT_RULES);

        assertEquals(ConstantsUtils.AlertStatusUtils.OVERDUE, buttonAlertStatus);
    }


    @Test
    public void testGetButtonAlertStatusReturnsDueDeliveryForDueDeliveryDate() {

        //delivery due
        AlertRule alertRule = new AlertRule(41, "2018-11-11");
        Whitebox.setInternalState(alertRule, ALERT_RULE_FIELD_TODAY_DATE, new LocalDate("2018-11-12"));

        String buttonAlertStatus = fpRulesEngineHelper.getButtonAlertStatus(alertRule, ConstantsUtils.RulesFileUtils.ALERT_RULES);

        assertEquals(ConstantsUtils.AlertStatusUtils.DELIVERY_DUE, buttonAlertStatus);
    }

    @Test
    public void testGetButtonAlertStatusReturnsExpiredForExpiredDeliveryDate() {

        //expired
        AlertRule alertRule = new AlertRule(42, "2018-11-04");
        Whitebox.setInternalState(alertRule, ALERT_RULE_FIELD_TODAY_DATE, new LocalDate("2018-11-11"));

        String buttonAlertStatus = fpRulesEngineHelper.getButtonAlertStatus(alertRule, ConstantsUtils.RulesFileUtils.ALERT_RULES);

        assertEquals(ConstantsUtils.AlertStatusUtils.EXPIRED, buttonAlertStatus);
    }


    @Test
    public void testGetButtonAlertStatusReturnsExpiredOverdueForOverdueDeliveryDate() {

        //Expired Overdue
        AlertRule alertRule = new AlertRule(42, "2018-10-09");
        Whitebox.setInternalState(alertRule, ALERT_RULE_FIELD_TODAY_DATE, new LocalDate("2018-11-09"));

        String buttonAlertStatus = fpRulesEngineHelper.getButtonAlertStatus(alertRule, ConstantsUtils.RulesFileUtils.ALERT_RULES);

        assertEquals(ConstantsUtils.AlertStatusUtils.EXPIRED, buttonAlertStatus);
    }

    @Test
    public void testGetContactVisitScheduleInvokesInferentialRulesEngineProcessInferentialRulesWithCorrectParams() {

        ScheduleRule scheduleRule = new ScheduleRule(20, true, DUMMY_BASE_ENTITY_ID);

        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        List<Integer> scheduleWeeksList = fpRulesEngineHelperSpy
                .getFollowupVisitScheduleDate(scheduleRule, ConstantsUtils.RulesFileUtils.VISIT_SCHEDULE_RULES);

        Assert.assertNotNull(scheduleWeeksList);

        Mockito.verify(fpRulesEngineHelperSpy)
                .processInferentialRules(ArgumentMatchers.any(Rules.class), ArgumentMatchers.any(Facts.class));

    }

    @Test
    public void testGetContactVisitScheduleGeneratesCorrectScheduleAt4Weeks() {

        ScheduleRule scheduleRule = new ScheduleRule(4, true, DUMMY_BASE_ENTITY_ID);

        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        List<Integer> scheduleWeeksList = fpRulesEngineHelperSpy
                .getFollowupVisitScheduleDate(scheduleRule, ConstantsUtils.RulesFileUtils.VISIT_SCHEDULE_RULES);

        Assert.assertNotNull(scheduleWeeksList);

        assertEquals(Arrays.asList(12, 20, 26, 30, 34, 36, 38, 40, 41), scheduleWeeksList);

    }

    @Test
    public void testGetContactVisitScheduleGeneratesCorrectScheduleAt12Weeks() {

        ScheduleRule scheduleRule = new ScheduleRule(12, true, DUMMY_BASE_ENTITY_ID);

        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        List<Integer> scheduleWeeksList = fpRulesEngineHelperSpy
                .getFollowupVisitScheduleDate(scheduleRule, ConstantsUtils.RulesFileUtils.VISIT_SCHEDULE_RULES);

        Assert.assertNotNull(scheduleWeeksList);

        assertEquals(Arrays.asList(20, 26, 30, 34, 36, 38, 40, 41), scheduleWeeksList);

    }

    @Test
    public void testGetContactVisitScheduleGeneratesCorrectScheduleAt20Weeks() {

        ScheduleRule scheduleRule = new ScheduleRule(20, true, DUMMY_BASE_ENTITY_ID);

        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        List<Integer> scheduleWeeksList = fpRulesEngineHelperSpy
                .getFollowupVisitScheduleDate(scheduleRule, ConstantsUtils.RulesFileUtils.VISIT_SCHEDULE_RULES);

        Assert.assertNotNull(scheduleWeeksList);

        assertEquals(Arrays.asList(26, 30, 34, 36, 38, 40, 41), scheduleWeeksList);

    }

    @Test
    public void testGetContactVisitScheduleGeneratesCorrectScheduleAt28Weeks() {

        ScheduleRule scheduleRule = new ScheduleRule(28, true, DUMMY_BASE_ENTITY_ID);

        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        List<Integer> scheduleWeeksList = fpRulesEngineHelperSpy
                .getFollowupVisitScheduleDate(scheduleRule, ConstantsUtils.RulesFileUtils.VISIT_SCHEDULE_RULES);

        Assert.assertNotNull(scheduleWeeksList);

        assertEquals(Arrays.asList(32, 34, 36, 38, 40, 41), scheduleWeeksList);

    }

    @Test
    public void testGetContactVisitScheduleGeneratesCorrectScheduleAt40Weeks() {

        ScheduleRule scheduleRule = new ScheduleRule(40, true, DUMMY_BASE_ENTITY_ID);

        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        List<Integer> scheduleWeeksList = fpRulesEngineHelperSpy
                .getFollowupVisitScheduleDate(scheduleRule, ConstantsUtils.RulesFileUtils.VISIT_SCHEDULE_RULES);

        Assert.assertNotNull(scheduleWeeksList);

        assertEquals(Arrays.asList(40, 41), scheduleWeeksList);

    }

    @Test
    public void testStripGaNumber() {
        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        assertEquals("12", fpRulesEngineHelperSpy.stripGaNumber("12 Weeks 7 Days"));
        assertEquals("12", fpRulesEngineHelperSpy.stripGaNumber("12"));
        assertEquals("12", fpRulesEngineHelperSpy.stripGaNumber("12 Weeks"));
        assertEquals("12", fpRulesEngineHelperSpy.stripGaNumber("12 Weeks 7"));
    }

    @Test
    public void testGetValueFromCommonInputsFieldInAccordion() throws JSONException {
        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        fpRulesEngineHelperSpy.setJsonObject(new JSONObject(DUMMY_JSON_OBJECT));
        //Test obtaining value for edit_text field
        assertEquals(fpRulesEngineHelperSpy.getValueFromAccordion("accordion_ultrasound", "step1_elly_test"), "12");
        //Test obtaining value for hidden field
        assertEquals(
                fpRulesEngineHelperSpy.getValueFromAccordion("accordion_ultrasound", "step1_ultrasound_gest_age"),
                "39 weeks 6 days");
        //Test obtaining value for number_selector field
        assertEquals(fpRulesEngineHelperSpy.getValueFromAccordion("accordion_ultrasound", "step1_no_of_fetuses"),
                "1");
        //Test obtaining value for date_picker field
        assertEquals(
                fpRulesEngineHelperSpy.getValueFromAccordion("accordion_ultrasound", "step1_blood_type_test_date"),
                "08-04-2019");
    }

    @Test
    public void testGetValueFromRadioButtonsFieldInAccordion() throws JSONException {
        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        fpRulesEngineHelperSpy.setJsonObject(new JSONObject(DUMMY_JSON_OBJECT));
        //Test obtaining value for extended_radio_button field
        assertEquals(fpRulesEngineHelperSpy.getValueFromAccordion("accordion_ultrasound", "step1_ultrasound"),
                "done_today");
        //Test obtaining value for native_radio field
        assertEquals(fpRulesEngineHelperSpy.getValueFromAccordion("accordion_ultrasound", "step1_blood_type"), "ab");

    }

    @Test
    public void testGetValueFromCheckobxFieldInAccordion() throws JSONException {
        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        fpRulesEngineHelperSpy.setJsonObject(new JSONObject(DUMMY_JSON_OBJECT));
        //Test obtaining value for check_box field
        assertEquals(
                fpRulesEngineHelperSpy.getValueFromAccordion("accordion_ultrasound", "step1_urine_test_notdone"),
                "[stock_out, expired_stock, other]");

    }

    @Test
    public void testGetValueFromAccordionWithEmptyJson() throws JSONException {
        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        assertEquals(
                fpRulesEngineHelperSpy.getValueFromAccordion("accordion_ultrasound", "step1_blood_type_test_date"), "");
    }

    @Test
    public void testGetValueFromAccordionWithMissingStep() throws JSONException {
        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        assertEquals(
                fpRulesEngineHelperSpy.getValueFromAccordion("accordion_ultrasound", "step3_blood_type_test_date"), "");
    }

    @Test
    public void testGetValueFromAccordionWithNoValues() throws JSONException {
        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        assertEquals(
                fpRulesEngineHelperSpy.getValueFromAccordion("accordion_other_tests", "step2_blood_type_test_date"), "");
    }

    @Test
    public void testCompareDateWhenFirstDateIsLower() {
        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        assertEquals(fpRulesEngineHelperSpy.compareTwoDates("31-05-2018", "31-05-2019"), -1);
        assertEquals(fpRulesEngineHelperSpy.compareTwoDates("31-01-2019", "31-05-2019"), -1);
    }

    @Test
    public void testCompareDateWhenBothDatesAreEqual() {
        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        assertEquals(fpRulesEngineHelperSpy.compareTwoDates("31-05-2019", "31-05-2019"), 0);
    }

    @Test
    public void testCompareDateWhenFirstDateIsHigher() {
        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        assertEquals(fpRulesEngineHelperSpy.compareTwoDates("31-05-2019", "31-05-2018"), 1);
        assertEquals(fpRulesEngineHelperSpy.compareTwoDates("31-05-2019", "31-01-2019"), 1);
    }

    @Test
    public void testCompareDateWhenAnyDateIsNullOrEmpty() {
        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        assertEquals(fpRulesEngineHelperSpy.compareTwoDates("", "31-05-2019"), -2);
    }

    @Test
    public void testCompareDateAgainstToday() {
        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        assertEquals(fpRulesEngineHelperSpy
                .compareDateAgainstToday((new LocalDate()).toString(FormUtils.NATIIVE_FORM_DATE_FORMAT_PATTERN)), 0);
    }

    @Test
    public void testCompareDatesWithContactDate() throws ParseException {
        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        assertTrue(fpRulesEngineHelperSpy.compareDateAgainstContactDate("31-05-2019", "2019-05-31"));
        assertTrue(fpRulesEngineHelperSpy.compareDateAgainstContactDate("30-05-2019", "2019-05-31"));
        assertFalse(fpRulesEngineHelperSpy.compareDateAgainstContactDate(null, "2019-05-31"));
    }

    @Test
    public void testConvertContactDate() throws ParseException {
        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        assertEquals(fpRulesEngineHelperSpy.convertContactDateToTestDate("2019-05-31"), "31-05-2019");
        assertEquals(fpRulesEngineHelperSpy.convertContactDateToTestDate("2018-02-01"), "01-02-2018");
        assertEquals(fpRulesEngineHelperSpy.convertContactDateToTestDate(""), "");
        assertEquals(fpRulesEngineHelperSpy.convertContactDateToTestDate(null), "");
    }

    @Test
    public void testCompareDateWithDurationsAddedAgainstToday() {
        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        assertEquals(-1, fpRulesEngineHelperSpy.compareDateWithDurationsAddedAgainstToday("31-05-2018", "28d"));
        assertEquals(-1, fpRulesEngineHelperSpy.compareDateWithDurationsAddedAgainstToday("28-11-2019", "4d"));
        assertEquals(-1, fpRulesEngineHelperSpy.compareDateWithDurationsAddedAgainstToday("28-11-2018", "2m"));
        assertEquals(1, fpRulesEngineHelperSpy.compareDateWithDurationsAddedAgainstToday("28-11-2030", "1y"));
    }

    @Test
    public void testGetRelevance() {
        FPRulesEngineHelper fpRulesEngineHelperSpy = Mockito.spy(fpRulesEngineHelper);
        boolean isRelevant = fpRulesEngineHelperSpy.getRelevance(new Facts(), "true");
        assertTrue(isRelevant);
    }
}
