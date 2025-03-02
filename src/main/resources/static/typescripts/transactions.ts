async function buildTransactions(): Promise<void> {
    const messages = await fetchLocalization("transactions");
    if (!messages) return;

    monthAbbreviations = messages["monthAbbreviations"]
        .split("', '")
        .map((month: string) => month.replace(/'/g, ''));
    transactionsHiddenToggle = false;

    await loadTransactions(messages);
    splitDataIntoPages(messages, "transaction", transactionData);
    setUpSorting();

    document.getElementById("searchBarInput")?.addEventListener("input", () => searchTable(messages, "transaction"));

    document.getElementById("changeHiddenButton")?.addEventListener("click", () => showChangeHiddenDialog(messages));

    document.getElementById("changeContractButton")?.addEventListener("click", async () => {
        await buildChangeContract("/transactions", getCheckedTransactions());
    });

    document.getElementById("showHiddenRows")?.addEventListener("change", () => changeRowVisibility());
}

async function loadTransactions(messages: Record<string, string>): Promise<void> {
    try {
        const response = await fetch(`/transactions/${bankAccountId}/data`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            const responseBody = await response.json();
            showAlert(responseBody.alertType, responseBody.message);
            showAlert("ERROR", messages["error_loadingTransactions"]);
            return;
        }

        transactionData = await response.json();
        filteredTransactionData = transactionData;
    } catch (error) {
        console.error("There was an error loading the transactions:", error);
        showAlert('error', messages["error_generic"]);
    }
}

function addRowsToTransactionTable(data: Transaction[], messages: Record<string, string>): void {
    try {
        const tableBody = getCurrentTableBody();
        if (!tableBody) return;
        const currency = getCurrentCurrencySymbol();

        data.forEach((transaction, index) => {
            if (!transaction || typeof transaction !== "object") {
                console.warn(`Warning: Skipping invalid transaction at index ${index}.`, transaction);
                return;
            }

            let rowClass: string | null = transaction.isHidden ? "hiddenRow" : null;
            if (rowClass && !transactionsHiddenToggle) {
                rowClass += " hidden";
            }

            const newRow = createAndAppendElement(tableBody, "tr", rowClass, null, { id: transaction.id.toString() });
            const trCheckBox = createAndAppendElement(newRow, "td", null, "", { style: "width: 5%" });
            const checkBox = createAndAppendElement(trCheckBox, "input", "tableCheckbox", "", {
                type: "checkbox",
                id: transaction.id.toString(),
                style: "margin-left: 10px;",
            }) as HTMLInputElement;

            if (rowClass) {
                createAndAppendElement(trCheckBox, "span", "bi bi-eye-slash");
            }

            checkBox.addEventListener("change", () => updateRowStyle(newRow, checkBox));

            newRow.addEventListener("click", (event) => {
                if ((event.target as HTMLInputElement).type === "checkbox") return;
                checkBox.checked = !checkBox.checked;
                updateRowStyle(newRow, checkBox);
            });

            createAndAppendElement(newRow, "td", "", transaction.counterParty?.name || "", { style: "width: 25%" });
            createAndAppendElement(newRow, "td", "", transaction.contract?.name || "", { style: "width: 15%" });
            createAndAppendElement(newRow, "td", "", transaction.category?.name || "", { style: "width: 15%" });
            createAndAppendElement(newRow, "td", "rightAligned", formatDateString(transaction.date), { style: "width: 10%" });
            createAndAppendElement(newRow, "td", "rightAligned", formatNumber(transaction.amountInBankBefore, currency), { style: "width: 10%" });
            createAndAppendElement(newRow, "td", "rightAligned", formatNumber(transaction.amount, currency), { style: "width: 10%" });
            createAndAppendElement(newRow, "td", "rightAligned", formatNumber(transaction.amountInBankAfter, currency), { style: "width: 10%" });
        });
    } catch (error) {
        console.error("Unexpected error in addRowsToTransactionTable:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}

function updateRowStyle(newRow: HTMLElement, checkBox: HTMLInputElement): void {
    newRow.classList.toggle("selectedRow", checkBox.checked);
}

function filterTransactions(messages: Record<string, string>, searchString: string): void {
    try {
        filteredTransactionData = transactionData.filter(transaction =>
            transaction.counterParty?.name?.toLowerCase().includes(searchString) ||
            transaction.contract?.name?.toLowerCase().includes(searchString) ||
            transaction.category?.name?.toLowerCase().includes(searchString) ||
            transaction.date?.toLowerCase().includes(searchString) ||
            transaction.amountInBankBefore?.toString().toLowerCase().includes(searchString) ||
            transaction.amount?.toString().toLowerCase().includes(searchString) ||
            transaction.amountInBankAfter?.toString().toLowerCase().includes(searchString)
        );

        splitDataIntoPages(messages, "transaction", filteredTransactionData);
    } catch (error) {
        console.error("Unexpected error in filterTransactions:", error);
        showAlert("ERROR", messages["error_generic"]);
    }
}

function getCheckedTransactions(): Transaction[] {
    const checkedRows = new Set(getCheckedRows());
    return filteredTransactionData.filter(transaction => checkedRows.has(transaction.id));
}

function changeRowVisibility(): void {
    const currentTableBody = getCurrentTableBody();
    if (!currentTableBody) return;
    const rows = Array.from(currentTableBody.querySelectorAll("tr.hiddenRow"));
    transactionsHiddenToggle = !transactionsHiddenToggle;
    rows.forEach(row => row.classList.toggle("hidden"));
}

function showChangeHiddenDialog(messages: Record<string, string>): void {
    const { alreadyHidden, notHidden } = classifyHiddenTransactions();

    const dialogContent = createDialogContent(messages["changeHiddenHeader"], "bi bi-eye");
    const listContainer = createAndAppendElement(dialogContent, "div", "flexContainerSpaced");

    // Hidden transactions
    const leftSide = createListSection(listContainer, messages["alreadyHiddenHeader"], alreadyHidden);
    createDialogButton(leftSide, "bi bi-eye", messages["unHide"], "left", () =>
        updateTransactionVisibility(messages, dialogContent, leftSide, false)
    );

    // Not hidden transactions
    const rightSide = createListSection(listContainer, messages["notHiddenHeader"], notHidden);
    createDialogButton(rightSide, "bi bi-eye-slash", messages["hide"], "right", () =>
        updateTransactionVisibility(messages, dialogContent, rightSide, true)
    );
}

function classifyHiddenTransactions(): { alreadyHidden: Transaction[]; notHidden: Transaction[] } {
    const alreadyHidden: Transaction[] = [];
    const notHidden: Transaction[] = [];

    const transactions: Transaction[] = getCheckedTransactions();

    transactions.forEach(transaction => {
        transaction.isHidden ? alreadyHidden.push(transaction) : notHidden.push(transaction);
    });

    return { alreadyHidden, notHidden };
}

async function updateTransactionVisibility(
    messages: Record<string, string>,
    model: HTMLElement,
    listContainer: HTMLElement,
    hide: boolean
): Promise<void> {
    try {
        const transactionIds: number[] = Array.from(listContainer.querySelectorAll("div span"))
            .map(span => Number((span as HTMLElement).id)) // Ensure type assertion for HTMLElement
            .filter(id => id !== 0); // Remove 0 if present

        if (transactionIds.length === 0) {
            showAlert("INFO", messages["noTransactionsUpdated"], model);
            return;
        }

        const endpoint = hide ? "hideTransactions" : "unHideTransactions";
        const response = await fetch(`/transactions/${bankAccountId}/data/${endpoint}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(transactionIds),
        });

        const responseBody = await response.json();

        showAlert(responseBody.alertType, responseBody.message, model);

        updateCachedTransactionsAndUI(messages, transactionIds);
    } catch (error) {
        console.error("Unexpected error in updateTransactionVisibility:", error);
        showAlert("ERROR", messages["error_generic"], model);
    }
}

function updateCachedTransactionsAndUI(messages: Record<string, string>, transactionIds: number[]): void {
    filteredTransactionData.forEach((transaction: Transaction) => {
        if (transactionIds.includes(transaction.id)) {
            transaction.isHidden = !transaction.isHidden;
        }
    });

    splitDataIntoPages(messages, "transaction", filteredTransactionData);
}