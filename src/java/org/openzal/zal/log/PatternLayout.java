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

package org.openzal.zal.log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;

@Plugin(name = "PatternLayout", category = "Converter")
@ConverterKeys({ "z" })
public class PatternLayout extends LogEventPatternConverter
{

  protected PatternLayout(String name, String style) {
      super(name, style);
  }

  public static PatternLayout newInstance(String[] options)
  {
      return new PatternLayout("z", Thread.currentThread().getName());
  }

  @Override
  public void format(LogEvent event, StringBuilder toAppendTo) {
      toAppendTo.append(com.zimbra.common.util.ZimbraLog.getContextString() == null ? "" : com.zimbra.common.util.ZimbraLog.getContextString());
  }

}
