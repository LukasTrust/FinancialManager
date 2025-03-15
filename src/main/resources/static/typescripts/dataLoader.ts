document.addEventListener('DOMContentLoaded', async () => {
    await setLocale();

    // Load sidebar
    await loadSidebar();

    // Load first page to view
    await buildAddBankAccount();

    // Listen for clicks on links (or buttons) to dynamically load content
    document.querySelectorAll<HTMLAnchorElement>('a[data-ajax="true"]').forEach(link => {
        link.addEventListener('click', async (event: Event) => {
            event.preventDefault();  // Prevent the default link behavior

            const url = link.getAttribute('href');
            if (!url) return;

            if (link.parentElement?.id) {
                bankAccountId = Number(link.parentElement.id);
            }

            await loadURL(url);
        });
    });
});

async function loadURL(url: string): Promise<void> {
    try {
        // Fetch the content from the server (just the content fragment)
        const response = await fetch(url);
        const html = await response.text();

        // Extract and replace the content (assuming the fragment is inside a div with class "content")
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, 'text/html');
        const newContent = doc.getElementById('content');

        if (contentArea && newContent) {
            contentArea.innerHTML = newContent.innerHTML;
        }

        switch (url) {
            case "/addBankAccount":
                await buildAddBankAccount();
                break;
            case "/bankAccountOverview":
                await buildBankAccountOverview();
                break;
            case "/transactions":
                await buildTransactions();
                break;
            case "/counterParties":
                await buildCounterParties();
                break;
            case "/contracts":
                await buildContracts();
                break;
        }
    } catch (error) {
        console.error("Error loading content:", error);
    }
}

async function loadBankAccounts(): Promise<AnyBankAccount[]> {
    try {
        const response = await fetch('/getBankAccountsOfUser');
        if (response.ok) {
            return await response.json();
        } else {
            console.warn('Failed to load bank accounts');
        }
    } catch (error) {
        console.error("Error fetching bank accounts:", error);
    }
    return [];
}

async function loadData(type: Type, messages: Record<string, string>): Promise<void> {
    try {
        let url: string = `/${type.toString()}`;

        if (type !== Type.COUNTERPARTY)
            url += `/${bankAccountId}`;
        url += `/data`;

        const response = await fetch(url, {
            method: 'GET',
            headers: {'Content-Type': 'application/json'}
        });

        if (!response.ok) {
            await showAlertFromResponse(response);
            return;
        }

        const data = await response.json();
        contractsHiddenToggle = false;
        counterPartiesHiddenToggle = false;
        transactionsHiddenToggle = false;

        switch (type) {
            case Type.CONTRACT: {
                contractData = data;
                filteredContractData = data;
                break;
            }
            case Type.COUNTERPARTY: {
                counterPartyData = data;
                filteredCounterPartyData = data;
                break;
            }
            case Type.TRANSACTION: {
                transactionData = data;
                filteredTransactionData = data;
                break;
            }
        }
    } catch (error) {
        console.error("There was an error loading the data:", error);
        console.error("Type:", type);
        showAlert('error', messages["error_generic"]);
    }
}