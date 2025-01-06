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

function clearIdInFocus() {
    let accountIdInFocus = localStorage.getItem('accountIdInFocus');
    accountIdInFocus = null;
    localStorage.setItem('accountIdInFocus', accountIdInFocus);
}

function addBankAccountToSidebar(accountName, accountId, isSavings) {
    let accountIdInFocus = localStorage.getItem('accountIdInFocus') || null;

    const sidebar = document.getElementById('topNav');

    // Create account item and link
    const accountItem = createElement('li', 'navItem account');
    accountItem.id = accountId;
    const accountLink = createElement('a', 'navLink', '', { href: `/bankAccountOverview` });

    accountLink.addEventListener("click", () => {
        accountIdInFocus = accountId;
        localStorage.setItem('accountIdInFocus', accountIdInFocus);
    });

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
    const sublist = createElement('ul', 'navSublist');
    if (accountId != accountIdInFocus) {
        sublist.classList.add('hidden');
    }
    else {
        console.log("Else.");
    }

    const subItems = [
        { name: 'Overview', href: `/bankAccountOverview`, icon: 'bi bi-border-style' },
        { name: 'Transactions', href: `/bankAccount/transactions`, icon: 'bi bi-receipt' },
        { name: 'Categories', href: `/bankAccount/categories`, icon: 'bi bi-tag-fill' },
        { name: 'Counterparties', href: `/bankAccount/counterparties`, icon: 'bi bi-person-fill' },
        { name: 'Contracts', href: `/bankAccount/contracts`, icon: 'bi bi-file-earmark-fill' }
    ];

    subItems.forEach(subItem => {
        const subItemElement = createElement('li', 'navSubitem');
        const subItemLink = createElement('a', 'navSublink', '', { href: subItem.href });

        accountLink.addEventListener("click", () => {
            accountIdInFocus = accountId;
            localStorage.setItem('accountIdInFocus', accountIdInFocus);
        });

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

    console.log(accountIdInFocus);
}