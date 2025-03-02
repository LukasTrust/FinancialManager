const contentArea: HTMLElement | null = document.getElementById('content');
let timer: number | null = null;
let alerts: HTMLElement[] = [];
let existingChart: any = null; // Consider specifying a more precise type if possible
let bankAccountId: number;
let bankAccountSymbols: Record<string, string> = {};
let currentLanguage: string;
let monthAbbreviations: string[] = [];
let transactionData: any[] = []; // Define a proper type if available
let filteredTransactionData: any[] = []; // Define a proper type if available
let transactionsHiddenToggle: boolean = false;
let selectedTransactionGroup: HTMLElement | null = null;
let selectedCounterparty: string | null = null;
let selectedContract: HTMLElement | null = null;
