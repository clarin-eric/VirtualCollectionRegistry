## Introduction
This document aims to provide a description of the virtual collection registry (VCR) submission endpoints, which can be used to create virtual collections from external applications.

_Note_: The original version of this document was managed via [google documents](https://docs.google.com/document/d/1HYNDhtNIamcNP3kQg4IgLhcOk7uH60Ij5PJVksJLADM/). This markdown version
replaces the google drive version.

An easy to use, javascript based, widget has been created to make the process of integrating with the VCR submission endpoints as easy as possible.
Code and documentation can be found here https://github.com/clarin-eric/js-vcr-integration/.

## Submission endpoints
Send a POST request, with form urlencoded parameters, to any of the following two endpoints:
* `https://<domain_name>/<context_path>/submit/extensional`, to submit an extensional collection
* `https://<domain_name>/<context_path>/submit/intensional`, to submit an intensional1 collection

### Alpha / development instance
An alpha/development instance of the virtual collection registry is available at https://alpha-collections.clarin.eu, 
with an empty context path, resulting in the following endpoint URLs:
* `http://alpha-collections.clarin.eu/submit/extensional` 
* `http://alpha-collections.clarin.eu/submit/intensional`

This instance is used for testing development releases and should be considered unstable.
Any collections created in this instance can be removed without notice.

### Beta instance
A beta instance of the virtual collection registry is available at https://beta-collections.clarin.eu, with an empty 
context path, resulting in the following endpoint URLs:
* `http://beta-collections.clarin.eu/submit/extensional` 
* `http://beta-collections.clarin.eu/submit/intensional` 

This instance is used for testing staging releases before they are deployed in production. This instance can be considered 
relatively stable and should be used to develop and test against for third party integrations.
Any collections in this instance can be removed without notice, however this should not happen regularly.

### Production instance
The virtual collection registry production instance is available at https://collections.clarin.eu, with an empty context 
path, resulting in the following endpoint URLs:
* `http://collections.clarin.eu/submit/extensional` 
* `http://collections.clarin.eu/submit/intensional`

This is the stable production instance and should not be used for testing.

### Parameters
The following parameters are supported on both endpoints and should be sent in form urlencoded form:

| Name | Type | Required | Endpoints |
|------|------|----------|-----------|
| name  | String | Recommended | Extensional + Intensional |
| description | String | Recommended |Extensional + Intensional |
| original_query | String | Optional |Extensional |
| keyword | List&lt;String&gt; | Optional | Extensional + Intensional |
| purpose | Controlled Vocabulary | Optional | Extensional + Intensional |
| reproducibility | Controlled Vocabulary | Optional | Extensional + Intensional |
| reproducibilityNotice | String | Optional | Extensional + Intensional |

The following parameters are supported in the extensional endpoint and should be sent in form urlencoded form:

| Name | Type | Required | Endpoints |
|------|------|----------|-----------|
| metadataUri | List&lt;String&gt; or List&lt;JSON&gt; | Yes | Extensional |
| resourceUri | List&lt;String&gt; or List&lt;JSON&gt; | Yes | Extensional |

The following parameters are supported in the intensional endpoint and should be sent in form urlencoded form:

| Name | Type | Required | Endpoints |
|------|------|----------|-----------|
| queryDescription | String | Yes | Intensional |
| queryUri | String | Yes | Intensional |
| queryProfile | String | Yes | Intensional |
| queryValue | String | Yes | Intensional |

Notes:

* Lists: `List<String>` or `List<JSON>`, must be specified by repeating the parameter key multiple times, e.g. `metadataUri=...&metadataUri=...`
  * For a `List<String>` the `key=value` pair can be provided one or more times, where `value` must be a single string value: 
  `key=value_1&key=value_2&...`
  * For a `List<JSON>` the `key=value` pair can be provided one or more times, where `value` must be a single JSON object :
  `key={"prop1": "val_1", ...}&key={"prop2": "val_2", ...}&...`
* Purpose Controlled vocabulary (default value marked with *): 
  * `RESEARCH, REFERENCE*, SAMPLE, FUTURE_USE`
* Reproducibility Controlled vocabulary (default value in bold): 
  * `INTENDED*, FLUCTUATING, UNTENDED`
* Both the metadataUri and resourceUri fields support JSON as its value to specify additional metadata (uri, label and description) describing the resource or metadata field.
  * JSON format: `{"uri": "", "label": "", "description": ""}`
  * As a list of fields: `metadataUri={"uri": "", "label": "", "description": ""}&metadataUri={"uri": "", "label": "", "description": ""}&...`
* For extenstional collections the `original_query` field can be used to link the submitted resources to the query used to generate this collection.
* Referrer information is used to keep track of the origin of submissions. If you want this to be tracked for resources 
submitted via your repository make sure to properly send the `Referer` header. Curl header example: ` -H 'Referer: https://your.host.name/'`

### Examples

The workflow assumes an interactive, javascript enabled, user-agent. Curl can be used to demonstrate some basic requests.
Since the response HTML/Javascript is not properly rendered, this example does not really show the workflow as it is 
intended.

Curl example (against a local instance (http://localhost:8080/vcr) with basic authentication (user1:user1)):

```
curl -v \
     -u user1:user1 \
     -H 'Content-Type: application/x-www-form-urlencoded' \
     -H 'Referer: https://your.host.name/' \
     -d 'name=test&metadataUri=http://www.clarin.eu/1&metadataUri=http://www.clarin.eu/2&resourceUri=http://www.clarin.eu/&&description=test-collection&keyword=&purpose=&reproducibility=' \
     http://localhost:8080/vcr/submit/extensional
```

Curl example against beta instance with shibboleth authentication:

Request:
```
curl -v \
     -H 'Content-Type: application/x-www-form-urlencoded' \
     -H 'Referer: https://your.host.name/' \
     -d 'name=test&metadataUri=http://www.clarin.eu/1&metadataUri=http://www.clarin.eu/2&resourceUri=http://www.clarin.eu/&&description=test-collection&keyword=&purpose=&reproducibility=' \
     https://beta-collections.clarin.eu/submit/extensional
```

Response:
```
< HTTP/1.1 302
< Server: nginx
< ...
< Location: https://beta-collections.clarin.eu/submit/extensional;jsessionid=<some session id>?0

> GET /submit/extensional;jsessionid=<some session id>?0 HTTP/1.1
> Host: beta-collections.clarin.eu
> User-Agent: curl/7.54.0
> Accept: */*
> Content-Type: application/x-www-form-urlencoded

< HTTP/1.1 200
< Server: nginx
< ...
< Content-Type: text/html;charset=utf-8

<html body>
```

## Workflow

Figure 1 shows a sequence diagram describing the interaction between the user (user-agent), the service to be integrated with the VCR and the VCR itself. There is a clear distinction between step 1 and 2, which happen on the external service side before sending the user-agent to the VCR, either n the same or in a new browser window, for the remainder of the steps. Therefore the actual integration with the external service is focussed at step 1 and 2 in the sequence diagram. 

![Figure 1: sequence diagram!](./vcr_integration_workflow_sequence_diagram.png "Figure 1: sequence diagram")

Diagram [source](https://www.websequencediagrams.com/?lz=dGl0bGUgVkNSIEludGVncmF0aW9uIHdvcmtmbG93CgpVc2VyLWFnZW50LT5TZXJ2aWNlOiAxLiBzZWxlY3QgcmVzb3VyY2VzIGFuZCBjcmVhdGUgY29sbGVjdGlvbgoALActPgA-CjogMi4gSFRUUCAyMDAgT0sAVg1WQ1I6IDMuIFBPU1Qgd2l0aCBwYXJhbWV0ZXJzIHRvIFZDUgpWQ1IAIgdQcm9jZXNzIDQuIGlucHV0AIEABXN0b3JlIGluIHNlc3Npb24AJQs1LiBMb2dpAAcMNi4gUmVkaXJlY3QgdG8AgTALAIFHBmlvbiBwYWdlIABwBgCBOgw3AIE7C29rIG9yAIFPBmVycm9yICg0eHggb3IgNXh4KQ&s=default)

Note:
* Step 5: Login is only required if no authenticated session is available and is not specified in detail. This workflow can be quite complicated, especially in the SAML case. For the integration of an external service this is not very relevant since this is taken care of completely on the VCR side.
* Since authentication is implemented via SAML SSO, all communication should happen via the user-agent, including the POST from the service to the VCR endpoint (step 3). The easiest way to achieve this is via form on the external application side. See integration section for an example.

## Integration

Integration of the VCR in an external application (portal, catalog, …) typically creates an extensional collection. This 
requires functionality in the external application to gather a set of links. This can be a search result, a cherry picking 
approach to select individual links or a combination. In the end this is really up to the external application.

One consideration is to send links to individual resources or send links to landing pages with a collection of links. 

After collecting a set of links in the external application, the extensional collection endpoint can be called with the 
following minimal set of required parameters:

| Name | Type | Required |
|------|------|----------|
| name | String | Yes |
| description | String | Yes |
| metadataUri | List&lt;String&gt; | Yes |
| resourceUri | List&lt;String&gt; | Yes |

Notes:
* A set of keywords is optional but is prefered.
* Purpose and reproducibility can be omitted in most cases as long as the defaults (`purpose= REFERENCE` and `reproducibility=INTENDED`) make sense.

Both the production and beta VCR instances are running with SAML based (shibboleth) authentication. This relies heavily
on browser driven workflows, thus it is advisable to perform the submission POST request from the user browser. This ensures
the authentication workflow functions smoothly. One way to achieve this, is by adding all POST data in a form. A simple example
is shown in the next section.

### Form example

This is an example of how one could implement a form, served by an external application, to submit a virtual collection to the VCR:
```
<form id="virtualCollectionForm" method="post" enctype="application/x-www-form-urlencoded" name="vcrForm" action="https://collections.clarin.eu/submit/extensional"> 
    <input id="collectionName" type="text" name="name" value="Your collection name">
    <input id="collectionDescription" type="text" name="description" value="Your collection description">
    <input type="hidden" name="metadataUri" value="{&quot;uri&quot;:&quot;https://1.uri.com&quot;,&quot;label&quot;:&quot;uri 1&quot;,&quot;description&quot;}">         
    <input type="hidden" name="metadataUri" value="{&quot;uri&quot;:&quot;https://2.uri.com&quot;,&quot;label&quot;:&quot;uri 2&quot;,&quot;description&quot;}">
    ...             
    <input type="submit" value="Submit">
</form>
```

### Example integration

Figure 2 and 3 show an example integration between the CLARIN VLO and the VCR. 

Figure 2 shows how to create a virtual collection based on some search result (Kleve + Nijmegen):
![Figure 2: VLO integration example!](./vcr_integration_example_1.png "Figure 2: VLO integration example")

 After clicking the highlighted “Create virtual collection from search result” button, the user is redirected to a confirmation page, shown in figure 3, before being sent to the VCR:
![Figure 3: VLO integration confirmation example!](./vcr_integration_example_2.png "Figure 2: VLO integration confirmation example")
