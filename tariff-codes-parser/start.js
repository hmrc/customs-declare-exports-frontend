const tariffCodes   = require('./src/data/tariff-codes')
const etlAcceptance = require('./src/etl/etl-acceptance')
const etlExports    = require('./src/etl/execute-etl')

//Update CDS Exports code lists
etlExports.executeEtl()

// Update CDS Exports Acceptance code lists
tariffCodes.supportedAcceptanceCodes.map( (tariffObj) =>
    etlAcceptance.etl(tariffObj))

etlAcceptance.etlAuthorisationHolders(tariffCodes.authorisationHoldersCodes)