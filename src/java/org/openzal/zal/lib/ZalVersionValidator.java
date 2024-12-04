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

package org.openzal.zal.lib;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class ZalVersionValidator
{
  private final static String ATTR_VERSION            = "Specification-Version";
  private final static String ATTR_ZAL_IMPLEMENTATION = "Implementation-Version";

  public Version validate(JarAccessor jar, ZimbraVersion zimbraVersion) throws IOException, NoSuchAlgorithmException
  {
    validateVersion(jar, zimbraVersion);

    return new Version(jar.getAttributeInManifest(ATTR_VERSION));
  }

  private void validateVersion(JarAccessor jar, ZimbraVersion zalVersion) throws IOException
  {
    Version zalImplementationVersion = new Version(jar.getAttributeInManifest(ATTR_ZAL_IMPLEMENTATION));
    if (!zalImplementationVersion.is(zalVersion.getMajor(),zalVersion.getMinor()))
    {
      throw new RuntimeException(
        "Zimbra version mismatch - ZAL " + jar.getPath() + " built for Zimbra: " + zalImplementationVersion.toString()
      );
    }
  }
}
