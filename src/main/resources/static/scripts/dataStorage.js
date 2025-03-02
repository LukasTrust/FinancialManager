const contentArea = document.getElementById('content');
let timer = null;
let alerts = [];
let existingChart = null; // Consider specifying a more precise type if possible
let bankAccountId;
let bankAccountSymbols = {};
let currentLanguage;
let monthAbbreviations = [];
let transactionData = []; // Define a proper type if available
let filteredTransactionData = []; // Define a proper type if available
let transactionsHiddenToggle = false;
let selectedTransactionGroup = null;
let selectedCounterparty = null;
let selectedContract = null;
//# sourceMappingURL=dataStorage.js.map