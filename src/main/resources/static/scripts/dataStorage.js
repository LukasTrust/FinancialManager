const contentArea = document.getElementById('content');
let timer;
let alerts = [];
let existingChart = null;
let bankAccountId;
let bankAccountSymbols= {}
let currentLanguage;
let monthAbbreviations;
let transactionData;
let filteredTransactionData;
let transactionsHiddenToggle;
let selectedTransactionGroup;
let selectedCounterparty;
let selectedContract;