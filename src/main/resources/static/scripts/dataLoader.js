async function loadBankAccounts() {
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
}