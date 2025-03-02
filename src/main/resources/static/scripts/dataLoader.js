document.addEventListener('DOMContentLoaded', async () => {
    await setLocale();
    // Load sidebar
    await loadSidebar();
    // Load first page to view
    await buildAddBankAccount();
    // Listen for clicks on links (or buttons) to dynamically load content
    document.querySelectorAll('a[data-ajax="true"]').forEach(link => {
        link.addEventListener('click', async (event) => {
            var _a;
            event.preventDefault(); // Prevent the default link behavior
            const url = link.getAttribute('href');
            if (!url)
                return;
            if ((_a = link.parentElement) === null || _a === void 0 ? void 0 : _a.id) {
                bankAccountId = Number(link.parentElement.id);
            }
            await loadURL(url);
        });
    });
});
async function loadURL(url) {
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
            case "/changeContract":
                break;
        }
    }
    catch (error) {
        console.error("Error loading content:", error);
    }
}
async function loadBankAccounts() {
    try {
        const response = await fetch('/getBankAccountsOfUser');
        if (response.ok) {
            return await response.json();
        }
        else {
            console.warn('Failed to load bank accounts');
        }
    }
    catch (error) {
        console.error("Error fetching bank accounts:", error);
    }
    return [];
}
//# sourceMappingURL=dataLoader.js.map