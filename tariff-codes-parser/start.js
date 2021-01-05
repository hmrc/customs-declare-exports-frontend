const tariffCodes   = require('./src/data/tariff-codes')
const etlAcceptance = require('./src/etl/etl-acceptance')
const etlMovements = require('./src/etl/etl-movements')
const etlExportsBe = require('./src/etl/etl-exports-be')
const etlExportsFe    = require('./src/etl/etl-exports-fe')

//Update CDS Exports FE code lists
etlExportsFe.executeEtl()

//Update CDS Exports BE code lists
etlExportsBe.executeEtl()

//Update Internal and External Movements FE code lists
etlMovements.executeEtl()

// Update CDS Exports Acceptance code lists
tariffCodes.supportedAcceptanceCodes.map( (tariffObj) =>
    etlAcceptance.etl(tariffObj))

etlAcceptance.etlAuthorisationHolders(tariffCodes.authorisationHoldersCodes)

