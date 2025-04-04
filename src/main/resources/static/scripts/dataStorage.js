const contentArea = document.getElementById('content');
let timer = null;
let alerts = [];
let existingChart = null;
let currentLanguage;
let monthAbbreviations = [];
let bankAccountId;
let bankAccounts = {};
const searchStringFields = [
    { addButtonId: "addCounterPartyStrings", inputId: "inputCounterPartyStrings", listId: "counterPartySearchStrings" },
    { addButtonId: "addAmountStrings", inputId: "inputAmountStrings", listId: "amountSearchStrings" },
    { addButtonId: "addAmountAfterStrings", inputId: "inputAmountAfterStrings", listId: "amountInBankAfterSearchStrings" },
    { addButtonId: "addDateStrings", inputId: "inputDateStrings", listId: "dateSearchStrings" },
    { addButtonId: "addInterestRateStrings", inputId: "inputInterestRateStrings", listId: "interestRateSearchStrings" }
];
let transactionData = [];
let filteredTransactionData = [];
let transactionsHiddenToggle = false;
let selectedTransactionGroup = null;
let selectedCounterparty = null;
let selectedContract = null;
let headerContract = null;
let counterPartyData = [];
let filteredCounterPartyData = [];
let counterPartiesHiddenToggle = false;
let contractData = [];
let filteredContractData = [];
let contractsHiddenToggle = false;
//# sourceMappingURL=dataStorage.js.map