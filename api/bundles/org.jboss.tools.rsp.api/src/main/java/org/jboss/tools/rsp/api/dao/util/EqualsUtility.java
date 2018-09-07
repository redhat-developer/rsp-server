/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.tools.rsp.api.dao.util;

/**
 * Utility class for simple implementation of equals methods
 * 
 * @author jrichter
 */
public class EqualsUtility {
    
  public static boolean areEqual(Object a, Object b){
    return a == null ? b == null : a.equals(b);
  }
  
}
