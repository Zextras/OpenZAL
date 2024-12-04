/*
 * ZAL - The abstraction layer for Zimbra.
 * Copyright (C) 2016 ZeXtras S.r.l.
 *
 * This file is part of ZAL.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, version 2 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZAL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.openzal.zal;

import javax.annotation.Nonnull;
import org.openzal.zal.lib.Version;
import org.openzal.zal.lib.ZimbraVersion;

public class ZalVersion
{
  public static final    Version current = new Version(ZalBuildInfo.VERSION);
  @Nonnull public static Version target;

  static
  {
    /* $if ZimbraX == 0 $ */
    String implementationVersion = ZalVersion.class.getPackage().getImplementationVersion();
    if (implementationVersion != null && !implementationVersion.isEmpty())
    {
      target = new Version(implementationVersion);
    }
    /* $else $
    target = new Version(8,9,0);
    /* $endif $ */
  }

  public static void checkCompatibility()
  {
    /* $if ZimbraX == 0 $ */
    if (!ZimbraVersion.current.is(ZalVersion.target.getMajor(), ZalVersion.target.getMinor()))
    {
      throw new RuntimeException("Zimbra version mismatch - ZAL built for Zimbra: " + ZalVersion.target.toString());
    }
    /* $endif $ */
  }

  public static boolean isZimbraX()
  {
    /* $if ZimbraX == 1 $
    return true;
    /* $else $ */
    return false;
    /* $endif $ */
  }

  public static void main(String args[])
  {
    System.out.println("zal_version: " + current.toString());
    System.out.println("zal_commit: " + ZalBuildInfo.COMMIT);
    /* $if ZimbraX == 1 $
    System.out.println("target_zimbra_version: Zimbra X");
    /* $else $ */
    System.out.println("target_zimbra_version: " + target.toString());
    /* $endif $ */

    System.exit(0);
  }
}
