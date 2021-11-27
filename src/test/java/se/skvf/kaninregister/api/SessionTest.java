package se.skvf.kaninregister.api;

import org.junit.jupiter.api.Test;
import se.skvf.kaninregister.model.Owner;

import java.io.IOException;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

public class SessionTest extends BunnyRegistryApiTest {

    @Test
    public void session_restartPreviousSession() throws IOException {

        String ownerId = randomUUID().toString();
        String sessionId = mockSession(ownerId);

        when(sessions.getOwnerIdForSession(sessionId)).thenReturn(ownerId);

        Owner owner = mockOwner(ownerId);

        OwnerDTO dto = api.session();

        assertOwner(dto, owner);
    }

    @Test
    public void session_noPreviousSession_But_Cookie() {

        String ownerId = randomUUID().toString();
        String sessionId = mockSession(ownerId);

        when(sessions.getOwnerIdForSession(sessionId)).thenReturn(null);

        assertNull(api.session());
    }

    @Test
    public void session_noPreviousSession() {
        assertNull(api.session());
    }

}
