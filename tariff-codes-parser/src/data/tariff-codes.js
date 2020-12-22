const countryCodes = {
    tariffUrl: 'https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/897009/Table_1_-_listed_by_country_code.csv',
    exportsFEOutputPath: '../conf/code-lists/location-autocomplete-canonical-list.json',
    e2eOutputPath: '../../cds-exports-acceptance/src/main/resources/lists/countries.json'
}

const authorisationHoldersCodes = {
    tariffUrl: 'https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/867581/Authorisation_type_codes_for_data_element.csv',
    exportsFEOutputPath: '../conf/code-lists/holder-of-authorisation-codes.csv',
    e2eOutputPath: '../../cds-exports-acceptance/src/main/resources/lists/holder_codes.txt'
}

const officesOfExitCodes = {
    tariffUrl: 'https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/870573/UK_Customs_Office_List__COL__codes_5-12.csv',
    exportsFEOutputPath: '../conf/code-lists/office-of-exits.csv',
    e2eOutputPath: '../../cds-exports-acceptance/src/main/resources/lists/officeOfExit.json'
}

const packageCodes = {
    tariffUrl: 'https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/888831/Package-type-codes-6-9_-_v1.csv',
    exportsFEOutputPath: '../conf/code-lists/package-types.csv',
    e2eOutputPath: '../../cds-exports-acceptance/src/main/resources/lists/packageType.json'
}

const supervisingOfficeCodes = {
    tariffUrl: 'https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/871693/Supervising_office_codes_5-27.csv',
    exportsFEOutputPath: '../conf/code-lists/customs-offices.csv',
    e2eOutputPath: '../../cds-exports-acceptance/src/main/resources/lists/customsOffice.json'
}

const documentTypeCodes = {
    tariffUrl: 'https://assets.publishing.service.gov.uk/government/uploads/system/uploads/attachment_data/file/915216/Previous_document_codes_for_Data_Element_2-1_of_the_Customs_Declaration_Service_-_v2.csv',
    exportsFEOutputPath: '../conf/code-lists/document-type-autocomplete-list.json',
    e2eOutputPath: '../../cds-exports-acceptance/src/main/resources/lists/documentCode.json'
}

module.exports = {
    countryCodes,
    authorisationHoldersCodes,
    officesOfExitCodes,
    packageCodes,
    supervisingOfficeCodes,
    documentTypeCodes
}