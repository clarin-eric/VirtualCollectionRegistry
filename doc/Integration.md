## Introduction
This document aims to provide a description of the virtual collection registry (VCR) submission endpoints, which can be used to create virtual collections from external applications.

##Submission endpoints
Send a POST request, with form urlencoded parameters, to any of the following two endpoints:
* https://<domain_name>/<context_path>/submit/extensional, to submit an extensional collection
* https://<domain_name>/<context_path>/submit/intensional, to submit an intensional1 collection

### Beta instance
A beta instance of the virtual collection registry is available at https://collections.clarin-dev.eu, with an empty context path, resulting in the following endpoint URLs:
* http://collections.clarin-dev.eu/submit/extensional 
* http://collections.clarin-dev.eu/submit/intensional 

### Production instance
The virtual collection registry production instance is available at https://collections.clarin.eu, with an empty context path, resulting in the following endpoint URLs:
* http://collections.clarin.eu/submit/extensional 
* http://collections.clarin.eu/submit/intensional

Parameters
The following parameters are supported and should be sent in form urlencoded form:

Name
Type
Required
Endpoints
name
String
Yes
Extensional + Intensional
description
String
        Yes
Extensional + Intensional
keyword
List<String>
No
Extensional + Intensional
purpose
Controlled Vocabulary
No
Extensional + Intensional
reproducibility
Controlled Vocabulary
No
Extensional + Intensional
reproducibilityNotice
String
No
Extensional + Intensional
metadataUri
List<String> or List<JSON>
Yes
Extensional
resourceUri
List<String> or List<JSON>
Yes
Extensional
queryDescription
String
Yes
Intensional
queryUri
String
Yes
Intensional
queryProfile
String
Yes
Intensional
queryValue
String
Yes
Intensional


Notes:
List<String> or List<JSON> parameter keys can be supplied multiple times, e.g. metadataUri=...&metadataUri=...
Purpose Controlled vocabulary (default value in bold): 
RESEARCH, REFERENCE, SAMPLE, FUTURE_USE
Reproducibility Controlled vocabulary (default value in bold): 
INTENDED, FLUCTUATING,UNTENDED
Both the metadataUri and resourceUri fields support JSON as its value to specify additional metadata (uri, label and description) describing the resource or metadata field.
JSON format: {“uri”: “”, “label”: “”, “description”: “”}
As a list of fields: metadataUri={“uri”: “”, “label”: “”, “description”: “”}&metadataUri={“uri”: “”, “label”: “”, “description”: “”}&...
Examples
Curl example (against a local instance with basic authentication):

curl -v \
-u user1:user1 \
-d 'name=test&metadataUri=http://www.clarin.eu/1&metadataUri=http://www.clarin.eu/2&resourceUri=http://www.clarin.eu/&&description=test-collection&keyword=&purpose=&reproducibility=' \
       http://localhost:8080/vcr/service/submit/extensional
Workflow
Figure 1 shows a sequence diagram describing the interaction between the user (user-agent), the service to be integrated with the VCR and the VCR itself. There is a clear distinction between step 1 and 2, which happen on the external service side before sending the user-agent to the VCR, either n the same or in a new browser window, for the remainder of the steps. Therefore the actual integration with the external service is focussed at step 1 and 2 in the sequence diagram. 

Step 5: Login is only required if no authenticated session is available and is not specified in detail. This workflow can be quite complicated, especially in the SAML case. For the integration of an external service this is not very relevant since this is taken care of completely on the VCR side.
Figure 1: VCR Integration workflow (source)

Integration
Integration of the VCR in an external application (portal, catalog, …) typically requires functionality in the external location to gather a set of links. This can be a search result, a cherry picking approach to select individual links or a combination. In the end this is really up to the external application.

One consideration is to send links to individual resources or send links to landing pages with a collection of links. 

After collecting a set of links in the external application, the extensional collection endpoint can be called with the following parameters:

Name
Type
Required
Endpoints
name
String
Yes
Extensional + Intensional
description
String
Yes
Extensional + Intensional
metadataUri
List<String>
Yes
Extensional
resourceUri
List<String>
Yes
Extensional

A set of keywords is optional but is prefered, purpose and reproducibility can be omitted in most cases as long as the defaults (purpose= REFERENCE and reproducibility=INTENDED) make sense.
Example integration
Figure 2 and 3 show an example integration between the CLARIN VLO and the VCR. Figure 2 shows how to create a virtual collection based on some search result (Kleve + Nijmegen). After clicking the highlighted “Create virtual collection from search result” button, the user is redirected to a confirmation page  show in figure 3 before being sent to the VCR.

Figure 2: VLO integration example



Figure 3: VLO integration confirmation example


