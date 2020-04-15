package org.smartregister.fp.fragment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.internal.WhiteboxImpl;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.fp.activity.BaseUnitTest;
import org.smartregister.fp.features.home.repository.RegisterQueryProvider;
import org.smartregister.fp.features.home.view.HomeRegisterFragment;

import java.util.Arrays;

public class HomeRegisterFragmentTest extends BaseUnitTest {

    @Mock
    private FPLibrary ancLibrary;

    private HomeRegisterFragment homeRegisterFragment;

    @Mock
    private CommonRepository commonRepository;

    @Mock
    private RecyclerViewPaginatedAdapter clientAdapter;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        homeRegisterFragment = Mockito.spy(HomeRegisterFragment.class);
    }

    @Test
    public void testCountExecuteShouldPopulateClientAdapterWithCorrectValues() {
        ReflectionHelpers.setStaticField(FPLibrary.class, "instance", ancLibrary);
        WhiteboxImpl.setInternalState(homeRegisterFragment, "clientAdapter", clientAdapter);
        Mockito.doReturn(commonRepository).when(homeRegisterFragment).commonRepository();
        Mockito.when(commonRepository.countSearchIds(Mockito.anyString())).thenReturn(10);
        Mockito.when(ancLibrary.getRegisterQueryProvider()).thenReturn(new RegisterQueryProvider());
        homeRegisterFragment.countExecute();
        Mockito.verify(clientAdapter, Mockito.times(1)).setTotalcount(Mockito.anyInt());
        Mockito.verify(clientAdapter, Mockito.times(1)).setCurrentlimit(Mockito.anyInt());
        Mockito.verify(clientAdapter, Mockito.times(1)).setCurrentoffset(Mockito.anyInt());
    }

    @Test
    public void testFilterAndSortQuery() throws Exception {
        ReflectionHelpers.setStaticField(FPLibrary.class, "instance", ancLibrary);
        Mockito.when(ancLibrary.getRegisterQueryProvider()).thenReturn(new RegisterQueryProvider());
        Mockito.doReturn(commonRepository).when(homeRegisterFragment).commonRepository();
        Mockito.when(commonRepository.isFts()).thenReturn(true);
        Mockito.when(commonRepository.findSearchIds(Mockito.anyString())).thenReturn(Arrays.asList("2323-23", "546456-234"));
        WhiteboxImpl.setInternalState(homeRegisterFragment, "mainSelect", "");
        WhiteboxImpl.setInternalState(homeRegisterFragment, "clientAdapter", clientAdapter);
        Mockito.when(clientAdapter.getCurrentlimit()).thenReturn(10);
        Mockito.when(clientAdapter.getCurrentoffset()).thenReturn(1);
        String result = WhiteboxImpl.invokeMethod(homeRegisterFragment, "filterAndSortQuery");
        String expected = "Select ec_client.id as _id , ec_client.relationalid , ec_client.last_interacted_with , ec_client.base_entity_id , ec_client.first_name , ec_client.last_name , ec_client.client_id , ec_client.dob , ec_client.date_removed FROM ec_client  order by last_interacted_with DESC";
        Assert.assertEquals(expected, result);
    }
}