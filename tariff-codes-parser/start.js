const csv           = require('csv')
const fs            = require('fs')
const https         = require('https')
const csv2Json      = require('csvtojson')
const tariffCodes   = require('./src/data/tariff-codes')

const characterToReplace = /[^a-zA-Z0-9,\.\-()<> &:'’\[\]\\"\*\/]/g

const authorisationHoldersOutput = fs.createWriteStream(tariffCodes.authorisationHoldersCodes.exportsFEOutputPath);
https.get(tariffCodes.authorisationHoldersCodes.tariffUrl, (response) => {
    response.pipe(
        csv.parse     ()).pipe(
        csv.transform ( (line) => {
                    return line.map( (value) => {
                        return value.trim().replace("—","-").replace("–","-").replace(characterToReplace," ")
                      })})).pipe(
        csv.stringify ({quoted: true })).pipe(authorisationHoldersOutput)
})

const officesOfExitOutput = fs.createWriteStream(tariffCodes.officesOfExitCodes.exportsFEOutputPath);
https.get(tariffCodes.officesOfExitCodes.tariffUrl, (response) => {
    response.pipe(
        csv.parse     ()).pipe(
        csv.transform ( (line) => {
                    return [2,3].map(index => line[index].trim().replace("—","-").replace("–","-").replace(characterToReplace," ")
                    .replace("Usual name", "Description").replace("COL code","Code"))
                    })).pipe(
        csv.stringify ({quoted: true})).pipe(officesOfExitOutput)
})

const packageCodesOutput = fs.createWriteStream(tariffCodes.packageCodes.exportsFEOutputPath);
https.get(tariffCodes.packageCodes.tariffUrl, (response) => {
    response.pipe(
        csv.parse     ()).pipe(
        csv.transform ( (line) => {
                    return [0,1].map(index => line[index].trim().replace("—","-").replace("–","-").replace(characterToReplace," ")
                    .replace("Name", "Description"))
                    })).pipe(
        csv.stringify ({quoted: true})).pipe(packageCodesOutput)
})

const customsOfficeOutput = fs.createWriteStream(tariffCodes.supervisingOfficeCodes.exportsFEOutputPath);
https.get(tariffCodes.supervisingOfficeCodes.tariffUrl, (response) => {
    response.pipe(
        csv.parse     ()).pipe(
        csv.transform ( (line) => {
                    return [1,0].map(index => line[index].trim().replace("—","-").replace("–","-").replace(characterToReplace," ")
                    .replace("HMRC office", "Description").replace("UK supervising office code to be declared in Data Element 5/27", "Code"))
                    })).pipe(
        csv.stringify ({quoted: true})).pipe(customsOfficeOutput)
})

https.get(tariffCodes.documentTypeCodes.tariffUrl, (response) => {
    csv2Json().fromStream(response)
    .then(records => {
        let transformedRecords = [];
        records.map( (record) => {
            
            // identify when potential comments might start
            if (Object.values(record)[0].trim().length === 0) return;

            const description = record['Reference identifier for Document Name/Type (3rd component)'].replace(/ /g, " ").replace(/ /g, " ")
            const code = record['Code to be Declared (2nd component)'].replace(/ /g, " ")
            
            transformedRecords = transformedRecords.concat([[description, code]])
        })
        fs.writeFile(tariffCodes.documentTypeCodes.exportsFEOutputPath, JSON.stringify(transformedRecords, null, 4), (err) => {
            if (err) throw err;
        });
    }).catch(err => console.log(err));
})

https.get(tariffCodes.countryCodes.tariffUrl, (response) => {
    csv2Json().fromStream(response)
    .then(records => {
        let transformedRecords = [];
        records.map( (record) => {

            // identify when potential comments might start
            if (Object.values(record)[0].trim().length === 0) return;

            const transformedCountryRecord = [record['Country name'], `country:${record['Country code']}`]
            transformedRecords = transformedRecords.concat([transformedCountryRecord])
        })
        fs.writeFile(tariffCodes.countryCodes.exportsFEOutputPath, JSON.stringify(transformedRecords, null, 4), (err) => {
            if (err) throw err;
        });
    }).catch(err => console.log(err));
})

// Update cds-exports-acceptance JSON code lists

https.get(tariffCodes.countryCodes.tariffUrl, (response) => {
    const euCurrencyCodes = ['EUR', 'BGN', 'HRK', 'CZK', 'DKK', 'HUF', 'PLN', 'RON', 'SEK', 'CHF']
    csv2Json().fromStream(response)
        .then(records => {
            let countries = [];
            records.map( (record) => {

                // identify when potential comments might start
                if (Object.values(record)[0].trim().length === 0) return;

                const countryRecord = {
                    'name': record['Country name'],
                    'code': record['Country code'],
                    'zone': euCurrencyCodes.includes(record['Currency code'])? 'EU' : ''
                }
                countries = countries.concat(countryRecord)
            })
            const countryCodeObj = {'countries': countries}
            fs.writeFile(tariffCodes.countryCodes.e2eOutputPath, JSON.stringify(countryCodeObj, null, 4), (err) => {
                if (err) throw err;
            });
        }).catch(err => console.log(err));
})

https.get(tariffCodes.officesOfExitCodes.tariffUrl, (response) => {
    csv2Json().fromStream(response)
        .then(records => {
            let transformedRecords = [];
            records.map( (record) => {

                // identify when potential comments might start
                if (Object.values(record)[0].trim().length === 0) return;

                const transformedRecord = {
                    'description': record['Usual name'].trim().replace("—","-").replace("–","-").replace(characterToReplace," "),
                    'code': record['COL code'],
                }
                transformedRecords = transformedRecords.concat(transformedRecord)
            })
            const transformedRecordsObj = {'officesOfExit': transformedRecords}
            fs.writeFile(tariffCodes.officesOfExitCodes.e2eOutputPath, JSON.stringify(transformedRecordsObj, null, 4), (err) => {
                if (err) throw err;
            });
        }).catch(err => console.log(err));
})

https.get(tariffCodes.packageCodes.tariffUrl, (response) => {
    csv2Json().fromStream(response)
        .then(records => {
            let transformedRecords = [];
            records.map( (record) => {

                // identify when potential comments might start
                if (Object.values(record)[0].trim().length === 0) return;

                const transformedRecord = {
                    'code': record['Code'],
                    'description': record['Name'].trim().replace("—","-").replace("–","-").replace(characterToReplace," ")
                }
                transformedRecords = transformedRecords.concat(transformedRecord)
            })
            const transformedRecordsObj = {'packageTypes': transformedRecords}
            fs.writeFile(tariffCodes.packageCodes.e2eOutputPath, JSON.stringify(transformedRecordsObj, null, 4), (err) => {
                if (err) throw err;
            });
        }).catch(err => console.log(err));
})

https.get(tariffCodes.documentTypeCodes.tariffUrl, (response) => {
    csv2Json().fromStream(response)
        .then(records => {
            let transformedRecords = [];
            records.map( (record) => {

                // identify when potential comments might start
                if (Object.values(record)[0].trim().length === 0) return;

                const transformedRecord = {
                    'code': record['Code to be Declared (2nd component)'].replace(/ /g, " "),
                    'description': record['Reference identifier for Document Name/Type (3rd component)'].replace(/ /g, " ").replace(/ /g, " ")
                }
                transformedRecords = transformedRecords.concat(transformedRecord)
            })
            const transformedRecordsObj = {'documentCodes': transformedRecords}
            fs.writeFile(tariffCodes.documentTypeCodes.e2eOutputPath, JSON.stringify(transformedRecordsObj, null, 4), (err) => {
                if (err) throw err;
            });
        }).catch(err => console.log(err));
})

https.get(tariffCodes.supervisingOfficeCodes.tariffUrl, (response) => {
    csv2Json().fromStream(response)
        .then(records => {
            let transformedRecords = [];
            records.map( (record) => {

                // identify when potential comments might start
                if (Object.values(record)[0].trim().length === 0) return;

                const transformedRecord = {
                    'code': record['UK supervising office code to be declared in Data Element 5/27'],
                    'description': record['HMRC office'].trim().replace("—","-").replace("–","-").replace(characterToReplace," ")
                }
                transformedRecords = transformedRecords.concat(transformedRecord)
            })
            const transformedRecordsObj = {'customsOffices': transformedRecords}
            fs.writeFile(tariffCodes.supervisingOfficeCodes.e2eOutputPath, JSON.stringify(transformedRecordsObj, null, 4), (err) => {
                if (err) throw err;
            });
        }).catch(err => console.log(err));
})

https.get(tariffCodes.authorisationHoldersCodes.tariffUrl, (response) => {
    csv2Json().fromStream(response)
        .then(records => {
            let transformedRecords = '';

            records.map( (record) => {
                // identify when potential comments might start
                if (Object.values(record)[0].trim().length === 0) return;

                transformedRecords +=  (transformedRecords.length !== 0? '\n' : '') + record['Code']
            })

            fs.writeFile(tariffCodes.authorisationHoldersCodes.e2eOutputPath, transformedRecords, (err) => {
                if (err) throw err;
            });
        }).catch(err => console.log(err));
})