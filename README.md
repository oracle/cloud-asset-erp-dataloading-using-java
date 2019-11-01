# **Java Code sample for loading bulk data into Oracle ERP cloud**
This repository contains code and configuration file for a command line tool that loads bulk data in a .csv file into Oracle ERP Cloud. The code can be complied and run as a command line tool, or it can be used as a baseline for other Java enterprise applications. It can be useful as a utility to load data files on an adhoc basis quickly. A good understanding of integration with Oracle ERP Cloud is required to use or modify this code.

A configuration file allows certain aspects of the job to be customized without the need to modify code. A sample configuration file is provided.

# **Link to more information**
To learn more about the integraton pattern implemented by the code, for ERP web services used and for detailed instructions to run it as a tool, see solution documentation at 
https://docs.oracle.com/en/solutions/load-data-erp-java/index.html

# **Dependencies**

* JDK - Oracle Java SE 1.8 was used to build and test the utility. Verify compatibility with any other versions of Java.
* Oracle ERP Cloud - Access to Oracle ERP Cloud is required to run the code. 
* Nework connectivity - This code sample connects with ERP Cloud, so connectivity to internet is required.

# **Command line usage**
The tool can be run from command line on any platform that supports Java. The basic command, assuming that the complied binary is packed into ERPDataLoad.jar, is: 

```
java -jar "ERPDataLoad.jar"
```

Note: The command line utility prompts for a password for user specified in configuration file, to prevent the password being revealed. 

## **Sample Configuration file**

A sample configuration for the tool is provided below. Configuration contains two groups of settings, one for client-specific settings and another for server-specific settings. This file must be modified with settings such as URLs, credentials, data file location and name of a specific ERP job to be invoked.

```
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
# **Get help**

Visit Oracle Cloud Customer Connect Community at https://cloudcustomerconnect.oracle.com for additional resources and FAQs. 

# **License**
Copyright (c) 2015, 2019, Oracle and/or its affiliates. All rights reserved.

The code in this repository is licensed under the Universal Permissive License 1.0. See the [LICENSE](https://github.com/oracle/cloud-asset-erp-dataloading-using-java/blob/master/LICENSE) for details.
