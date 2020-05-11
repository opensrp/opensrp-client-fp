package org.smartregister.fp.common.interactor;

import android.content.Context;
import android.util.Pair;

import androidx.annotation.VisibleForTesting;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.fp.common.contact.BaseContactContract;
import org.smartregister.fp.common.contact.ContactContract;
import org.smartregister.fp.common.domain.ClientDetail;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.common.model.PreviousContact;
import org.smartregister.fp.common.model.Task;
import org.smartregister.fp.common.repository.PreviousContactRepository;
import org.smartregister.fp.common.util.AppExecutors;
import org.smartregister.fp.common.util.ConstantsUtils;
import org.smartregister.fp.common.util.DBConstantsUtils;
import org.smartregister.fp.common.util.FPJsonFormUtils;
import org.smartregister.fp.common.util.Utils;
import org.smartregister.repository.DetailsRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by keyman 30/07/2018.
 */
public class ContactInteractor extends BaseContactInteractor implements ContactContract.Interactor {
    private Utils utils = new Utils();

    public ContactInteractor() {
        this(new AppExecutors());
    }

    @VisibleForTesting
    ContactInteractor(AppExecutors appExecutors) {
        super(appExecutors);
    }

    @Override
    public void fetchWomanDetails(String baseEntityId, BaseContactContract.InteractorCallback callBack) {
        super.fetchWomanDetails(baseEntityId, callBack);
    }

    @Override
    public HashMap<String, String> finalizeContactForm(final Map<String, String> details, Context context) {
        /*if (details != null) {
            try {
                String referral = details.get(ConstantsUtils.REFERRAL);
                String baseEntityId = details.get(DBConstantsUtils.KeyUtils.BASE_ENTITY_ID);

                int gestationAge = getGestationAge(details);
                int nextContact;
                boolean isFirst = false;
                String nextContactVisitDate;


                if (referral == null) {
                    isFirst = TextUtils.equals("1", details.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT));
                    ScheduleRule scheduleRule = new ScheduleRule(gestationAge, isFirst, baseEntityId);

                    List<Integer> integerList = FPLibrary.getInstance().getFPRulesEngineHelper()
                            .getFollowupVisitScheduleDate(scheduleRule, ConstantsUtils.RulesFileUtils.VISIT_SCHEDULE_RULES);

                    int nextContactVisitWeeks = integerList.get(0);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(ConstantsUtils.DetailsKeyUtils.CONTACT_SCHEDULE, integerList);
                    addThePreviousContactSchedule(baseEntityId, details, integerList);
                    getDetailsRepository().add(baseEntityId, ConstantsUtils.DetailsKeyUtils.CONTACT_SCHEDULE, jsonObject.toString(),
                            Calendar.getInstance().getTimeInMillis());
                    //convert String to LocalDate ;
                    LocalDate localDate = new LocalDate(details.get(DBConstantsUtils.KeyUtils.EDD));
                    nextContactVisitDate =
                            localDate.minusWeeks(ConstantsUtils.DELIVERY_DATE_WEEKS).plusWeeks(nextContactVisitWeeks).toString();
                    nextContact = getNextContact(details);
                } else {
                    nextContact = Integer.parseInt(details.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT));
                    nextContactVisitDate = details.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE);
                }

                if (referral == null) {
                    List<Task> doneTasks = utils.getContactTasksRepositoryHelper().getClosedTasks(baseEntityId);
                    utils.createTasksPartialContainer(baseEntityId, context, nextContact - 1, doneTasks);
                    removeAllDoneTasks(doneTasks);
                }

                PartialContacts partialContacts =
                        new PartialContacts(details, referral, baseEntityId, isFirst).invoke();
                PartialContactRepository partialContactRepository = partialContacts.getPartialContactRepository();
                List<PartialContact> partialContactList = partialContacts.getPartialContactList();

                ContactVisit contactVisit =
                        new ContactVisit(details, referral, baseEntityId, nextContact, nextContactVisitDate,
                                partialContactRepository, partialContactList).invoke();
                Facts facts = contactVisit.getFacts();
                List<String> formSubmissionIDs = contactVisit.getFormSubmissionIDs();
                ClientDetail clientDetail = contactVisit.getClientDetail();

                //Attention Flags
                String attentionFlagsString;
                if (referral == null) {
                    attentionFlagsString = new JSONObject(facts.asMap()).toString();
                } else {
                    attentionFlagsString = getDetailsRepository().getAllDetailsForClient(baseEntityId)
                            .get(ConstantsUtils.DetailsKeyUtils.ATTENTION_FLAG_FACTS);
                }
                addAttentionFlags(baseEntityId, details, new JSONObject(facts.asMap()).toString());
                getDetailsRepository().add(baseEntityId, ConstantsUtils.DetailsKeyUtils.ATTENTION_FLAG_FACTS, attentionFlagsString,
                        Calendar.getInstance().getTimeInMillis());

                addTheContactDate(baseEntityId, details);
                updateWomanDetails(details, clientDetail);
                if (referral != null && !TextUtils.isEmpty(details.get(DBConstantsUtils.KeyUtils.EDD))) {
                    addReferralGa(baseEntityId, details);
                }

                Pair<Event, Event> eventPair = FPJsonFormUtils.createContactVisitEvent(formSubmissionIDs, details);
                if (eventPair != null) {
                    createEvent(baseEntityId, new JSONObject(facts.asMap()).toString(), eventPair, referral);
                    JSONObject updateClientEventJson = new JSONObject(FPJsonFormUtils.gson.toJson(eventPair.second));
                    FPLibrary.getInstance().getEcSyncHelper().addEvent(baseEntityId, updateClientEventJson);
                }
            } catch (Exception e) {
                Timber.e(e, "%s --> finalizeContactForm", this.getClass().getCanonicalName());
            }
        }*/
        return (HashMap<String, String>) details;
    }

