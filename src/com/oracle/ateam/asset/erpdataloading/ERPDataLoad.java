/**
** Erp dataloading tool
**
** Copyright (c) 2019 Oracle, Inc.  All rights reserved.
** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl.
 */

package com.oracle.ateam.asset.erpdataloading;

//Object factories can not be imported since there are many of them with the same name.
//Refer to them by fully qualifed names
import com.oracle.xmlns.apps.financials.commonmodules.shared.model.erpintegrationservice.DocumentDetails;
import com.oracle.xmlns.apps.financials.commonmodules.shared.model.erpintegrationservice.EssJob;
import com.oracle.xmlns.apps.financials.commonmodules.shared.model.erpintegrationservice.ObjectFactory;
import com.oracle.xmlns.apps.financials.commonmodules.shared.model.erpintegrationservice.types.LoadAndImportData;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;
import org.w3c.dom.Document;
import java.util.Scanner;

/**
 * *
 * This is a command line utility to perform imports in to ERP cloud
 *
 */
public class ERPDataLoad {

    private static String strERPURL;
    private static String strUsername;
    private static String strPd;
    private static String strLockfile;
    private static String strDatafile;
    private static String strArchivepath;
    private static String strDatafileContentType;
    private static String strJobName;
    private static String strParameterList;
    private static String strInterfaceDetails;
    private static String strNotificationCode;
    private static String strCallbackURL;


    /**
     * *
     * Main method orchestrates the overall flow
     *
     */
    public static void main(String[] args) {

        //Look for the property file name in arg[0]. Ignore all other args.
        if (args.length == 0) {
            System.out.println("Property file not provided in input. Quitting..");
            System.exit(1);
        }
        System.out.println("Starting with configuration:" + args[0]);

        try {

            //Load settings from configuration file
            getProperties(args[0]);

            //Get password the test account.
            System.out.println("Enter password for " + strUsername + " and hit Enter to proceed:");
            Scanner in = new Scanner(System.in);
            strPd = in.nextLine();

            System.out.println(args[0] + "|" + strERPURL + "|" + strUsername + "|" + strLockfile + "|" + strDatafile + "|" + strArchivepath);

            //execute bulk import and archive data file if the job succeeds
            importBulk(strDatafile, strERPURL, strUsername, strPd);

            //Archive data file to archive folder
            archiveDataFile();

        } catch (Exception ex) {
            System.out.println("Error occured during processing. " + ex.getMessage());
        }

    }

    /**
     * *
     * Archive data file after upload, in order to prevent duplicate loads
     */
    private static void archiveDataFile() throws Exception {

        try {
            File sourceFile = new File(strDatafile);
            String strTargetfileName = strArchivepath + File.separator + sourceFile.getName() + "." + (new SimpleDateFormat("dd-MM-yyyy-mm-ss").format(new Date()));
            if (sourceFile.renameTo(new File(strTargetfileName))) {
                System.out.println("Data file archived as: " + strTargetfileName);
            }
        } catch (Exception ex) {
            System.out.println("Error archiving file " + strDatafile);
            throw new Exception("Error archiving file");
        }

    }

