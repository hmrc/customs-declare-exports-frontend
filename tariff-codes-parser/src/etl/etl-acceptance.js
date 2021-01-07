const fs            = require('fs')
const https         = require('https')
const csv2Json      = require('csvtojson')
const tariffCodes   = require('../data/tariff-codes')

const etlAuthorisationHolders = (obj) => https.get(obj.tariffUrl, (response) => {
    csv2Json().fromStream(response)
        .then(records => {
            let transformedRecords = '';

            records.map( (record) => {
                // identify when potential comments might start
                if (Object.values(record)[0].trim().length === 0) return;

                transformedRecords +=  (transformedRecords.length !== 0? '\n' : '') + record['Code']
            })

            fs.writeFile(obj.e2eOutputPath, transformedRecords, (err) => {
                if (err) throw err;
            });
        }).catch(err => console.log(err));
})

const extractData = (tariffObj, records) => {
    let transformedRecords = [];
    records.map((record) => {

        // identify when potential comments might start
        if (Object.values(record)[0].trim().length === 0) return;

        let transformedRecord = {
            'code': record[tariffObj.tariffHeaders.code],
            'description': record[tariffObj.tariffHeaders.description].trim().replace("—", "-").replace("–", "-").replace(tariffCodes.charactersToReplace, " ").replace("  ", " ")
        }

        if (record['Currency code']) {
            //country data, which has a slightly different structure and headers
            transformedRecord = {
                'code': record[tariffObj.tariffHeaders.code],
                'name': record[tariffObj.tariffHeaders.description].trim().replace("*", "").replace("—", "-").replace("–", "-").replace(tariffCodes.charactersToReplace, " ").replace("  ", " "),
                'zone': tariffCodes.euCurrencyCodes.includes(record['Currency code']) ? 'EU' : ''
            }
        }

        transformedRecords = transformedRecords.concat(transformedRecord)
    })
    const transformedRecordsObj = {};
    transformedRecordsObj[tariffObj.e2eArrayProperty] = transformedRecords
    return transformedRecordsObj;
}

const etl = (tariffObj) => {
    https.get(tariffObj.tariffUrl, (response) => {
        csv2Json().fromStream(response)
            .then(records => {
                const transformedRecordsObj = extractData(tariffObj, records);
                fs.writeFile(tariffObj.e2eOutputPath, JSON.stringify(transformedRecordsObj, null, 4), (err) => {
                    if (err) throw err;
                });
            }).catch(err => console.log(err));
    })
}

module.exports = {
    etlAuthorisationHolders,
    etl
}