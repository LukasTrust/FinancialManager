let bankAccountId;

document.addEventListener('DOMContentLoaded', async () => {
    const contentArea = document.getElementById('content');  // Where the content will go

    await setLocale();

    // Load sidebar
    await loadSidebar();

    // Load fist page to view
    await buildAddBankAccount();

    // Listen for clicks on links (or buttons) to dynamically load content
    document.querySelectorAll('a[data-ajax="true"]').forEach(link => {
        link.addEventListener('click', async (event) => {
            event.preventDefault();  // Prevent the default link behavior

            const url = link.getAttribute('href');

            if (link.parentElement.id) {
                bankAccountId = link.parentElement.id;
            }

            try {
                // Fetch the content from the server (just the content fragment)
                const response = await fetch(url);
                const html = await response.text();

                // Extract and replace the content (assuming the fragment is inside a div with class "content")
                const parser = new DOMParser();
                const doc = parser.parseFromString(html, 'text/html');
                const newContent = doc.getElementById('content');

                // Replace the old content with the new one
                contentArea.innerHTML = newContent.innerHTML;

                console.log(url);
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
                }

            } catch (error) {
                console.error("Error loading content:", error);
            }
        });
    });
});

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