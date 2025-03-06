const contentArea: HTMLElement | null = document.getElementById('content');
let timer: number | null = null;
let alerts: HTMLElement[] = [];

let existingChart: any = null;
let currentLanguage: string;
let monthAbbreviations: string[] = [];

let bankAccountId: number;
let bankAccountSymbols: Record<string, string> = {};

let transactionData: Transaction[] = []
let filteredTransactionData: Transaction[] = [];
let transactionsHiddenToggle: boolean = false;

let selectedTransactionGroup: HTMLElement | null = null;
let selectedCounterparty: string | null = null;
let selectedContract: HTMLElement | null = null;

let counterPartyData: CounterPartyDisplay[] = [];
let filteredCounterPartyData: CounterPartyDisplay[] = [];
let counterPartiesHiddenToggle: boolean = false;