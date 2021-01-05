const countryCodes = {
    id: 'countryCodes',
    tariffUrl: 'https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/897009/Table_1_-_listed_by_country_code.csv',
    exportsFEOutputPath: '../conf/code-lists/location-autocomplete-canonical-list.json',
    e2eOutputPath: '../../cds-exports-acceptance/src/main/resources/lists/countries.json',
    exportsBEOutputPath: '../../customs-declare-exports/conf/code-lists/location-autocomplete-canonical-list.json',
    externalMovementsPath: '../../customs-movements-frontend/conf/location-autocomplete-canonical-list.json',
    internalMovementsPath: '../../customs-exports-internal-frontend/conf/location-autocomplete-canonical-list.json',
    tariffHeaders: {
        code: 'Country code',
        description: 'Country name'
    },
    e2eArrayProperty: 'countries'
}

const authorisationHoldersCodes = {
    id: 'authorisationHoldersCodes',
    tariffUrl: 'https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/867581/Authorisation_type_codes_for_data_element.csv',
    exportsFEOutputPath: '../conf/code-lists/holder-of-authorisation-codes.csv',
    e2eOutputPath: '../../cds-exports-acceptance/src/main/resources/lists/holder_codes.txt'
}

const officesOfExitCodes = {
    id: 'officesOfExitCodes',
    tariffUrl: 'https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/870573/UK_Customs_Office_List__COL__codes_5-12.csv',
    exportsFEOutputPath: '../conf/code-lists/office-of-exits.csv',
    e2eOutputPath: '../../cds-exports-acceptance/src/main/resources/lists/officeOfExit.json',
    tariffHeaders: {
        code: 'COL code',
        description: 'Usual name'
    },
    e2eArrayProperty: 'officesOfExit'

}

const packageCodes = {
    id: 'packageCodes',
    tariffUrl: 'https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/888831/Package-type-codes-6-9_-_v1.csv',
    exportsFEOutputPath: '../conf/code-lists/package-types.csv',
    e2eOutputPath: '../../cds-exports-acceptance/src/main/resources/lists/packageType.json',
    tariffHeaders: {
        code: 'Code',
        description: 'Name'
    },
    e2eArrayProperty: 'packageTypes'
}

const supervisingOfficeCodes = {
    id: 'supervisingOfficeCodes',
    tariffUrl: 'https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/871693/Supervising_office_codes_5-27.csv',
    exportsFEOutputPath: '../conf/code-lists/customs-offices.csv',
    e2eOutputPath: '../../cds-exports-acceptance/src/main/resources/lists/customsOffice.json',
    tariffHeaders: {
        code: 'UK supervising office code to be declared in Data Element 5/27',
        description: 'HMRC office'
    },
    e2eArrayProperty: 'customsOffices'
}

const documentTypeCodes = {
    id: 'documentTypeCodes',
    tariffUrl: 'https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/915216/Previous_document_codes_for_Data_Element_2-1_of_the_Customs_Declaration_Service_-_v2.csv',
    exportsFEOutputPath: '../conf/code-lists/document-type-autocomplete-list.json',
    e2eOutputPath: '../../cds-exports-acceptance/src/main/resources/lists/documentCode.json',
    tariffHeaders: {
        code: 'Code to be Declared (2nd component)',
        description: 'Reference identifier for Document Name/Type (3rd component)'
    },
    e2eArrayProperty: 'documentCodes'
}
const euCurrencyCodes = ['EUR', 'BGN', 'HRK', 'CZK', 'DKK', 'HUF', 'PLN', 'RON', 'SEK', 'CHF']

const charactersToReplace = /[^a-zA-Z0-9,;#%\.\-()<> &:'’\[\]\\"\*\/]/g

const supportedAcceptanceCodes = [countryCodes, officesOfExitCodes, packageCodes, supervisingOfficeCodes, documentTypeCodes]

module.exports = {
    charactersToReplace,
    euCurrencyCodes,
    supportedAcceptanceCodes,
    countryCodes,
    authorisationHoldersCodes,
    officesOfExitCodes,
    packageCodes,
    supervisingOfficeCodes,
    documentTypeCodes
}