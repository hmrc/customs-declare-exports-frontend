# customs-declare-exports-frontend

## Summary
This public-facing microservice is part of Customs Exports Declaration Service (CEDS). It is designed to work in tandem with [customs-declare-exports](https://github.com/hmrc/customs-declare-exports) service.

It provides functionality to submit and manage exports declarations.

## Prerequisites
This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at a [JRE](https://www.java.com/en/download/) to run and a JDK for development.

This service does **not** use MongoDB.

This service depends on other services. The easiest way to set up required microservices is to use Service Manager and profiles from [service-manager-config](https://github.com/hmrc/service-manager-config/) repository:
- CDS_EXPORTS_DECLARATION_DEPS - all services EXCEPT both declarations services
- CDS_EXPORTS_DECLARATION_ALL - all services together with both declarations services

### Running the application
In order to run the application you need to have SBT installed. Then, it is enough to start the service with: 

`sbt run`

### Testing the application
This repository contains unit tests for the service. In order to run them, simply execute:

`sbt test`


## Developer notes
You may want to point to a non-local frontend Assets server.  If so then simply set an environment variable
ASSETS_URL

### Feature flags
This service uses feature flags to enable/disable some of its features. These can be changed/overridden in config under `microservice.services.features.<featureName>` key.

The list of feature flags and what they are responsible for:

`betaBanner = [enabled/disabled]` - When enabled, all pages in the service have BETA banner.

`ead = [enabled/disabled]` - When enabled, page under url */submissions/[ID]/information* may contain link to generate Exports Accompanying Document.

`sfus = [enabled/disabled]` - When enabled, page under url */submissions/[ID]/information* may contain link to Secure File Upload Service.

`secureMessagingInbox = [disabled / sfus / exports]` - Controls which Secure Messaging Inbox is used - none, the one embedded into exports service, or redirects to inbox in Secure File Upload Service.

`use-improved-error-messages = [true/false]` - When enabled, DMS errors have descriptions made by Exports team designers. Otherwise, they have default CDS descriptions.

### Scalastyle

Project contains scalafmt plugin.

Commands for code formatting:

```
scalafmt        # format compile sources
test:scalafmt   # format test sources
sbt:scalafmt    # format .sbt source
```

To ensure everything is formatted you can check project using commands below

```
scalafmt::test      # check compile sources
test:scalafmt::test # check test sources
sbt:scalafmt::test  # check .sbt sources
```

### Pre-merge check
There is a script called `precheck.sh` that runs all tests, examine their coverage and check if all the files are properly formatted.
It is a good practise to run it just before pushing to GitHub. 


### Auto Complete

This project has a 
[TamperMonkey](https://chrome.google.com/webstore/detail/tampermonkey/dhdgffkkebhmkfjojejmpbldmpobfkfo?hl=en) (Google Chrome)
or 
[GreaseMonkey](https://addons.mozilla.org/en-GB/firefox/addon/greasemonkey/) (Firefox)
Auto Complete Script to help speed through the form journey.

These scripts can be found in the docs directory.

### Updating Tariff Code lists (!!THIS IS NOW DEPRECATED!!)

As per Exports Product Manager and CDS Stakeholders instructions, the [CDS Tariff](https://www.gov.uk/government/collections/uk-trade-tariff-volume-3-for-cds--2) 
is our source of truth for any CDS codes going forward, until further instructions or until we connect with a service that 
provides this data for us.

There is a small Node.js project on tariff-codes-parser that allows us to automate this task.
We have been informed that the most up-to-date codes can be found on the [CDS Tariff](https://www.gov.uk/government/collections/uk-trade-tariff-volume-3-for-cds--2).

We use the following codes:
 * [Country codes](https://www.gov.uk/government/publications/country-codes-for-the-customs-declaration-service) *Last published: 1 August 2018*
 * [Authorisation codes](https://www.gov.uk/government/publications/authorisation-type-codes-for-data-element-339-of-the-customs-declaration-service) (3/39 in tariff) *Last published: 1 August 2018*
 * [UK Office of Exit codes](https://www.gov.uk/government/publications/uk-customs-office-codes-for-data-element-512-of-the-customs-declaration-service) (5/12 in tariff) *Last published: 1 August 2018*
 * [Document type codes (previous document page)](https://www.gov.uk/government/publications/previous-document-codes-for-data-element-21-of-the-customs-declaration-service) (2/1 in tariff) *Last published: 1 August 2018*
 * [Package Type codes](https://www.gov.uk/government/publications/package-type-codes-for-data-element-69-of-the-customs-declaration-service) (6/9 in tariff) *Last published: 1 August 2018*
 * [Customs supervising office codes](https://www.gov.uk/government/publications/supervising-office-codes-for-data-element-527-of-the-customs-declaration-service) (5/27 in tariff) *Last published: 1 August 2018*

**The steps required to update the code-lists:** 
 1. Install [Node.js](https://nodejs.dev/learn/how-to-install-nodejs) and [npm](https://www.npmjs.com/get-npm) on your local machine.
 2. Clone [Exports Acceptance project](https://github.com/hmrc/cds-exports-acceptance), [External Movements](https://github.com/hmrc/customs-movements-frontend), [Internal Movements](https://github.com/hmrc/customs-exports-internal-frontend) and [Declarations BE](https://github.com/hmrc/customs-declare-exports) into the same workspace folder as this project, as we will have to update the code lists as well. 
 3. Ensure the tariff URLs are still valid and update them on `tariff-codes.js`
 4. Go into the Node.js project: `cd tariff-codes-parser`    
 5. Install the Node.js dependencies:  `npm install`
 6. Execute, parse and update the code lists: `node start.js`
 7. Check the differences `git diff` and/or create a Pull Request.
 
 **Recommended future work:** 
  * Consider moving this code to Scala

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
