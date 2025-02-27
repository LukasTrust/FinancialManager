const contentArea = document.getElementById('content');
let alerts = [];
let existingChart = null;
let bankAccountId;
let bankAccountSymbols= {}
let currentLanguage;
let monthAbbreviations;
let transactionData;
let filteredTransactionData;
let transactionsHiddenToggle;
let contractData;
let selectedTransactionGroup;
let selectedCounterparty;
let selectedContract;