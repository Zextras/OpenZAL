package com.zimbra.cs.mailbox;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.ProvisioningImpProxy;
import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.HSQLZimbraDatabase;
import com.zimbra.cs.ephemeral.EphemeralStore;
import com.zimbra.cs.ldap.ZLdapFilterFactorySimulator;
import com.zimbra.cs.util.JMSession;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.junit.rules.ExternalResource;
import org.openzal.zal.MailboxManagerImp;
import org.openzal.zal.Provisioning;
import org.openzal.zal.extension.Zimbra;

// for testing purpose only
public class ZimbraSimulator extends ExternalResource
{

  static Logger logger = Logger.getLogger(ZimbraSimulator.class);

  private MailboxManager mMailboxManager;

  protected File mTmpDir;

  private List<ZimbraSimulatorExtension> extensions = new ArrayList<>();

  public ZimbraSimulator(ZimbraSimulatorExtension ...extensions)
  {
    initHsql();
    init();
    this.extensions = Arrays.asList(extensions);
  }

  public ZimbraSimulator(Zimbra zimbra, ZimbraSimulatorExtension ...extensions)
  {
    initHsql();
    init(zimbra);
    this.extensions = Arrays.asList(extensions);
  }

  private static void initHsql() {
    try
    {
      Class.forName("org.hsqldb.jdbcDriver");
    }
    catch (Exception e)
    {
      logger.error("Error loading DB Driver: ", e);
      throw new RuntimeException(e);
    }
  }


  /*
    junit @Rule implementation
   */
  protected void before() throws Throwable
  {
  }

  protected void after()
  {
    try
    {
      cleanup();
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  Zimbra mZimbra;

  private void init()
  {
    init(null);
  }

  private void init(Zimbra zimbra)
  {
    try
    {
      //Locale.setDefault(Locale.US);

      initProperties();
      initIndexing();
      if (zimbra == null)
      {
        initProvisioning();
      }
      initHSQLDatabase();
      mZimbra = new Zimbra(zimbra);
      initMailboxManager(zimbra);

      extensions.forEach( ext -> ext.setup(zimbra));
      /* $if ZimbraVersion < 8.0.0 $
      Volume.reloadVolumes();
       $endif$ */

      try
      {
        ScheduledTaskManager.getTask("", "", -1);
      }
      catch (Throwable ex)
      {
        ScheduledTaskManager.startup();
      }
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
    catch (Throwable throwable)
    {
      throw new RuntimeException(throwable);
    }
  }

  private void initProperties()
          throws Throwable
  {
    System.setProperty("mail.mime.decodetext.strict",       "false");
    System.setProperty("mail.mime.encodefilename",          "true");
    System.setProperty("mail.mime.charset",                 "utf-8");
    System.setProperty("mail.mime.base64.ignoreerrors",     "true");
    System.setProperty("mail.mime.ignoremultipartencoding", "false");
    System.setProperty("mail.mime.multipart.allowempty",    "true");

    System.setProperty("zimbra.native.required", "false");
    System.setProperty("log4j.configuration", "it/data/carbonio/config/log4j-test.properties");
    System.setProperty("zimbra.config", "it/data/carbonio/config/localconfig-test.xml");

    LC.zimbra_attrs_directory.setDefault("it/data/carbonio/attrs/");
    LC.zimbra_rights_directory.setDefault("it/data/carbonio/rights/");
    ZimbraLog.toolSetupLog4j("INFO", "it/data/carbonio/config/log4j-test.properties");

    mTmpDir = createTmpDir("junit_tmp_");
    LC.calendar_cache_directory.setDefault(mTmpDir.getAbsolutePath());
  }

  public static File createTmpDir(String name) throws Throwable
  {
    File createdFolder = File.createTempFile(name, "", null);
    createdFolder.delete();
    if( createdFolder.exists() )
    {
      throw new RuntimeException("Not empty temporary directory: "+createdFolder.getAbsolutePath());
    }
    createdFolder.mkdir();
    if( !createdFolder.exists() )
    {
      throw new RuntimeException("Unable to create temporary directory: "+createdFolder.getAbsolutePath());
    }
    return createdFolder;
  }

  private static void recursiveDelete(File file)
  {
    File[] files = file.listFiles();
    if (files != null)
    {
      for (File each : files)
      {
        recursiveDelete(each);
      }
    }
    if (file.exists() && !file.delete())
    {
      throw new RuntimeException("Unable to delete file " + file.getAbsolutePath());
    }
  }

  private void initMailboxManager(Zimbra zimbra) throws Exception
  {
    mMailboxManager = (MailboxManager) Class.forName(LC.zimbra_class_mboxmanager.value()).newInstance();
    if (zimbra == null)
    {
      com.zimbra.cs.mailbox.MailboxManager.setInstance(mMailboxManager);
    }
  }

  private void initIndexing()
  {
    MailboxIndex.startup();
  }

//  private void initStorageManager(Zimbra zimbra) throws Exception
//  {
//    //LC.zimbra_class_store.setDefault(StoreManagerSimulator.class.getName());
//    //com.zimbra.cs.store.StoreManager.getInstance().startup();
//    mVolumeManager = new VolumeManager();
//    mStoreSimulator = new StoreManagerSimulator();
//    mStoreSimulator.startup();
//    mStoreRoot = mStoreSimulator.getStoreRoot();
//    FileBlobStoreWrap storeManagerSimulator = new FileBlobStoreSimulatorWrap(mStoreSimulator);
//    mStoreManager = new StoreManagerImpl(
//            storeManagerSimulator,
//            mVolumeManager
//    );
//
//    if (zimbra == null)
//    {
//      mZimbra.overrideZimbraStoreManager(mStoreManager);
//    }
//  }

  private void initProvisioning() throws Exception
  {
    com.zimbra.cs.account.Provisioning.setInstance(new MockProvisioning());
    ZLdapFilterFactorySimulator.setInstance();
    /* $if ZimbraVersion >= 8.7.6$ */
    EphemeralStore.registerFactory("in-memory", "com.zimbra.cs.ephemeral.InMemoryEphemeralStore$Factory");
    /* $endif$ */
  }

  public void initHSQLDatabase() throws Exception
  {
    LC.zimbra_class_database.setDefault(HSQLZimbraDatabase.class.getName());
    DbPool.startup();
    HSQLZimbraDatabase.createDatabase();
    useMVCC();
  }

  public void cleanup() throws Exception
  {
    JMSession.resetSmtpHosts(); // clean bad hosts that fails during tests
    HSQLZimbraDatabase.clearDatabase();
    extensions.forEach( ext -> {
      try {
        ext.cleanup();
      } catch (Exception e) {
        logger.warn("Ignoring exception on close ", e);
      }
    });
    mZimbra.restoreZimbraStoreManager();
    recursiveDelete(mTmpDir);
    //sVolumeManagerInstance.set(null, sVolumeManagerBuilder.newInstance());
    //((StoreManagerSimulator) com.zimbra.cs.store.StoreManager.getInstance()).shutdown();
  }

  public Provisioning getProvisioning()
  {
    return new ProvisioningImpProxy(com.zimbra.cs.account.Provisioning.getInstance());
  }

  public MockProvisioning getMockProvisioning()
  {
    return (MockProvisioning) com.zimbra.cs.account.Provisioning.getInstance();
  }

  public org.openzal.zal.MailboxManager getMailboxManager()
  {
    return new MailboxManagerImp(mMailboxManager);
  }

  public Zimbra getZimbra()
  {
    return mZimbra;
  }

  private void useMVCC() throws Exception
  {
    HSQLZimbraDatabase.useMVCC();
  }
}
