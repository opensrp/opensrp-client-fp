package org.smartregister.fp.adapter;


import android.widget.LinearLayout;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.reflect.Whitebox;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.fp.activity.BaseUnitTest;
import org.smartregister.fp.common.library.FPLibrary;
import org.smartregister.fp.features.profile.adapter.ClientHistoryAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ClientHistoryAdapterTest extends BaseUnitTest {
    private ClientHistoryAdapter clientHistoryAdapter;
    @Mock
    private List<HashMap<String, String>> clientHistoryList;
    @Mock
    private ClientHistoryAdapter.HistoryItemClickListener itemClickListener;
    @Mock
    private FPLibrary fpLibrary;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        clientHistoryAdapter = new ClientHistoryAdapter(clientHistoryList, itemClickListener);
        ReflectionHelpers.setStaticField(FPLibrary.class, "instance", fpLibrary);
    }

    @Test
    public void testClientHistoryAdapterInstantiatesCorrectly() {
        Assert.assertNotNull(clientHistoryAdapter);
    }

    @Test
    public void testOnCreateViewHolderReturnsValidViewHolderInstance() {
        LinearLayout viewGroup = new LinearLayout(RuntimeEnvironment.application);
        viewGroup.setLayoutParams(new LinearLayout.LayoutParams(100, 200));
        ClientHistoryAdapter.ViewHolder viewHolder = clientHistoryAdapter.onCreateViewHolder(viewGroup, 0);
        Assert.assertNotNull(viewHolder);
    }

    @Test
    public void testGetItemCountInvokesGetSizeMethodOfDataList() {
        Whitebox.setInternalState(clientHistoryAdapter, "data", new ArrayList<>());
        Assert.assertEquals(0, clientHistoryAdapter.getItemCount());
    }
}
