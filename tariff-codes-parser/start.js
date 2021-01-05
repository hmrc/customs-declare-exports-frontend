const tariffCodes   = require('./src/data/tariff-codes')
const etlAcceptance = require('./src/etl/etl-acceptance')
const etlMovements = require('./src/etl/etl-movements')
const etlExports    = require('./src/etl/execute-etl')

//Update CDS Exports code lists
etlExports.executeEtl()

// Update CDS Exports Acceptance code lists
tariffCodes.supportedAcceptanceCodes.map( (tariffObj) =>
    etlAcceptance.etl(tariffObj))

//Update Movements code lists
etlMovements.executeEtl()

// Update Exports Acceptance tests
etlAcceptance.etlAuthorisationHolders(tariffCodes.authorisationHoldersCodes)

