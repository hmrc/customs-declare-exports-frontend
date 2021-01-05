const fs            = require('fs')
const tariffCodes   = require('../data/tariff-codes')

const executeEtl = () => {
    fs.copyFile(tariffCodes.countryCodes.exportsFEOutputPath, tariffCodes.countryCodes.externalMovementsPath, (err) => {
        if (err) {
            console.log('location-autocomplete-canonical-list.json could not be copied to customs-movements-frontend');
            throw err;
        }
    });

    fs.copyFile(tariffCodes.countryCodes.exportsFEOutputPath, tariffCodes.countryCodes.internalMovementsPath, (err) => {
        if (err) {
            console.log('location-autocomplete-canonical-list.json could not be copied to customs-exports-internal-movements');
            throw err;
        }
    });
}

module.exports = {
    executeEtl
}