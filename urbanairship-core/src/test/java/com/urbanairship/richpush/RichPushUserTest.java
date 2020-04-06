/* Copyright Airship and Contributors */

package com.urbanairship.richpush;

import com.urbanairship.BaseTestCase;
import com.urbanairship.PreferenceDataStore;
import com.urbanairship.TestApplication;
import com.urbanairship.channel.AirshipChannel;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class RichPushUserTest extends BaseTestCase {

    private final String fakeUserId = "fakeUserId";
    private final String fakeToken = "fakeToken";
    private final String fakeChannelId = "fakeChannelId";

    private RichPushUser user;
    private PreferenceDataStore dataStore;
    private TestUserListener listener;
    private AirshipChannel mockChannel;
    private RichPushUser mockUser;

    @Before
    public void setUp() {
        dataStore = TestApplication.getApplication().preferenceDataStore;
        user = new RichPushUser(dataStore);
        listener = new TestUserListener();
        user.addListener(listener);

        mockChannel = Mockito.mock(AirshipChannel.class);
        mockUser = Mockito.mock(RichPushUser.class);
        TestApplication.getApplication().setChannel(mockChannel);

    }

    /**
     * Test isCreated returns true when user has been created.
     */
    @Test
    public void testIsCreatedTrue() {
        user.setUser(fakeUserId, fakeToken);
        assertTrue("Should return true.", user.isUserCreated());
    }

    /**
     * Test isCreated returns false when user has not been created.
     */
    @Test
    public void testIsCreatedFalse() {
        // Clear any user or user token
        user.setUser(null, null);
        assertFalse("Should return false.", user.isUserCreated());
    }

    /**
     * Test isCreated returns false when user token doesn't exist.
     */
    @Test
    public void testIsCreatedFalseNoUserToken() {
        user.setUser(fakeUserId, null);
        assertFalse("Should return false.", user.isUserCreated());
    }

    /**
     * Test setting and getting the user credentials.
     */
    @Test
    public void testUser() throws JSONException {
        user.setUser(fakeUserId, fakeToken);

        assertEquals("User ID should match", fakeUserId, user.getId());
        assertEquals("User password should match", fakeToken, user.getPassword());
    }

    /**
     * Test setting and getting the user credentials.
     */
    @Test
    public void testUserMissingId() throws JSONException {
        user.setUser(null, fakeToken);

        assertNull(user.getId());
        assertNull(user.getPassword());
    }

    /**
     * Test setting and getting the user credentials.
     */
    @Test
    public void testUserMissingToken() {
        user.setUser(fakeUserId, null);

        assertNull(user.getId());
        assertNull(user.getPassword());
    }

    /**
     * Test user token is obfuscated when stored in preferences.
     */
    @Test
    public void testUserTokenObfuscated() {
        user.setUser(fakeUserId, fakeUserId);

        assertNotEquals(fakeToken, dataStore.getString("com.urbanairship.user.USER_TOKEN", fakeToken));
    }

    /**
     * Test migrate old token storage.
     */
    @Test
    public void testMigrateToken() {
        dataStore.put("com.urbanairship.user.PASSWORD", fakeToken);
        dataStore.put("com.urbanairship.user.ID", fakeUserId);

        user = new RichPushUser(dataStore);

        assertEquals("User ID should match", fakeUserId, user.getId());
        assertEquals("User password should match", fakeToken, user.getPassword());

        assertNull(dataStore.getString("com.urbanairship.user.PASSWORD", null));
    }


    /**
     * Tests update user starts the rich push service and notifies the listener
     * on a success result
     */
    @Test
    public void testRichPushUpdateSuccess() {
        user.onUserUpdated(true);

        // Verify the listener received a success callback
        assertTrue("Listener should be notified of user update success.", listener.lastUpdateUserResult);
    }

    /**
     * Tests update user starts the rich push service and notifies the listener
     * on an error result
     */
    @Test
    public void testRichPushUpdateError() {
        user.onUserUpdated(false);

        // Verify the listener received a success callback
        assertFalse("Listener should be notified of user update failed.",
                listener.lastUpdateUserResult);
    }

    /**
     * Tests if the user should be updated
     */
    @Test
    public void testShouldUpdate() {
        assertTrue(user.shouldUpdate());
    }

    /**
     * Tests if the user should not be updated
     */
    @Test
    public void testShouldUpdateFalse() {
        // Set a channel ID
        when(mockChannel.getId()).thenReturn(fakeChannelId);
        dataStore.put("com.urbanairship.user.REGISTERED_CHANNEL_ID", fakeChannelId);
        assertFalse(user.shouldUpdate());
    }

    /**
     * Listener that captures the last update user result
     */
    private class TestUserListener implements RichPushUser.Listener {

        Boolean lastUpdateUserResult = null;

        @Override
        public void onUserUpdated(boolean success) {
            lastUpdateUserResult = success;
        }

    }

}
