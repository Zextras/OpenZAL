package com.zimbra.cs.mailbox;

import com.zextras.lib.Error.UnableToRegisterDatabaseDriverError;
import com.zextras.lib.log.ZELog;
import com.zextras.lib.vfs.ramvfs.RamFS;
import org.openzal.zal.lib.ZimbraVersion;
import org.openzal.zal.Utils;
import org.openzal.zal.ZEMailboxManager;
import org.openzal.zal.ZEProvisioning;
import org.openzal.zal.ZEStoreManager;
import org.openzal.zal.ZEStoreManagerImp;
import com.zimbra.common.localconfig.ConfigException;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.HSQLZimbraDatabase;
import com.zimbra.cs.ldap.ZLdapFilterFactorySimulator;
import com.zimbra.cs.store.StoreManagerSimulator;
import com.zimbra.cs.store.StoreManager;
import org.dom4j.DocumentException;

/* $if ZimbraVersion >= 8.0.0 $ */
/* $else$
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.store.*;
import com.zimbra.cs.store.file.*;
 $endif$ */

/* $if ZimbraVersion < 8.0.0$
import com.zimbra.cs.index.MailboxIndex;
$else$ */
/* $endif$ */


// for testing purpose only
public class ZimbraSimulator
{
  private final ZEStoreManagerImp mStoreManager;

  public RamFS getStoreRoot()
  {
    return mStoreRoot;
  }

  private RamFS mStoreRoot;

  public ZimbraSimulator() throws UnableToRegisterDatabaseDriverError
  {
    try
    {
      Class.forName("org.hsqldb.jdbcDriver");
    }
    catch (Exception e)
    {
      ZELog.chat.err("Error loading DB Driver: " + Utils.exceptionToString(e));
      UnableToRegisterDatabaseDriverError newEx = new UnableToRegisterDatabaseDriverError();
      newEx.initCause(e);
      throw newEx;
    }

    init();

    mStoreManager = new ZEStoreManagerImp(
      StoreManager.getInstance()
    );
  }

  private void init()
  {
    try
    {
      initProperties();
      initIndexing();
      initStorageManager();
      initProvisioning();
      initHSQLDatabase();
      initMailboxManager();

      /* $if ZimbraVersion < 8.0.0 $
      Volume.reloadVolumes();
       $endif$ */
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  private void initProperties() throws ConfigException, DocumentException
  {
    System.setProperty("zimbra.native.required", "false");
    System.setProperty("log4j.configuration", "it/data/zimbra-config/log4j-test.properties");
    System.setProperty("zimbra.config", "it/data/zimbra-config/localconfig-test.xml");

    LC.zimbra_attrs_directory.setDefault("it/data/zimbra-attrs/" + ZimbraVersion.current.toString());
    ZimbraLog.toolSetupLog4j("INFO", "it/data/zimbra-config/log4j-test.properties");
  }

  private void initMailboxManager() throws ServiceException
  {
    LC.zimbra_class_mboxmanager.setDefault(MailboxManager.class.getName());
  }

  private void initIndexing()
  {
    MailboxIndex.startup();
  }

  private void initStorageManager() throws Exception
  {
    LC.zimbra_class_store.setDefault(StoreManagerSimulator.class.getName());
    StoreManager.getInstance().startup();
    mStoreRoot = ((StoreManagerSimulator)StoreManager.getInstance()).getStoreRoot();
  }

  private void initProvisioning() throws Exception
  {
    Provisioning.setInstance(new MockProvisioning());
/* $if ZimbraVersion >= 8.0.0 $*/
    ZLdapFilterFactorySimulator.setInstance();
/* $endif $*/
  }

  public void initHSQLDatabase() throws Exception
  {
    LC.zimbra_class_database.setDefault(HSQLZimbraDatabase.class.getName());
    DbPool.startup();
    HSQLZimbraDatabase.createDatabase();
  }

  public void cleanup() throws Exception
  {
    HSQLZimbraDatabase.clearDatabase();
  }

  public ZEProvisioning getProvisioning() throws Exception
  {
    return new ZEProvisioning(Provisioning.getInstance());
  }

  public ZEMailboxManager getMailboxManager() throws Exception
  {
    return new ZEMailboxManager(MailboxManager.getInstance());
  }

  public ZEStoreManager getStoreManager()
  {
    return mStoreManager;
  }
}