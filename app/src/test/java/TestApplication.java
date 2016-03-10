import org.mercycorps.translationcards.MainApplication;
import org.mercycorps.translationcards.data.DbManager;
import org.mercycorps.translationcards.media.MediaPlayerManager;
import org.robolectric.TestLifecycleApplication;

import java.lang.reflect.Method;

import static org.mockito.Mockito.mock;

public class TestApplication extends MainApplication implements TestLifecycleApplication {
    @Override
    public void beforeTest(Method method) {

    }

    @Override
    public void prepareTest(Object o) {

    }

    @Override
    public void afterTest(Method method) {

    }

    @Override
    public MediaPlayerManager getMediaPlayerManager() {
        return mock(MediaPlayerManager.class);
    }
}
