const fs            = require('fs')
const tariffCodes   = require('../data/tariff-codes')

const executeEtl = () => {
    fs.copyFile(tariffCodes.countryCodes.exportsFEOutputPath, tariffCodes.countryCodes.exportsBEOutputPath, (err) => {
        if (err) {
            console.log('location-autocomplete-canonical-list.json could not be copied to customs-declare-exports');
            throw err;
        }
    });
}

module.exports = {
    executeEtl
}