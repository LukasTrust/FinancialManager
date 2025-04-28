interface User {
    id: number;
    firstName: string;
    lastName: string;
    email: string;
}

interface BankAccount {
    id: number;
    users?: User;
    name: string;
    description?: string;
    currencySymbol: string;
    amountSearchStrings: string[];
    dateSearchStrings: string[];
    counterPartySearchStrings: string[];
    amountInBankAfterSearchStrings: string[];
}

interface SavingsBankAccount extends BankAccount {
    interestRate: number;
    interestRateSearchStrings: string[];
}

type AnyBankAccount = BankAccount | SavingsBankAccount;

interface Category {
    id: number;
    users: User;
    name: string;
    description?: string;
    maxSpendingPerMonth?: number;
    counterParties: CounterParty[];
}

interface Contract {
    id: number;
    name: string;
    description?: string;
    startDate: string;
    endDate?: string;
    monthsBetweenPayments: number;
    amount: number;
    lastPaymentDate: string;
    lastUpdatedAt: string;
    counterParty: CounterParty;
    users?: User;
    hidden: boolean;
}

interface ContractDisplay {
    contract: Contract;
    contractHistories: ContractHistory[];
    transactionCount: number;
    totalAmount: number;
}

interface ContractHistory {
    id: number;
    contract: Contract;
    previousAmount: number;
    newAmount: number;
    changedAt: string
}

interface Transaction {
    id: number;
    date: string;
    amount: number;
    amountInBankBefore: number;
    amountInBankAfter: number;
    originalCounterParty: string;
    hidden: boolean;
    bankAccount: BankAccount;
    contract?: Contract;
    counterParty: CounterParty;
}

interface CounterParty {
    id: number;
    users?: User;
    name: string;
    description?: string;
    hidden: boolean;
    counterPartySearchStrings: string[];
}

interface CounterPartyDisplay {
    counterParty: CounterParty;
    transactionCount: number;
    contractCount: number;
    totalAmount: number;
}

interface ListElementObject {
    id: number,
    text: string,
    toolTip: string
}

enum Type {
    TRANSACTION = "transactions",
    COUNTERPARTY = "counterParties",
    CONTRACT = "contracts",
    CATEGORY = "categories",
}

enum DataTypeForSort {
    string = "string",
    number = "number",
    date = "date",
    input = "input"
}

enum AlertType {
    SUCCESS = "SUCCESS",
    WARNING = "WARNING",
    ERROR = "ERROR",
    INFO = "INFO"
}

interface Response<T = any> {
    alertType: AlertType;
    message: string;
    data: T;
}

interface ChartData {
    title: string,
    seriesList: ChartSeries[]
}

interface ChartSeries {
    name: string,
    dataPoints: DataPoint[]
}

interface DataPoint {
    value: number,
    amount: number,
    counterPartyName: string,
    info: string,
    date: Date,
    style: PointStyle
}

enum PointStyle {
    GOOD = "GOOD",
    NORMAL = "NORMAL",
    BAD = "BAD"
}

interface TooltipOptions {
    backgroundColor: string;
    titleFont: {
        family: string;
        size: number;
        weight: string;
    };
    bodyFont: {
        family: string;
        size: number;
    };
    padding: number;
    usePointStyle: boolean;
    callbacks: {
        label: (context: any) => string;
    };
}

interface KeyFigure {
    name: string;
    tooltip: string;
    value: number;
}

interface DropdownOptions {
    id: string;
    parent: HTMLElement;
    items: any[];
    preSelectedItems: any[];
    defaultText: string;
    clearText: string;
    multiSelect?: boolean;
}