    public void addThePreviousContactSchedule(String baseEntityId, Map<String, String> details, List<Integer> integerList) {
        PreviousContact previousContact = preLoadPreviousContact(baseEntityId, details);
        previousContact.setKey(ConstantsUtils.DetailsKeyUtils.CONTACT_SCHEDULE);
        previousContact.setValue(String.valueOf(integerList));
        FPLibrary.getInstance().getPreviousContactRepository().savePreviousContact(previousContact);
    }

    protected DetailsRepository getDetailsRepository() {
        return FPLibrary.getInstance().getDetailsRepository();
    }

    public int getNextContact(Map<String, String> details) {
        int nextContact = details.containsKey(DBConstantsUtils.KeyUtils.NEXT_CONTACT) && details.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT) != null ? Integer.valueOf(details.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT)) : 1;
        nextContact += 1;
        return nextContact;
    }

    public void removeAllDoneTasks(List<Task> doneTasks) {
        for (Task task : doneTasks) {
            Long taskId = task.getId();
            utils.getContactTasksRepositoryHelper().deleteContactTask(taskId);
        }
    }

    public void addAttentionFlags(String baseEntityId, Map<String, String> details,
                                   String attentionFlagsString) {
        PreviousContact previousContact = preLoadPreviousContact(baseEntityId, details);
        previousContact.setKey(ConstantsUtils.DetailsKeyUtils.ATTENTION_FLAG_FACTS);
        previousContact.setValue(attentionFlagsString);
        FPLibrary.getInstance().getPreviousContactRepository().savePreviousContact(previousContact);
    }

    public void addTheContactDate(String baseEntityId, Map<String, String> details) {
        PreviousContact previousContact = preLoadPreviousContact(baseEntityId, details);
        previousContact.setKey(ConstantsUtils.CONTACT_DATE);
        previousContact.setValue(Utils.getDBDateToday());
        FPLibrary.getInstance().getPreviousContactRepository().savePreviousContact(previousContact);
    }

    public void updateWomanDetails(Map<String, String> details, ClientDetail clientDetail) {
        //update woman profile details
        if (details != null) {
            if (details.get(ConstantsUtils.REFERRAL) != null) {
                details.put(DBConstantsUtils.KeyUtils.LAST_CONTACT_RECORD_DATE, details.get(DBConstantsUtils.KeyUtils.LAST_CONTACT_RECORD_DATE));
                details.put(DBConstantsUtils.KeyUtils.YELLOW_FLAG_COUNT, details.get(DBConstantsUtils.KeyUtils.YELLOW_FLAG_COUNT));
                details.put(DBConstantsUtils.KeyUtils.RED_FLAG_COUNT, details.get(DBConstantsUtils.KeyUtils.RED_FLAG_COUNT));
                details.put(DBConstantsUtils.KeyUtils.CONTACT_STATUS, clientDetail.getContactStatus());
            } else {
                details.put(DBConstantsUtils.KeyUtils.CONTACT_STATUS, clientDetail.getContactStatus());
                details.put(DBConstantsUtils.KeyUtils.LAST_CONTACT_RECORD_DATE, Utils.getDBDateToday());
                details.put(DBConstantsUtils.KeyUtils.YELLOW_FLAG_COUNT, clientDetail.getYellowFlagCount().toString());
                details.put(DBConstantsUtils.KeyUtils.RED_FLAG_COUNT, clientDetail.getRedFlagCount().toString());

            }
            details.put(DBConstantsUtils.KeyUtils.NEXT_CONTACT, clientDetail.getNextContact().toString());
            details.put(DBConstantsUtils.KeyUtils.NEXT_CONTACT_DATE, clientDetail.getNextContactDate());
            details.put(DBConstantsUtils.KeyUtils.PREVIOUS_CONTACT_STATUS, clientDetail.getPreviousContactStatus());
        }
    }

    public void addReferralGa(String baseEntityId, Map<String, String> details) {
        PreviousContact previousContact = preLoadPreviousContact(baseEntityId, details);
        previousContact.setKey(ConstantsUtils.GEST_AGE_OPENMRS);
        String edd = details.get(DBConstantsUtils.KeyUtils.EDD);
        previousContact.setValue(String.valueOf(Utils.getGestationAgeFromEDDate(edd)));
        FPLibrary.getInstance().getPreviousContactRepository().savePreviousContact(previousContact);
    }

    public void createEvent(String baseEntityId, String attentionFlagsString, Pair<Event, Event> eventPair,
                             String referral)
            throws JSONException {
        Event event = eventPair.first;
        event.addDetails(ConstantsUtils.DetailsKeyUtils.ATTENTION_FLAG_FACTS, attentionFlagsString);
        String currentContactState = getCurrentContactState(baseEntityId);
        if (currentContactState != null && referral == null) {
            event.addDetails(ConstantsUtils.DetailsKeyUtils.PREVIOUS_CONTACTS, currentContactState);
        }
        JSONObject eventJson = new JSONObject(FPJsonFormUtils.gson.toJson(event));
        FPLibrary.getInstance().getEcSyncHelper().addEvent(baseEntityId, eventJson);
    }

    public PreviousContact preLoadPreviousContact(String baseEntityId, Map<String, String> details) {
        PreviousContact previousContact = new PreviousContact();
        previousContact.setBaseEntityId(baseEntityId);
        String contactNo = details.containsKey(ConstantsUtils.REFERRAL) ? details.get(ConstantsUtils.REFERRAL) :
                details.get(DBConstantsUtils.KeyUtils.NEXT_CONTACT);
        previousContact.setContactNo(contactNo);
        return previousContact;
    }

    public String getCurrentContactState(String baseEntityId) throws JSONException {
        List<PreviousContact> previousContactList = getPreviousContactRepository().getPreviousContacts(baseEntityId, null);
        JSONObject stateObject = null;
        if (previousContactList != null) {
            stateObject = new JSONObject();

            for (PreviousContact previousContact : previousContactList) {
                stateObject.put(previousContact.getKey(), previousContact.getValue());
            }
        }

        return stateObject != null ? stateObject.toString() : null;
    }

    protected PreviousContactRepository getPreviousContactRepository() {
        return FPLibrary.getInstance().getPreviousContactRepository();
    }

    public int getGestationAge(Map<String, String> details) {
        return details.containsKey(DBConstantsUtils.KeyUtils.EDD) && details.get(DBConstantsUtils.KeyUtils.EDD) != null ? Utils
                .getGestationAgeFromEDDate(details.get(DBConstantsUtils.KeyUtils.EDD)) : 4;
    }


}
