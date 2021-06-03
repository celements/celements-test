/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.celements.common.test;

public enum ESeleniumBrowsers {
  
  Firefox("*firefox"),
  InternetExplorer("*iexplore");
  
  private String _browserCmd;

  private ESeleniumBrowsers(String browserCmd) {
    this._browserCmd = browserCmd;
  }
  
  public String getBrowserCmd() {
    return _browserCmd;
  }
  
  public static ESeleniumBrowsers getForBrowserCmd(String browserCmd) {
    if (Firefox.getBrowserCmd().equals(browserCmd)) {
      return Firefox;
    } else if (InternetExplorer.getBrowserCmd().equals(browserCmd)) {
     return InternetExplorer; 
    } else {
      throw new IllegalArgumentException("Unknown BrowserCmd " + browserCmd);
    }
  }

}
