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
    users?: User;
    name: string;
    description?: string;
    maxSpendingPerMonth?: number;
    counterPartySearchStrings: string[];
}

interface Contract {
    id: number;
    name: string;
    description?: string;
    startDate: string; // LocalDate is represented as a string in JSON
    endDate?: string;
    monthsBetweenPayments: number;
    amount: number;
    lastPaymentDate: string;
    lastUpdatedAt: string;
    counterParty: CounterParty;
    users?: User;
}

interface Transaction {
    id: number;
    date: string; // LocalDate is represented as a string in JSON
    amount: number;
    amountInBankBefore: number;
    amountInBankAfter: number;
    originalCounterParty: string;
    isHidden: boolean;
    bankAccount: BankAccount;
    contract?: Contract;
    counterParty: CounterParty;
    category?: Category;
}

interface CounterParty {
    id: number;
    users?: User;
    name: string;
    description?: string;
    counterPartySearchStrings: string[];
}

interface CounterPartyDisplay {
    counterParty: CounterParty;
    contractCount: number;
    totalAmount: number;
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

class PointStyle {
    static readonly NORMAL = new PointStyle("#000000", "#FFFFFF", 2);
    static readonly GOOD = new PointStyle("#008000", "#00FF00", 3);
    static readonly BAD = new PointStyle("#FF0000", "#FFCCCC", 3);

    constructor(
        public pointBorderColor: string,
        public pointBackgroundColor: string,
        public pointBorderWidth: number
    ) {}
}

class DataPoint {
    constructor(
        public value: number,
        public date: Date,
        public info: string,
        public style: PointStyle
    ) {}
}

class ChartSeries {
    constructor(
        public name: string,
        public dataPoints: DataPoint[]
    ) {}
}

class ChartData {
    constructor(
        public title: string,
        public seriesList: ChartSeries[]
    ) {}
}

interface KeyFigure {
    name: string;
    tooltip: string;
    value: number;
}
