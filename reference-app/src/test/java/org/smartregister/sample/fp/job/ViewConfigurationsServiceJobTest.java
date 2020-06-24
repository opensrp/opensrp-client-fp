package org.smartregister.sample.fp.job;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.smartregister.sample.fp.base.BaseUnitTest;

public class ViewConfigurationsServiceJobTest extends BaseUnitTest {

    @Mock
    private Context context;

    @Mock
    private ComponentName componentName;

    @Before
    public void setUp() {

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testOnRunJobStartsCorrectService() {

        ViewConfigurationsServiceJob viewConfigurationsServiceJob = new ViewConfigurationsServiceJob();
        ViewConfigurationsServiceJob viewConfigurationsServiceJobSpy = Mockito.spy(viewConfigurationsServiceJob);

        ArgumentCaptor<Intent> intent = ArgumentCaptor.forClass(Intent.class);

        Mockito.doReturn(context).when(viewConfigurationsServiceJobSpy).getApplicationContext();
        Mockito.doReturn(componentName).when(context).startService(ArgumentMatchers.any(Intent.class));

        viewConfigurationsServiceJobSpy.onRunJob(null);

        Mockito.verify(context).startService(intent.capture());

        Assert.assertEquals("org.smartregister.configurableviews.service.PullConfigurableViewsIntentService", intent.getValue().getComponent().getClassName());
    }
}
