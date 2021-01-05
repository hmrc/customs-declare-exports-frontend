const fs            = require('fs')
const tariffCodes   = require('../data/tariff-codes')

const executeEtl = () => {
    fs.copyFile('../conf/code-lists/location-autocomplete-canonical-list.json', tariffCodes.countryCodes.externalMovementsPath, (err) => {
        if (err) throw err;
        console.log('location-autocomplete-canonical-list.json could not be copied to customs-movements-frontend');
    });

    fs.copyFile(tariffCodes.countryCodes.exportsFEOutputPath, tariffCodes.countryCodes.internalMovementsPath, (err) => {
        if (err) throw err;
        console.log('location-autocomplete-canonical-list.json could not be copied to customs-movements-frontend');
    });
}

module.exports = {
    executeEtl
}