    /**
     * *
     * Load properties
     */
    private static void getProperties(String strPropFile) throws Exception {

        if (strPropFile == null) {
            throw new Exception("Invalid property file.");
        }
        if (strPropFile.isEmpty()) {
            throw new Exception("Invalid property file.");
        }

        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream(strPropFile);
            prop.load(input);
            // set the properties value
            strERPURL = prop.getProperty("ERPURL", "url");
            strUsername = prop.getProperty("Username", "tt");
            strLockfile = prop.getProperty("Lockfile", "lockfile");
            strDatafile = prop.getProperty("Datafile", "password");
            strArchivepath = prop.getProperty("Archivepath", ".");

            strDatafileContentType = prop.getProperty("DatafileContentType", "zip");
            strJobName = prop.getProperty("JobName", "");
            //It is possible to externalize the parameters below
            strParameterList = "300000046975980,Balance Transfer,300000046975971,1234567890,N,N,N"; //prop.getProperty("ParameterList", "");
            strInterfaceDetails = "15"; //prop.getProperty("InterfaceDetails", "");
            strNotificationCode = "50"; //prop.getProperty("NotificationCode", "");
            strCallbackURL = prop.getProperty("CallbackURL", "");

        } catch (IOException io) {
            System.out.println("Error reading properties file:" + io.getMessage());
            throw new Exception("Unable to read property file at: " + strPropFile);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    System.out.println("Error closing properties file:" + e.getMessage());
                }
            }

        }
    }

    /**
     * *
     * This method executes the bulk import call to ERP cloud
     */
    public static void importBulk(String strDatafile, String strERPURL, String strUsername, String strPd) throws Exception {

        if (strDatafile == null || strERPURL == null || strUsername == null || strPd == null) {
            throw new Exception("Invalid input to importBulk.");
        }

        if (strDatafile.isEmpty() || strERPURL.isEmpty() || strUsername.isEmpty() || strPd.isEmpty()) {
            throw new Exception("Invalid input to importBulk.");
        }

        System.out.println("DataFile:" + strDatafile);
        try {
            ObjectFactory modelFactory = new ObjectFactory();

            DocumentDetails objDocDetail = new DocumentDetails();
            byte[] arrBytes = Base64.getEncoder().encode(fileToBytes(strDatafile));
            objDocDetail.setContent(arrBytes);
            System.out.println("Bytes:" + new String(arrBytes));
            objDocDetail.getContentType();
            objDocDetail.setFileName(strDatafile);
            objDocDetail.setContentType(modelFactory.createDocumentDetailContentType(strDatafileContentType));
            ArrayList jobDetails = new ArrayList();
            EssJob objEssJob = new EssJob();
            objEssJob.setJobName(modelFactory.createEssJobJobName(strJobName));
            objEssJob.setParameterList(modelFactory.createEssJobParameterList(strParameterList));
            jobDetails.add(objEssJob);

            com.oracle.xmlns.apps.financials.commonmodules.shared.model.erpintegrationservice.types.ObjectFactory objObjectFactory
                    = new com.oracle.xmlns.apps.financials.commonmodules.shared.model.erpintegrationservice.types.ObjectFactory();

            LoadAndImportData objLoadandImportData = new LoadAndImportData();
            objLoadandImportData.setDocument(objDocDetail);
            objLoadandImportData.getJobList().add(objEssJob);
            objLoadandImportData.setInterfaceDetails(objObjectFactory.createLoadAndImportDataAsyncInterfaceDetails(strInterfaceDetails));
            objLoadandImportData.setCallbackURL(objObjectFactory.createLoadAndImportDataAsyncCallbackURL(strCallbackURL));
            objLoadandImportData.setNotificationCode(objObjectFactory.createLoadAndImportDataAsyncNotificationCode(strNotificationCode));

            JAXBContext jaxbContext = JAXBContext.newInstance(LoadAndImportData.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            jaxbMarshaller.marshal(objLoadandImportData, document);

            SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
            soapMessage.getSOAPBody().addDocument(document);

            //Set authorization header
            String strAuth = strUsername + ":" + strPd;
            soapMessage.getMimeHeaders().addHeader("Authorization", "Basic " + new String(Base64.getEncoder().encode(strAuth.getBytes())));

            SOAPMessage soapRespMessage = processSoapRequest(strERPURL, soapMessage);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            soapMessage.writeTo(stream);
            String message = new String(stream.toByteArray(), "utf-8");
            System.out.println("Request:" + message);
            System.out.println("Status:" + soapRespMessage.getSOAPBody().getElementsByTagNameNS("http://xmlns.oracle.com/apps/financials/commonModules/shared/model/erpIntegrationService/types/", "result").item(0).getTextContent());
            System.out.println("Bulk load completed successfully.");
        } catch (Exception ex) {
            System.out.println("Bulk import failed. Error:" + ex.getMessage());
            throw new Exception("Bulk import failed. Check logs for more info.");
        }
    }

    /**
     * *
     * Method to convert data file to bytes
     */
    public static byte[] fileToBytes(String fileName) throws Exception {

        if (fileName == null) {
            throw new Exception("Invalid input data file.");
        }
        if (fileName.isEmpty()) {
            throw new Exception("Invalid input data file.");
        }

        FileInputStream fileInputStream = null;

        File file = new File(fileName);

        byte[] bFile = new byte[(int) file.length()];

        fileInputStream = new FileInputStream(file);
        try {
            //convert file into array of bytes
            int cntBytes = fileInputStream.read(bFile);
            if (cntBytes <= 0) {
                throw new Exception("Data file is empty");
            }
            return bFile;
        } catch (Exception ex) {
            System.out.println("Error converting file content to bytes:" + ex.getMessage());
            throw new Exception("Error converting file content to bytes. Check log for more info.");
        } finally {
            fileInputStream.close();
        }
    }

    /**
     * *
     * Process SOAP request to ERP cloud.
     */
    public static SOAPMessage processSoapRequest(String serviceURL, SOAPMessage soapRequest) throws Exception {

        if (serviceURL == null || soapRequest == null) {
            throw new Exception("Invalid input to processSoapRequest.");
        }

        if (serviceURL.isEmpty()) {
            throw new Exception("Invalid service URL input to processSoapRequest.");
        }

        HttpsURLConnection objHTTPSConn = null;
        SOAPConnection soapConnection = null;
        boolean blHTTPS = serviceURL.toLowerCase().startsWith("https");

        try {
            //Is this a HTTPS endpoint?
            //HTTPS processing
            if (blHTTPS) {
                //This code trust any security certificate from server. Depending on security needs, this 
                //code needs to be modified for your organization.
                SSLContext sslContext = SSLContext.getInstance("SSL");
                TrustManager[] objTrust
                        = new TrustManager[]{new TrustManager()};
                sslContext.init(null, objTrust, new java.security.SecureRandom());
                HttpsURLConnection
                        .setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                URL url = new URL(serviceURL);
                objHTTPSConn = (HttpsURLConnection) url.openConnection();
                objHTTPSConn.setHostnameVerifier(new CHostnameVerifier());
                objHTTPSConn.connect();
            }
            soapConnection = SOAPConnectionFactory.newInstance().createConnection();
            //Submit SOAP request
            return soapConnection.call(soapRequest, serviceURL);
        } catch (Exception ex) {
            System.out.println("Error processing SOAP request:" + ex.getMessage());
            throw new Exception("Error processing SOAP request.");
        } finally {
            if (soapConnection != null) {
                soapConnection.close();
            }
            if (blHTTPS && objHTTPSConn != null)objHTTPSConn.disconnect();
        }
    }

    /**
     * *
     * Modify this method to alter hostname verification behavior in SSL/TLS
     * later.
     */
    private static class CHostnameVerifier implements HostnameVerifier {

        public boolean verify(String hostname, SSLSession session) {

            try {
                if (hostname.isEmpty() || session == null) {
                    throw new Exception("Invalid input to verify.");
                }
                if (hostname.equalsIgnoreCase(session.getPeerHost())) 
                    return true;
            } catch (Exception ex) {
                System.out.println("Error verifying hostname." + ex.getMessage());
            }
            return false;
        }
    }

    /**
     * *
     * Defines trust manager for TLS connection
     *
     */
    private static class TrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] certs, String authType)  {
            /*
            *Default behavior. Modify this method to alter.
             */
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType)  {
            /*
            *Default behavior. Modify this method to alter.
             */
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

}
