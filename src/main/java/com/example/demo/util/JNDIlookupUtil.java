package com.example.demo.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JNDIlookupUtil {
//    public static final String tomcatContext ="java:comp/env/jndi/";

    public static final String tomcatContext = "java:comp/env/";

//    public static final String wasContext = tomcatContext + "wasContext";

    public static final String wasContext = "";

    public static String getJndiVal(String jndiName) {
        Context initCtx = null;

        try {
            initCtx = new InitialContext();
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }


        try {
            Context envCtx = (Context) initCtx.lookup(wasContext);
            return ((String) envCtx.lookup(jndiName)).trim();
        } catch (NamingException e) {
            Context envCtx = null;
            try {
                envCtx = (Context) initCtx.lookup(tomcatContext);
                return  ((String) envCtx.lookup(jndiName)).trim();
            } catch (NamingException ex) {
                throw new RuntimeException(ex);
            }

        }



    }


}
