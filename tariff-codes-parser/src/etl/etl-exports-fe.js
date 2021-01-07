const csv           = require('csv')
const fs            = require('fs')
const https         = require('https')
const csv2Json      = require('csvtojson')
const tariffCodes   = require('../data/tariff-codes')

const etlExportsFe = () => {
    const authorisationHoldersOutput = fs.createWriteStream(tariffCodes.authorisationHoldersCodes.exportsFEOutputPath);
    https.get(tariffCodes.authorisationHoldersCodes.tariffUrl, (response) => {
        response.pipe(
            csv.parse()).pipe(
            csv.transform((line) => {
                return line.map((value) => {
                    return value.trim().replace("—", "-").replace("–", "-").replace(tariffCodes.charactersToReplace, " ")
                })
            })).pipe(
            csv.stringify({quoted: true})).pipe(authorisationHoldersOutput)
    })

    const officesOfExitOutput = fs.createWriteStream(tariffCodes.officesOfExitCodes.exportsFEOutputPath);
    https.get(tariffCodes.officesOfExitCodes.tariffUrl, (response) => {
        response.pipe(
            csv.parse()).pipe(
            csv.transform((line) => {
                return [2, 3].map(index => line[index].trim().replace("—", "-").replace("–", "-").replace(tariffCodes.charactersToReplace, "")
                    .replace("Usual name", "Description").replace("COL code", "Code"))
            })).pipe(
            csv.stringify({quoted: true})).pipe(officesOfExitOutput)
    })

    const packageCodesOutput = fs.createWriteStream(tariffCodes.packageCodes.exportsFEOutputPath);
    https.get(tariffCodes.packageCodes.tariffUrl, (response) => {
        response.pipe(
            csv.parse()).pipe(
            csv.transform((line) => {
                return [0, 1].map(index => line[index].trim().replace("—", "-").replace("–", "-").replace(tariffCodes.charactersToReplace, "").replace("  ", " ")
                    .replace("Name", "Description"))
            })).pipe(
            csv.stringify({quoted: true})).pipe(packageCodesOutput)
    })

    const customsOfficeOutput = fs.createWriteStream(tariffCodes.supervisingOfficeCodes.exportsFEOutputPath);
    https.get(tariffCodes.supervisingOfficeCodes.tariffUrl, (response) => {
        response.pipe(
            csv.parse()).pipe(
            csv.transform((line) => {
                return [1, 0].map(index => line[index].trim().replace("—", "-").replace("–", "-").replace(tariffCodes.charactersToReplace, "")
                    .replace("HMRC office", "Description").replace("UK supervising office code to be declared in Data Element 5/27", "Code"))
            })).pipe(
            csv.stringify({quoted: true})).pipe(customsOfficeOutput)
    })

    https.get(tariffCodes.documentTypeCodes.tariffUrl, (response) => {
        csv2Json().fromStream(response)
            .then(records => {
                let transformedRecords = [];
                records.map((record) => {

                    // identify when potential comments might start
                    if (Object.values(record)[0].trim().length === 0) return;

                    const description = record['Reference identifier for Document Name/Type (3rd component)'].replace("—", "-").replace(/ /g, " ").replace(/ /g, " ").replace("  ", " ")
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
                records.map((record) => {

                    // identify when potential comments might start
                    if (Object.values(record)[0].trim().length === 0) return;

                    const transformedCountryRecord = [record['Country name'].replace("*", ""), `country:${record['Country code']}`]
                    transformedRecords = transformedRecords.concat([transformedCountryRecord])
                })
                fs.writeFile(tariffCodes.countryCodes.exportsFEOutputPath, JSON.stringify(transformedRecords, null, 4), (err) => {
                    if (err) throw err;
                });
            }).catch(err => console.log(err));
    })
}

module.exports = {
    executeEtl: etlExportsFe
}