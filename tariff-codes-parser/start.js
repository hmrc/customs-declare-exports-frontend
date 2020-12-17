const csv       = require('csv')
const fs        = require('fs')
const https     = require('https')
const csv2Json  = require('csvtojson')
const urls      = require('./src/data/urls')
const outFiles  = require('./src/data/output-files')


const authorisationHoldersOutput = fs.createWriteStream(outFiles.authorisationHoldersCodes);
https.get(urls.authorisationHoldersCodes, (response) => {
    response.pipe(
        csv.parse     ()).pipe(
        csv.transform ( (line) => {
                    return line.map( (value) => {
                        return value.trim().replace("—","-").replace("–","-").replace(/[^a-zA-Z0-9,\.\-() \*\/]/g," ")
                      })})).pipe(
        csv.stringify ({quoted: true })).pipe(authorisationHoldersOutput)
})

const officesOfExitOutput = fs.createWriteStream(outFiles.officesOfExitCodes);
https.get(urls.officesOfExitCodes, (response) => {
    response.pipe(
        csv.parse     ()).pipe(
        csv.transform ( (line) => {
                    return [2,3].map(index => line[index].trim().replace("—","-").replace("–","-").replace(/[^a-zA-Z0-9,\.\-() \*\/]/g," ")
                    .replace("Usual name", "Description").replace("COL code","Code"))
                    })).pipe(
        csv.stringify ({quoted: true})).pipe(officesOfExitOutput)
})

const packageCodesOutput = fs.createWriteStream(outFiles.packageCodes);
https.get(urls.packageCodes, (response) => {
    response.pipe(
        csv.parse     ()).pipe(
        csv.transform ( (line) => {
                    return [0,1].map(index => line[index].trim().replace("—","-").replace("–","-").replace(/[^a-zA-Z0-9,\.\-() \*\/]/g," ")
                    .replace("Name", "Description"))
                    })).pipe(
        csv.stringify ({quoted: true})).pipe(packageCodesOutput)
})

const customsOfficeOutput = fs.createWriteStream(outFiles.supervisingOfficeCodes);
https.get(urls.supervisingOfficeCodes, (response) => {
    response.pipe(
        csv.parse     ()).pipe(
        csv.transform ( (line) => {
                    return [1,0].map(index => line[index].trim().replace("—","-").replace("–","-").replace(/[^a-zA-Z0-9,\.\-() \*\/]/g," ")
                    .replace("HMRC office", "Description").replace("UK supervising office code to be declared in Data Element 5/27", "Code"))
                    })).pipe(
        csv.stringify ({quoted: true})).pipe(customsOfficeOutput)
})

https.get(urls.documentTypeCodes, (response) => {
    csv2Json().fromStream(response)
    .then(records => {
        var transformedRecords = []
        records.map( (record) => {
            
            // identify when potential comments might start
            if (Object.values(record)[0].trim().length === 0) return;

            const description = record['Reference identifier for Document Name/Type (3rd component)'].replace(/ /g, " ").replace(/ /g, " ")
            const code = record['Code to be Declared (2nd component)'].replace(/ /g, " ")
            
            transformedRecords = transformedRecords.concat([[description, code]])
        })
        fs.writeFile(outFiles.documentTypeCodes, JSON.stringify(transformedRecords, null, 4), (err) => {
            if (err) throw err;
        });
    }).catch(err => console.log(err));
})

https.get(urls.countryCodes, (response) => {
    csv2Json().fromStream(response)
    .then(records => {
        var transformedRecords = []
        records.map( (record) => {

            // identify when potential comments might start
            if (Object.values(record)[0].trim().length === 0) return;

            const transformedCountryRecord = [record['Country name'], `country:${record['Country code']}`]
            transformedRecords = transformedRecords.concat([transformedCountryRecord])
        })
        fs.writeFile(outFiles.countryCodes, JSON.stringify(transformedRecords, null, 4), (err) => {
            if (err) throw err;
        });
    }).catch(err => console.log(err));
})