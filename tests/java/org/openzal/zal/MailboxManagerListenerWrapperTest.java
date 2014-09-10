package java.org.openzal.zal;

import org.junit.Test;
import org.mockito.Mockito;
import org.openzal.zal.MailboxManagerListener;
import org.openzal.zal.MailboxManagerListenerWrapper;

import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.*;

public class MailboxManagerListenerWrapperTest
{
  @Test
  public void equals()
  {
    MailboxManagerListener listener = Mockito.mock(MailboxManagerListener.class);

    MailboxManagerListenerWrapper firstWrapper = new MailboxManagerListenerWrapper(listener);
    MailboxManagerListenerWrapper secondWrapper = new MailboxManagerListenerWrapper(listener);

    assertEquals(firstWrapper, secondWrapper);
  }

  @Test
  public void not_equals()
  {
    MailboxManagerListener firstListener = Mockito.mock(MailboxManagerListener.class);
    MailboxManagerListener secondListener = Mockito.mock(MailboxManagerListener.class);

    MailboxManagerListenerWrapper firstWrapper = new MailboxManagerListenerWrapper(firstListener);
    MailboxManagerListenerWrapper secondWrapper = new MailboxManagerListenerWrapper(secondListener);

    assertNotEquals(firstWrapper, secondWrapper);
  }

  @Test
  public void add_listener()
  {
    MailboxManagerListener firstListener = Mockito.mock(MailboxManagerListener.class);
    MailboxManagerListener secondListener = Mockito.mock(MailboxManagerListener.class);

    CopyOnWriteArrayList<MailboxManagerListenerWrapper> listeners =
      new CopyOnWriteArrayList<MailboxManagerListenerWrapper>();

    assertFalse(listeners.contains(new MailboxManagerListenerWrapper(firstListener)));
    assertFalse(listeners.contains(new MailboxManagerListenerWrapper(secondListener)));

    listeners.add(new MailboxManagerListenerWrapper(firstListener));
    assertTrue(listeners.contains(new MailboxManagerListenerWrapper(firstListener)));
    assertFalse(listeners.contains(new MailboxManagerListenerWrapper(secondListener)));

    listeners.add(new MailboxManagerListenerWrapper(secondListener));
    assertTrue(listeners.contains(new MailboxManagerListenerWrapper(firstListener)));
    assertTrue(listeners.contains(new MailboxManagerListenerWrapper(secondListener)));

    assertEquals(2, listeners.size());
  }

  @Test
  public void remove_listener()
  {
    MailboxManagerListener firstListener = Mockito.mock(MailboxManagerListener.class);
    MailboxManagerListener secondListener = Mockito.mock(MailboxManagerListener.class);

    CopyOnWriteArrayList<MailboxManagerListenerWrapper> listeners =
      new CopyOnWriteArrayList<MailboxManagerListenerWrapper>();

    listeners.add(new MailboxManagerListenerWrapper(firstListener));
    listeners.add(new MailboxManagerListenerWrapper(secondListener));

    assertTrue(listeners.remove(new MailboxManagerListenerWrapper(firstListener)));
    assertFalse(listeners.remove(new MailboxManagerListenerWrapper(firstListener)));

    assertFalse(listeners.contains(new MailboxManagerListenerWrapper(firstListener)));
    assertTrue(listeners.contains(new MailboxManagerListenerWrapper(secondListener)));

    assertEquals(1, listeners.size());
  }
}