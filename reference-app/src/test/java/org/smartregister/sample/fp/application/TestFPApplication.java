package org.smartregister.sample.fp.application;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.smartregister.repository.Repository;
import org.smartregister.sample.fp.R;
import org.smartregister.sample.fp.app.FPApplication;

/**
 * Created by ndegwamartin on 27/05/2018.
 */

public class TestFPApplication extends FPApplication {

    @Mock
    private Repository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        setTheme(R.style.FPAppTheme); //or just R.style.Theme_AppCompat

    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetRepositoryShouldReturnValidInstance() {
        FPApplication fpApplication = Mockito.mock(FPApplication.class);
        PowerMockito.when(fpApplication.getRepository()).thenReturn(repository);
        Repository repository = fpApplication.getRepository();
        Assert.assertNotNull(repository);
    }


}
