package org.smartregister.fp.interactor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.smartregister.fp.activity.BaseUnitTest;
import org.smartregister.fp.features.profile.presenter.ProfilePresenter;

public class ProfileInteractorTest extends BaseUnitTest {

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetProfileViewShouldReturnNullIfPresenterParameterIsNull() {
        ProfilePresenter presenter = new ProfilePresenter(null);
        ProfilePresenter presenterSpy = Mockito.spy(presenter);

        Assert.assertNull(presenterSpy.getProfileView());
    }

}
