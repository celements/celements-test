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

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * Selenium configuration class
 * @author Philipp Buser
 *
 */
public class SeleniumConfig {

  private static final String _SELENIUM_PROPERTIES_FILE = "selenium.properties";
  private static final String _SERVER_PORT_DEFAULT = "4444";
  private static final String _SERVER_HOST_DEFAULT = "localhost";
  private static final String _BROWSER_START_COMMAND_DEFAULT = "*firefox";
  private static final String _BROWSER_URL_DEFAULT = "http://localhost:8080/";
  private static final String _USERNAME_DEFAULT = "admin";
  private static final String _PASSWORD_DEFAULT = "admin";
  private static Boolean _ISFIRSTRUN = true;

  static Log _logger = LogFactory.getFactory().getInstance(SeleniumConfig.class);

  private Properties _confProps;
  private DefaultSelenium _selenium;
	private String _serverHost;
	private String _serverPort;
	private ESeleniumBrowsers _browser;
  private String _browserURL;
  private String _browserFirstRunURL;
	private String _username;
	private String _password;
	
  private Properties _getConfProps() {
    if (_confProps == null) {
      _confProps = new Properties();
      try {
        _confProps.load(this.getClass().getClassLoader().
            getResourceAsStream(_SELENIUM_PROPERTIES_FILE));
      } catch (IOException e) {
        _logger.error("failed to read selenium.properties file. ", e);
      }
    }
    return _confProps;
  }
  
  /**
   * starts and gets selenium object
   * @return selenium object
   */
  public DefaultSelenium getSelenium(){
    if (_selenium == null)
      _selenium = new DefaultSelenium(getServerHost(), 
          Integer.parseInt(getServerPort()), getBrowserStartCommand(), 
          getBrowserURL());     
    _selenium.start();    
    //do a dummy request the first time
    if (_ISFIRSTRUN){
      // open command has an intrinsic "waitForPageToLoad", thus we need to increase the
      // default timeout before calling the open command.
      _selenium.setTimeout("1800000"); // 1800 sec -> 30min
      //if xwiki.store.migration is set to 1 xwiki takes time to startup
      //moreover if some migrations are taking place this can take even more time.
      long startTS = System.currentTimeMillis();
      _selenium.open(getBrowserFirstRunURL(), "true");
      long timeUsed = System.currentTimeMillis() - startTS;
      _logger.warn("first URL request to startup using URL [" + getBrowserFirstRunURL()
          + "] and waitForPageToLoad in 30min. Seconds used [" + (timeUsed / 1000)
          + "].");
      _ISFIRSTRUN = false;
    }    
    _logger.warn("getSelenium setting timeout to 30sec. ");
    //if xwiki.store.migration is set to 1 xwiki takes time to startup
    _selenium.setTimeout("30000");
    return _selenium;
  } 
  

  /**
   * Login
   */
  public void Login(){
    String url = "loginsubmit/XWiki/XWikiLogin?j_rememberme=true&" + 
        "j_password=" + this.getPassword() + "&" + 
        "j_username=" + this.getUsername();
    
    if (_selenium == null)
      this.getSelenium();
    
    _logger.warn("Login to [" + url + "].");
    _selenium.open(url);
    _selenium.waitForPageToLoad("30000");
  }
  
  /**
   * Logout
   */
  public void Logout(){
    String url = "logout/XWiki/XWikiLogout";
    
    if (_selenium == null)
      this.getSelenium();
    
    _selenium.open(url);        
    _selenium.waitForPageToLoad("30000");
  }
  
  /**
   * gets server host
   * @return server host
   */
  public String getServerHost() {
    if (_serverHost == null) {
      _serverHost = _getConfProps().getProperty(
          "com.thoughtworks.selenium.serverHost", _SERVER_HOST_DEFAULT);
    }
    return _serverHost.trim();
  }  
  
  /**
   * gets server port
   * @return server port
   */
  public String getServerPort() {
    if (_serverPort == null) {
      _serverPort = _getConfProps().getProperty(
          "com.thoughtworks.selenium.serverPort", _SERVER_PORT_DEFAULT);
    }
    return _serverPort.trim();
  }  
  
  /**
   * gets browser startcommand
   * @return browser startcommand
   */
  public String getBrowserStartCommand() {
    return getBrowser().getBrowserCmd();
  }

  private ESeleniumBrowsers getBrowser() {
    if (_browser == null) {
      _browser = ESeleniumBrowsers.getForBrowserCmd(_getConfProps().getProperty(
          "com.thoughtworks.selenium.browserStartCommand", 
          _BROWSER_START_COMMAND_DEFAULT));
    }
    return _browser;
  }
  
  /**
   * gets browser URL
   * @return browser URL
   */
  public String getBrowserURL() {
    if (_browserURL == null) {
      _browserURL = _getConfProps().getProperty(
          "com.thoughtworks.selenium.browserURL", _BROWSER_URL_DEFAULT);
    }
    return _browserURL.trim();
  }
  
  /**
   * gets browser URL used for the first request initiating xwiki startup
   * @return browser first run URL
   */
  public String getBrowserFirstRunURL() {
    if (_browserFirstRunURL == null) {
      _browserFirstRunURL = _getConfProps().getProperty(
          "com.thoughtworks.selenium.browserFirstRunURL", getBrowserURL());
    }
    return _browserFirstRunURL.trim();
  }
  
  /**
   * gets username
   * @return username
   */
  public String getUsername() {
    if (_username == null) {
      _username = _getConfProps().getProperty(
          "com.thoughtworks.selenium.username", _USERNAME_DEFAULT);
    }
    return _username.trim();
  }
  
  /**
   * sets username
   * @param username
   */
  public void setUsername(String username) {
    this._username = username;
  }
  
  /**
   * gets password
   * @return password
   */
  public String getPassword() {
    if (_password == null) {
      _password = _getConfProps().getProperty(
          "com.thoughtworks.selenium.password", _PASSWORD_DEFAULT);
    }
    return _password.trim();
  }
  
  /**
   * sets password
   * @param password
   */
  public void setPassword(String password) {
    this._password = password;
  }

  /**
   * is firefox browser?
   * @return
   */
  public boolean isFirefox() {
    return ESeleniumBrowsers.Firefox == getBrowser();
  }

  /**
   * is internet explorer browser?
   * @return
   */
  public boolean isIE() {
    return ESeleniumBrowsers.InternetExplorer == getBrowser();
  }

  /**
   * stops selenium object
   */
  public void stopSelenium()
  {
    _selenium.stop();
  }



     
}
