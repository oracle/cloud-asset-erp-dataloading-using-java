**Asset Scope**

- Java code sample
- Monitors a known directory on the compute Server, e.g. a SFTP location
- When a new file is detected the file is read and uploaded to ERP cloud
- Log files are updated 
- Processed files are preserved, e.g. in a /processed directory
------

# **Link to more information about this solution.**
https://docs.oracle.com/en/solutions/load-data-erp-java/index.html

# **Command line tool to bulk load data into Oracle ERP cloud.**

ERP cloud allows bulk import of data through web services. The web service call could be invoked from any platform that supports SOAP clients, including a standalone Java program. A sample stand alone Java utility is provided by A-Team for customers to use as a baseline and tailor to suit their needs.

**Note: **The command line utility prompts for a password for user specified in configuration file.

**Sample Command line format.**

java -jar "ERPDataLoad.jar"

Dependencies:


JDK - Oracle Java SE 1.8 was used to build and test the utility. Verify compatibility with any other versions of Java.

Configuration file - A sample configuration file is provided with the utility. This file must be modified with environment-specific settings such as URLs and credentials and information such as data file’s location and a specific ERP job to be invoked.

## **Configuration file.**

```
#A sample configuration is provided below. Configuration contains two groups of settings, one for client-specific settings and another for server-specific settings.

#Job paramaters.

#Directory where the processed files are to be stored.
Archivepath={insert directory path where processed files should be archived} 

#ERP cloud URL
ERPURL={https://hostname:port/publicFinancialCommonErpIntegration/ErpIntegrationService}

#ERP Cloud username. The job will prompt for a password.
Username={insert username}

#A lock file to ensure that the job is singleton
Lockfile={Specify a file name in path writable to JDK}

#Input file for SOAP request  to ERP intergation service. These are specific to each job.

Datafile={full path to the .zip file}

#Zip is the only supported format
DatafileContentType=zip

#ERP cloud job name. Set this value depending on the type of job triggered.
JobName=oracle/apps/ess/financials/generalLedger/programs/common,JournalImportLauncher

#A URL that ERP cloud can call upon completion of the job.
CallbackURL={http://hostname:port/uri}
```