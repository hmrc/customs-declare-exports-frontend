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
[GreaseMonkey](https://addons.mozilla.org/en-GB/firefox/addon/greasemonkey/)
Auto Complete Script to help speed through the form journey.

These scripts can be found in the docs directory.
