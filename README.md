# customs-declare-exports-frontend

## About
This public-facing microservice is part of the Customs Exports Declaration Service (CEDS). It is designed to work in tandem with the [customs-declare-exports](https://github.com/hmrc/customs-declare-exports) back-end service.

It provides functionality for traders to submit and manage exports declarations (create, amend, copy and cancel declarations, view submissions and their notifications, and manage saved drafts).

| Key | Value | 
|-----|-------|
| Digital service | CDS Exports |
| Local port | `6791` |
| Base path | `/customs-declare-exports` |
| Back-end | [customs-declare-exports](https://github.com/hmrc/customs-declare-exports) (port `6792`) |
| Acceptance tests | [exports-ui-acceptance-tests](https://github.com/hmrc/exports-ui-acceptance-tests) |
| Performance tests | [exports-declarations-performance-tests](https://github.com/hmrc/exports-declarations-performance-tests) |
| Stubs used in Local and Staging |[customs-declarations-stub](https://github.com/hmrc/customs-declarations-stub) |

## How to Run This Service

### Prerequisites
Start all the services CDS Exports depends on with [Service Manager](https://github.com/hmrc/customs-declare-exports-frontend#service-manager-profiles):

```bash
sm2 --start CDS_EXPORTS_DECLARATION_ALL
```

### Running the Service Locally
To run this service from source (for example, to test your own changes), stop the instance started by Service Manager and run it with sbt:
```bash
sm2 --stop CUSTOMS_DECLARE_EXPORTS_FRONTEND
sbt run
```

The service starts on port `6791`. To access it, [sign in through the auth login stub](https://github.com/hmrc/exports-ui-acceptance-tests#enrolment-required), and you will be redirected to http://localhost:6791/customs-declare-exports/choice.

## How to Test This Service

### Local

#### Unit Tests
```bash
sbt test
```

#### Pre-Push Check
There is a script called `precheck.sh` that runs all tests, examines their coverage and checks if all the files are properly formatted.
It is good practice to run it just before pushing to GitHub.

```bash
./precheck.sh
```

#### Acceptance Tests (Smoke and Regression)
The acceptance tests for this service are in [exports-ui-acceptance-tests](https://github.com/hmrc/exports-ui-acceptance-tests). For how to run them and which environments they run against, see [README](https://github.com/hmrc/exports-ui-acceptance-tests#how-to-run-the-tests).

#### Performance Tests
The performance tests for this service are in [exports-declarations-performance-tests](https://github.com/hmrc/exports-declarations-performance-tests). For how to run them and which environments they run against, see [README](https://github.com/hmrc/exports-declarations-performance-tests#how-to-run-the-tests).

### Manual Testing in QA and Staging
After the successful [deployment pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSExports/job/customs-declare-exports-frontend-pipeline/), manual testing is performed in QA and/or Staging environment. For more details refer [Manual Service Verification in QA and Staging.](https://github.com/hmrc/exports-ui-acceptance-tests#manual-service-verification-in-qa-and-staging)

## Service Catalogue
- [customs-declare-exports-frontend in the MDTP Catalogue](https://catalogue.tax.service.gov.uk/repositories/customs-declare-exports-frontend)

## Jenkins Pipeline
- Front End Build and Deployment Pipeline
  -  [Build pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSExports/job/customs-declare-exports-frontend/)
  -  [Deployment pipeline](https://hmrcdigital.slack.com/archives/D0AHH4QKB9C/p1791392226925179)
- The acceptance test Jenkins jobs (smoke and regression) are listed in the [exports-ui-acceptance-tests README](https://github.com/hmrc/exports-ui-acceptance-tests#jenkins-builds).
- The performance test Jenkins jobs are listed in the [exports-declarations-performance-tests README](https://github.com/hmrc/exports-declarations-performance-tests#jenkins-builds).

## Service Manager Profiles
These profiles are defined in [service-manager-config](https://github.com/hmrc/service-manager-config).

| Profile | Use | Services started |
|---|---|---|
| `CDS_EXPORTS_DECLARATION_ALL` | Running and developing this service locally | `CUSTOMS_DECLARE_EXPORTS`, `CUSTOMS_DECLARE_EXPORTS_FRONTEND`, `AUTH`, `AUTH_LOGIN_API`, `CENTRALISED_AUTHORISATION_SERVER`, `AUTH_LOGIN_STUB`, `CUSTOMS_DECLARATIONS_STUB`, `CUSTOMS_DECLARATIONS_INFORMATION`, `USER_DETAILS`, `IDENTITY_VERIFICATION`, `CONTACT_FRONTEND`, `BAS_GATEWAY`, `BAS_GATEWAY_FRONTEND` |
| `CDS_EXPORTS_DECLARATION_ATS` | Running the [acceptance tests](https://github.com/hmrc/exports-ui-acceptance-tests#how-to-run-tests) | `CUSTOMS_DECLARE_EXPORTS`, `CUSTOMS_DECLARE_EXPORTS_FRONTEND`, `AUTH`, `AUTH_LOGIN_API`, `AUTH_LOGIN_STUB`, `BAS_GATEWAY`, `BAS_GATEWAY_FRONTEND`, `CUSTOMS_DECLARATIONS_STUB`, `USER_DETAILS`, `IDENTITY_VERIFICATION` |
| `CDS_EXPORTS_ALL` | All CDS Exports services (declarations, movements and internal) | See [service-manager-config](https://github.com/hmrc/service-manager-config) |

| Service | Port | Repository |
|---|---|---|
| `CUSTOMS_DECLARE_EXPORTS_FRONTEND` | 6791 | This service |
| `CUSTOMS_DECLARE_EXPORTS` | 6792 | [customs-declare-exports](https://github.com/hmrc/customs-declare-exports) |
| `CUSTOMS_DECLARATIONS_STUB` | 6790 | [customs-declarations-stub](https://github.com/hmrc/customs-declarations-stub) |

## Endpoints

All paths are relative to `/customs-declare-exports` and need an [authenticated user with the HMRC-CUS-ORG enrolment](https://github.com/hmrc/exports-ui-acceptance-tests#enrolment-required).

| Method | Path | Purpose | Sample request (query / form body) | Response |
|---|---|---|---|---|
| GET | `/` | Service start | – | `303` → `/choice` |
| GET | `/choice` | Landing page: create a declaration, view submissions, etc. | – | `200` |
| GET | `/dashboard` | Submitted declarations, grouped by status | `?groups=submitted&page=1&limit=25`<br>`groups`: `submitted`, `action`, `rejected`, `cancelled` | `200` |
| GET | `/saved-declarations` | Draft declarations | `?page=1` | `200` |
| GET | `/saved-declarations/:id` | Continue a draft | – | `303` → `/declaration/saved-summary`<br>Not found: `303` → `/saved-declarations` |
| POST | `/saved-declarations/:id/remove` | Delete a draft | `remove=true` (or `false`) | `303` → `/saved-declarations` |
| GET | `/submissions/:id/information` | Submission details and timeline; stores MRN, DUCR and LRN in the session | – | `200` |
| GET | `/submissions/:id/view` | View a submitted declaration | – | `200` |
| GET | `/submissions/:id/rejected-notifications` | Errors from CDS for a rejected declaration | – | `200` |
| POST | `/copy-declaration` | Copy a declaration with a new DUCR and LRN | `ducr.ducr=3GB986007773125-INVOICE123&lrn=QSLRN8514100` | `303` → `/declaration/saved-summary`<br>LRN used in the last 48 hours: `400` |
| POST | `/cancel-declaration` | Request cancellation (uses the submission data in the session) | `changeReason=1&statementDescription=No+longer+exported`<br>`changeReason`: `1` no longer required, `2` duplication, `3` other | `303` → `/cancellation-holding`<br>Already requested: `200` with error |
| GET | `/cancellation-result` | Outcome of a cancellation request | – | `200` |
| GET | `/ead-print-view/:mrn` | Printable Export Accompanying Document (EAD). *Verified email not needed* | `/ead-print-view/24GB1J1V8TS3JU1AR8` | `200`<br>MRN not found: error page |
| GET | `/file-upload` | Upload supporting documents. *Verified email not needed* | `?mrn=24GB1J1V8TS3JU1AR8` | `303` → `cds-file-upload-service/mrn-entry/:mrn` |
| GET | `/language/:lang` | Switch language | `english` or `cymraeg` | `303` |
| GET | `/sign-out` | Sign out. *No authentication needed* | `?signOutReason=UserAction` (or `SessionTimeout`) | `303` → `bas-gateway` sign-out |
| POST | `/declaration/standard-or-other` | Start a declaration; `STANDARD` creates the draft | `type=STANDARD` (or `NonStandardDeclarationType`) | `303` → `/declaration/type`, or `/declaration/declaration-choice` |
| POST | `/declaration/type` | Additional declaration type | `additionalDeclarationType=D`<br>Standard `A`/`D`, Simplified `C`/`F`, Occasional `B`/`E`, Clearance `J`/`K`, Supplementary `Y`/`Z` | `303` → `/declaration/declarant-details` (Clearance: `/declaration/do-you-have-ducr`) |
| GET | `/declaration/saved-summary` | Summary of the declaration in progress | – | `200` |
| POST | `/declaration/submit-your-declaration` | Submit the declaration to CDS | `fullName=Joe+Bloggs&jobRole=Export+Manager&email=joe.bloggs@example.com&confirmation=true` | `303` → `/declaration/holding`, then `/declaration/confirmation` |
| GET / POST | `/declaration/...` | Other journey pages (sections 1 to 6, amendments) | Page fields plus a button: `SaveAndContinue` (next page), `SaveAndReturnToSummary` or `SaveAndReturnToErrors` | `303` → next page |

## Developer Notes

### Feature Flags
This service uses feature flags to enable or disable some of its features. You can change or override them in config under the `microservice.services.features.<featureName>` key.

The feature flags and what they control:

`betaBanner = [true/false]` - When enabled, all pages in the service have a BETA banner.

### Scalafmt
The code is formatted with [sbt-scalafmt](https://scalameta.org/scalafmt/docs/installation.html#sbt), using the rules in `.scalafmt.conf`.

Check that all project files are formatted as expected:

```bash
sbt scalafmtCheckAll scalafmtSbtCheck
```

Format `*.sbt` and `project/*.scala` files:

```bash
sbt scalafmtSbt
```

Format all project files:

```bash
sbt scalafmtAll
```

### Autocomplete
This project has a
[TamperMonkey](https://chrome.google.com/webstore/detail/tampermonkey/dhdgffkkebhmkfjojejmpbldmpobfkfo?hl=en) (Google Chrome)
or
[GreaseMonkey](https://addons.mozilla.org/en-GB/firefox/addon/greasemonkey/) (Firefox)
`autocomplete` script to help you get through the form journey faster.

You can find these scripts in the [docs](https://github.com/hmrc/customs-declare-exports-frontend/tree/main/docs) directory.

### TariffCodeLists
As instructed by the Exports Product Manager and CDS stakeholders, the [CDS Tariff](https://www.gov.uk/government/collections/uk-trade-tariff-volume-3-for-cds--2)
is our source of truth for any CDS codes, until further instructions or until we connect to a service that provides this data.

We use the following codes:
 * [Country codes](https://www.gov.uk/government/publications/country-codes-for-the-customs-declaration-service) *Last updated 17 March 2023*
 * [Authorisation codes](https://www.gov.uk/government/publications/authorisation-type-codes-for-data-element-339-of-the-customs-declaration-service) (3/39 in tariff) *Last updated 1 July 2022*
 * [UK Office of Exit codes](https://www.gov.uk/government/publications/uk-customs-office-codes-for-data-element-512-of-the-customs-declaration-service) (5/12 in tariff) *Last updated 17 April 2023*
 * [Document type codes (previous document page)](https://www.gov.uk/government/publications/previous-document-codes-for-data-element-21-of-the-customs-declaration-service) (2/1 in tariff) *Last published: 1 August 2018*
 * [Package Type codes](https://www.gov.uk/government/publications/package-type-codes-for-data-element-69-of-the-customs-declaration-service) (6/9 in tariff) *Last published: 1 August 2018*
 * [Customs supervising office codes](https://www.gov.uk/government/publications/supervising-office-codes-for-data-element-527-of-the-customs-declaration-service) (5/27 in tariff) *Last updated 13 March 2023*

## License
This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
