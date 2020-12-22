# customs-declare-exports-frontend

[![Build Status](https://travis-ci.org/hmrc/customs-declare-exports-frontend.svg)](https://travis-ci.org/hmrc/customs-declare-exports-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/customs-declare-exports-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/customs-declare-exports-frontend/_latestVersion)

Declare Exports Frontend Application.


# Developer notes
You may want to point to a non-local frontend Assets server.  If so then simply set an environment variable
ASSETS_URL

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")

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

### Auto Complete

This project has a 
[TamperMonkey](https://chrome.google.com/webstore/detail/tampermonkey/dhdgffkkebhmkfjojejmpbldmpobfkfo?hl=en) (Google Chrome)
or 
[GreaseMonkey](https://addons.mozilla.org/en-GB/firefox/addon/greasemonkey/) (Firefox)
Auto Complete Script to help speed through the form journey.

These scripts can be found in the docs directory.

### Updating Tariff Code lists

There is a small Node.js project on tariff-codes-parser that allows us to automate this task.
We have been informed that the most up-to-date codes can be found on the [CDS Tariff](https://www.gov.uk/government/collections/uk-trade-tariff-volume-3-for-cds--2).

We use the following codes:
 * [Country codes](https://www.gov.uk/government/publications/country-codes-for-the-customs-declaration-service)
 * [Authorisation codes](https://www.gov.uk/government/publications/authorisation-type-codes-for-data-element-339-of-the-customs-declaration-service) (3/39 in tariff)
 * [UK Office of Exit codes](https://www.gov.uk/government/publications/uk-customs-office-codes-for-data-element-512-of-the-customs-declaration-service) (5/12 in tariff)
 * [Document type codes (previous document page)](https://www.gov.uk/government/publications/previous-document-codes-for-data-element-21-of-the-customs-declaration-service) (2/1 in tariff)
 * [Package Type codes](https://www.gov.uk/government/publications/package-type-codes-for-data-element-69-of-the-customs-declaration-service) (6/9 in tariff)
 * [Customs supervising office codes](https://www.gov.uk/government/publications/supervising-office-codes-for-data-element-527-of-the-customs-declaration-service) (5/27 in tariff)

**The steps required to update the code-lists:** 
 1. Install [Node.js](https://nodejs.dev/learn/how-to-install-nodejs) and [npm](https://www.npmjs.com/get-npm) on your local machine.
 2. Clone [Exports Acceptance project](https://github.com/hmrc/cds-exports-acceptance) into the same repo as this project, as we will have to update the code lists as well. 
 3. Ensure the tariff URLs are still valid and update them on `tariff-codes.js`
 4. Go into the Node.js project: `cd tariff-codes-parser`    
 5. Install the Node.js dependencies:  `npm install`
 6. Execute, parse and update the code lists: `node start.js`
 7. Check the differences `git diff` and/or create a Pull Request.
 
 **Recommended future work:** 
  * Consider moving this code to Scala
