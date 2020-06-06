package org.smartregister.fp.features.home.interactor;


import android.util.Pair;

import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.domain.UniqueId;
import org.smartregister.domain.db.EventClient;
import org.smartregister.fp.activity.BaseUnitTest;
import org.smartregister.fp.common.helper.ECSyncHelper;
import org.smartregister.fp.common.sync.BaseFPClientProcessorForJava;
import org.smartregister.fp.common.util.AppExecutors;
import org.smartregister.fp.features.home.contract.RegisterContract;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.UniqueIdRepository;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class RegisterInteractorTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    private RegisterContract.Interactor interactor;
    @Captor
    private ArgumentCaptor<Triple<String, String, String>> tripleArgumentCaptor;

    @Captor
    private ArgumentCaptor<JSONObject> jsonArgumentCaptor;

    @Captor
    private ArgumentCaptor<List<EventClient>> eventClientArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<Long> longArgumentCaptor;

    @Before
    public void setUp() {
        interactor = new RegisterInteractor(new AppExecutors(Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor(), Executors.newSingleThreadExecutor()));
    }

    @Test
    public void testGetNextUniqueId() {
        UniqueIdRepository uniqueIdRepository = Mockito.mock(UniqueIdRepository.class);
        RegisterContract.InteractorCallBack callBack = Mockito.mock(RegisterContract.InteractorCallBack.class);

        RegisterInteractor registerInteractor = (RegisterInteractor) interactor;
        registerInteractor.setUniqueIdRepository(uniqueIdRepository);

        String formName = "fp_register";
        String metadata = "metadata";
        String currentLocationId = "Location Id";
        String openmrsId = "openmrs id";

        UniqueId uniqueId = new UniqueId();
        uniqueId.setOpenmrsId(openmrsId);

        Mockito.when(uniqueIdRepository.getNextUniqueId()).thenReturn(uniqueId);
        registerInteractor.getNextUniqueId(Triple.of(formName, metadata, currentLocationId), callBack);
        verify(uniqueIdRepository, timeout(ASYNC_TIMEOUT)).getNextUniqueId();

        verify(callBack, timeout(ASYNC_TIMEOUT)).onUniqueIdFetched(tripleArgumentCaptor.capture(), stringArgumentCaptor.capture());

        Triple<String, String, String> triple = tripleArgumentCaptor.getValue();
        assertEquals(formName, triple.getLeft());
        assertEquals(metadata, triple.getMiddle());
        assertEquals(currentLocationId, triple.getRight());
        assertEquals(openmrsId, stringArgumentCaptor.getValue());

    }

    @Test
    public void testFailedToGetNextUniqueId() {
        UniqueIdRepository uniqueIdRepository = Mockito.mock(UniqueIdRepository.class);
        RegisterContract.InteractorCallBack callBack = Mockito.mock(RegisterContract.InteractorCallBack.class);

        RegisterInteractor registerInteractor = (RegisterInteractor) interactor;
        registerInteractor.setUniqueIdRepository(uniqueIdRepository);

        String formName = "fp_register";
        String metadata = "metadata";
        String currentLocationId = "Location Id";

        Triple<String, String, String> triple = Triple.of(formName, metadata, currentLocationId);

        Mockito.when(uniqueIdRepository.getNextUniqueId()).thenReturn(null);
        registerInteractor.getNextUniqueId(triple, callBack);
        verify(uniqueIdRepository, timeout(ASYNC_TIMEOUT)).getNextUniqueId();

        verify(callBack, timeout(ASYNC_TIMEOUT)).onNoUniqueId();
    }

    @Test
    public void testSaveNewRegistration() throws Exception {
        UniqueIdRepository uniqueIdRepository = Mockito.mock(UniqueIdRepository.class);
        ECSyncHelper syncHelper = Mockito.mock(ECSyncHelper.class);
        AllSharedPreferences allSharedPreferences = Mockito.mock(AllSharedPreferences.class);
        BaseFPClientProcessorForJava baseAncClientProcessorForJava = Mockito.mock(BaseFPClientProcessorForJava.class);
        RegisterContract.InteractorCallBack callBack = Mockito.mock(RegisterContract.InteractorCallBack.class);

        RegisterInteractor registerInteractor = (RegisterInteractor) interactor;
        registerInteractor.setUniqueIdRepository(uniqueIdRepository);
        registerInteractor.setSyncHelper(syncHelper);
        registerInteractor.setAllSharedPreferences(allSharedPreferences);
        registerInteractor.setClientProcessorForJava(baseAncClientProcessorForJava);

        String baseEntityId = "112123";
        String fpId = "1324354";

        Client client = new Client(baseEntityId);
        Map<String, String> identifiers = new HashMap<>();
        identifiers.put("client_id", fpId);
        client.setIdentifiers(identifiers);

        Event event = new Event();
        event.setBaseEntityId(baseEntityId);

        Pair<Client, Event> pair = Pair.create(client, event);

        JSONObject clientObject = new JSONObject(JsonFormUtils.gson.toJson(client));
        JSONObject eventObject = new JSONObject(JsonFormUtils.gson.toJson(event));

        String jsonString = "{'json':'string'}";

        long timestamp = new Date().getTime();

        List<EventClient> eventClients = new ArrayList<>();
        EventClient eventClient = new EventClient(JsonFormUtils.gson.fromJson(eventObject.toString(), org.smartregister.domain.db.Event.class),
                JsonFormUtils.gson.fromJson(clientObject.toString(), org.smartregister.domain.db.Client.class));
        eventClients.add(eventClient);

        Mockito.doReturn(timestamp).when(allSharedPreferences).fetchLastUpdatedAtDate(0);
        Mockito.doReturn(eventClients).when(syncHelper).getEvents(new Date(timestamp), BaseRepository.TYPE_Unprocessed);

        registerInteractor.saveRegistration(pair, jsonString, false, callBack);

        verify(syncHelper, timeout(ASYNC_TIMEOUT)).addClient(stringArgumentCaptor.capture(), jsonArgumentCaptor.capture());

        assertEquals(baseEntityId, stringArgumentCaptor.getValue());

        assertEquals(clientObject.get("type"), jsonArgumentCaptor.getValue().get("type"));
        assertEquals(clientObject.get("baseEntityId"), jsonArgumentCaptor.getValue().get("baseEntityId"));
        assertEquals(clientObject.getJSONObject("identifiers").get("client_id"), jsonArgumentCaptor.getValue().getJSONObject("identifiers").get("client_id"));

        verify(syncHelper, timeout(ASYNC_TIMEOUT)).addEvent(stringArgumentCaptor.capture(), jsonArgumentCaptor.capture());
        assertEquals(baseEntityId, stringArgumentCaptor.getValue());

        assertEquals(eventObject.get("type"), jsonArgumentCaptor.getValue().get("type"));
        assertEquals(eventObject.getString("baseEntityId"), jsonArgumentCaptor.getValue().getString("baseEntityId"));
        assertEquals(eventObject.getString("duration"), jsonArgumentCaptor.getValue().getString("duration"));
        assertEquals(eventObject.getString("version"), jsonArgumentCaptor.getValue().getString("version"));

        verify(uniqueIdRepository, timeout(ASYNC_TIMEOUT)).close(stringArgumentCaptor.capture());
        assertEquals(fpId, stringArgumentCaptor.getValue());

        verify(baseAncClientProcessorForJava, timeout(ASYNC_TIMEOUT)).processClient(eventClientArgumentCaptor.capture());
        assertEquals(eventClients, eventClientArgumentCaptor.getValue());

        verify(allSharedPreferences, timeout(ASYNC_TIMEOUT)).saveLastUpdatedAtDate(longArgumentCaptor.capture());
        assertEquals(new Long(timestamp), longArgumentCaptor.getValue());

        verify(callBack, timeout(ASYNC_TIMEOUT)).onRegistrationSaved(ArgumentMatchers.anyBoolean());
    }

}
