package org.openzal.zal.extension;

import com.zimbra.cs.mailbox.ZimbraSimulator;
import org.junit.Rule;
import org.junit.Test;
import org.openzal.zal.*;
import org.openzal.zal.Mailbox;
import org.openzal.zal.OperationContext;
import org.openzal.zal.exceptions.NoSuchItemException;

import java.util.HashMap;

public class ExceptionWrapperTest
{
  @Rule
  public ZimbraSimulator mZimbraSimulator = new ZimbraSimulator();

  @Test (expected = NoSuchItemException.class)
  public void wrap_new_no_suchitem_exception() throws Exception
  {
    Account account = mZimbraSimulator.getProvisioning().createAccount("test", "iddddd", new HashMap<String, Object>());
    Mailbox mbox = mZimbraSimulator.getMailboxManager().getMailboxByAccount(account);
    OperationContext octxt = mbox.newOperationContext();
    mbox.getTagById(octxt,10);
  }
}