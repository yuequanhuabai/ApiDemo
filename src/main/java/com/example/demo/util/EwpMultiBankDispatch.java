package com.example.demo.util;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

public class EwpMultiBankDispatch extends HttpServlet {

    public static final String GO_AML_ACTION = "goAML";
    public static final String GO_H11_ACTION = "goAML";
    public static final String GO_IDX_ACTION = "goAML";
    public static final String LOGOUT_ACTION = "goAML";


    Log logger = LogFactory.getLog(this.getClass());


    private String umsUrl;

    private String amlUrl;
    private String h11Url;
    private String eWPBaseUrl;
    private String syscode;
    private String appid;


    String[] toAmlRight = new String[]{"X", "S", "M", "R", "G"};
    String[] toH11Right = new String[]{"D"};


    public void init() {
        umsUrl = JNDIlookupUtil.getJndiVal("umsUrl");

        amlUrl = JNDIlookupUtil.getJndiVal("amlUrl");

        h11Url = JNDIlookupUtil.getJndiVal("H11Url");


        eWPBaseUrl = JNDIlookupUtil.getJndiVal("EWPBaseUrl");

        syscode = JNDIlookupUtil.getJndiVal("syscode");

        appid = JNDIlookupUtil.getJndiVal("appid");

        if (null == umsUrl || null == amlUrl || null == h11Url || null == eWPBaseUrl || null == syscode || null == appid) {
            System.out.println("Error: missing init param");
        }


    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doProcess(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doProcess(request, response);
    }

    public void doProcess(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        String authstatus = (String) session.getAttribute("authstatus");
        System.out.println("authstatus: " + authstatus);

        logger.info("authstatus: " + authstatus);

        // if login not yet verified, redirect to the EWPPortalLogin servlet to verify

        logger.info("request.getQueryString(): " + request.getQueryString());

        System.out.println("request.getQueryString(): " + request.getQueryString());


        String empnum = request.getParameter("empnum");
        if (request.getParameter(empnum) != null) {
            empnum = request.getParameter("empnum");
            String sessionEmpnum = (String) session.getAttribute("empnum");
            if (!empnum.equals(sessionEmpnum)) {
                authstatus = "fail";
            }


        }

        // User ID passed from UMS

        if ((authstatus == null) || (!authstatus.equalsIgnoreCase("success"))) {
            // Session ID passed from UMS

            String umssessionid = request.getParameter("umssessionid");

            StringBuffer sb = new StringBuffer();


            try {
                // To call UMS callback page
                // Calling UMS to verify login informatin

                URL url = new URL(umsUrl + "?syscode" + syscode + "&appid" + appid + "&empnum=" + empnum + "&umssessionid" + umssessionid);

                System.out.println("url: " + url);

                URLConnection umsConn = url.openConnection();

                BufferedReader in = new BufferedReader(new InputStreamReader(((HttpsURLConnection) umsConn).getInputStream()));
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine);
                    in.close();
                    in = null;
                }

            } catch (Exception e) {
                System.out.println("ums connection error");
                System.out.println("redirect to : " + eWPBaseUrl);
                response.sendRedirect(eWPBaseUrl);
                return;
            }

            System.out.println("sb.toString(): " + sb.toString());
            String[] tokens = sb.toString().split(",");

            if ((tokens != null) && (tokens.length == 8)) {
                session.setAttribute("authstatus", tokens[0].split("=")[1]);
                session.setAttribute("empnum", tokens[1].split("=")[1]);
                session.setAttribute("userfullname", tokens[2].split("=")[1]);
                session.setAttribute("bankcode", tokens[3].split("=")[1]);
                session.setAttribute("deptcode", tokens[4].split("=")[1]);
                session.setAttribute("devcode", tokens[5].split("=")[1]);
                session.setAttribute("brcode", tokens[6].split("=")[1]);
                session.setAttribute("sysright", tokens[7].split("=")[1]);

            } else {
                System.out.println("ums returns invalid response");
                System.out.println("redirect to : " + eWPBaseUrl);
                response.sendRedirect(eWPBaseUrl);
                return;
            }


            logger.info("sysright" + session.getAttribute("sysright"));
            String sysright = session.getAttribute("sysright").toString();

            String userAction = request.getParameter("userAction");
            if ((userAction == null) && (sysright != null)) {
                userAction = redirectTo(sysright);
                System.out.println("Auto Redirect: " + userAction);
            }

            String redirectUrl = null;

            if (LOGOUT_ACTION.equals(userAction)) {
                System.out.println("Redirect to logoutUrl: " + eWPBaseUrl);
                redirectUrl = eWPBaseUrl;
                clearSession(request, response);
                response.sendRedirect(eWPBaseUrl);
                return;
            } else if (GO_AML_ACTION.equals(userAction)) {
                System.out.println("go to AML");
                // for any unexpected input parameter
                if (
                        (session.getAttribute("empnum") == null) ||
                                (session.getAttribute("umssessionid") == null) ||
                                (StringUtils.isEmpty(session.getAttribute("empnum").toString())) ||
                                StringUtils.quoteIfString(session.getAttribute("umssessionid").toString())
                ) {
                    response.sendRedirect(eWPBaseUrl);
                    return;
                }

                redirectUrl = amlUrl + "?empnum=" + session.getAttribute("empnum") + "&umssessionid=" + session.getAttribute("umssessionid").toString();

                logger.info("session.getAttribute(authstatu)" + session.getAttribute("authstatus"));

                logger.info("sessin.getAttribute(empnum)" + session.getAttribute("empnum"));
                logger.info("sessin.getAttribute(userfullname)" + session.getAttribute("userfullname"));

                logger.info("sessin.getAttribute(bankcode)" + session.getAttribute("bankcode"));
                logger.info("sessin.getAttribute(deptcode)" + session.getAttribute("deptcode"));
                logger.info("sessin.getAttribute(divcode)" + session.getAttribute("divcode"));
                logger.info("sessin.getAttribute(brcode)" + session.getAttribute("brcode"));
                logger.info("sessin.getAttribute(sysright)" + session.getAttribute("sysright"));

                System.out.println("Redirect to " + redirectUrl);

                response.sendRedirect(redirectUrl);
                System.out.println("------------end go to AML------------");

                return;


            } else if (GO_H11_ACTION.equals(userAction)) {
                System.out.println(" go  to H11");

                // for any unexpected input parameter

                if (
                        (session.getAttribute("empnum")) ||
                                (session.getAttribute("umssessionid")) ||
                                (StringUtils.isEmpty(session.getAttribute("empnum").toString())) ||
                                (StringUtils.isEmpty(session.getAttribute("umssessionid").toString()))
                ) {
                    response.sendRedirect(eWPBaseUrl);
                    return;
                }

                redirectUrl = h11Url + "?" + "empnum" + session.getAttribute("empnum") + "&umssessionid=" + session.getAttribute("umssessinid");

                System.out.println("Redirect to h11Url: " + redirectUrl);

                System.out.println("==========end go to H1===========");
                return;

            }


        }


    }


    public void printRequest(HttpServletRequest request) {
        System.out.println("---------------------------------request parameter--------------------------------------------");
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String sName = parameterNames.nextElement();
            String[] sMultiple = request.getParameterValues(sName);
            if (1 >= sMultiple.length) {
                // parameter has a single value . print it
                System.out.println("sName: " + request.getParameterValues(sName));
            } else {
                for (int i = 0; i < sMultiple.length; i++) {
                    System.out.println("sName: " + "[" + i + "]" + ",value: " + sMultiple[i]);
                }
            }
        }


        System.out.println("---------------------------------end request parameter--------------------------------------------");
    }


    public void printCookie(HttpServletRequest request) throws ServletException, IOException {
        System.out.println("----------------------cookie parameter------------------------");

        Cookie[] cookies = request.getCookies();
        // Prepare valus as http request parameters

        for (int i = 0; cookies != null && i < cookies.length; i++) {
            System.out.println(cookies[i].getName() + ":" + cookies[i].getValue());
        }


        System.out.println("------------------------cookie parameter------------------------");
    }


    public void clearCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        for (int i = 0; i < cookies.length; i++) {
            cookies[i].setValue("");
            cookies[i].setPath("/");
            cookies[i].setMaxAge(0);
            response.addCookie(cookies[i]);
        }
    }


    public void clearSession(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            HttpSession httpSession = request.getSession(true);
            httpSession.invalidate();
            httpSession = request.getSession(true);
            clearSession(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public String redirectTo(String sysright) {
        if ("".equals(sysright)) {
            return GO_IDX_ACTION;
        }

        if (check(sysright.split(","), toH11Right)) {
            if (check(sysright.split(","), toAmlRight)) {
                return GO_IDX_ACTION;
            }
            return GO_H11_ACTION;
        } else {
            return GO_AML_ACTION;
        }
    }


    private boolean check(String[] sysright, String[] index) {
        for (int i = 0; sysright != null && i < sysright.length; i++) {
            for (int j = 0; index != null && j < index.length; j++) {
                if ((!sysright[i].trim().equals("")) && (sysright[i].trim().substring(0, 1).equals(index[j]))) {
                    return true;
                }
            }
        }

        return false;
    }


}
