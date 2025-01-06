document.addEventListener('DOMContentLoaded', async () => {
    const sidebar = document.querySelector('.sidebar');
    const sidebarToggle = document.querySelector('.sidebarToggle');
    const content = document.querySelector('.content');

    // Load localization messages
    const userLocale = navigator.language || 'en';
    await fetchLocalization("general", userLocale);
    const bankAccounts = await loadBankAccounts();

    for (const bankAccount of bankAccounts) {
        addBankAccountToSidebar(bankAccount.name, bankAccount.id, bankAccount.interestRate != null);
    }

    sidebarToggle.addEventListener('click', () => {
        toggleSidebar(sidebar, content);
    })
});

function toggleSidebar(sidebar, content) {
    sidebar.classList.toggle("collapsed");
    content.classList.toggle("fullScreen");
}

function createElement(type, className, textContent = '', attributes = {}) {
    const element = document.createElement(type);
    element.className = className;
    if (textContent) element.textContent = textContent;
    for (const [key, value] of Object.entries(attributes)) {
        element.setAttribute(key, value);
    }
    return element;
}

function addBankAccountToSidebar(accountName, accountId, isSavings) {
    const sidebar = document.getElementById('topNav');

    // Create account item and link
    const accountItem = createElement('li', 'navItem account');
    const accountLink = createElement('a', 'navLink', '', { href: `/bankAccount/${accountId}` });
    accountItem.appendChild(accountLink);

    // Add account icon
    const icon = createElement('span', isSavings ? 'bi bi-piggy-bank' : 'bi bi-bank');
    accountLink.appendChild(icon);

    // Add account name label
    const name = createElement('span', 'navLable', accountName);
    accountLink.appendChild(name);

    // Create tooltip span (optional)
    const toolTip = createElement('span', 'navTooltip');
    accountItem.appendChild(toolTip);

    // Create sublist
    const sublist = createElement('ul', 'navSublist hidden');
    const subItems = [
        { name: 'Overview', href: `/bankAccount/${accountId}/overview`, icon: 'bi bi-border-style' },
        { name: 'Transactions', href: `/bankAccount/${accountId}/transactions`, icon: 'bi bi-receipt' },
        { name: 'Categories', href: `/bankAccount/${accountId}/categories`, icon: 'bi bi-tag-fill' },
        { name: 'Counterparties', href: `/bankAccount/${accountId}/counterparties`, icon: 'bi bi-person-fill' },
        { name: 'Contracts', href: `/bankAccount/${accountId}/contracts`, icon: 'bi bi-file-earmark-fill' }
    ];

    subItems.forEach(subItem => {
        const subItemElement = createElement('li', 'navSubitem');
        const subItemLink = createElement('a', 'navSublink', '', { href: subItem.href });

        // Add sub-item icon
        const iconSubItem = createElement('span', subItem.icon);
        subItemLink.appendChild(iconSubItem);

        // Add sub-item name label
        const nameSubItem = createElement('span', 'navLable', subItem.name);
        subItemLink.appendChild(nameSubItem);

        subItemElement.appendChild(subItemLink);
        sublist.appendChild(subItemElement);
    });

    accountItem.appendChild(sublist);
    sidebar.appendChild(accountItem);

    // Use event delegation to handle sublist toggling
    sidebar.addEventListener('click', (e) => {
        if (e.target.closest('.navLink')) {
            const accountLink = e.target.closest('.navLink');
            const sublist = accountLink.nextElementSibling;
            if (sublist && sublist.classList.contains('navSublist')) {
                e.preventDefault(); // Prevent navigation
                sublist.classList.toggle('hidden');
                sublist.classList.toggle('beforeHidden');
            }
        }
    });
}