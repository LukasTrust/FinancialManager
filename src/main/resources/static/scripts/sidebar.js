async function loadSidebar() {
    try {
        const sidebar = document.querySelector('.sidebar');
        const sidebarToggle = document.querySelector('.sidebarToggle');
        const topNav = document.getElementById('topNav');
        if (!sidebar || !sidebarToggle || !topNav) {
            console.error("Required DOM elements for the sidebar are missing.");
            return;
        }
        initStaticLinks();
        const messages = await loadLocalization("sidebar");
        if (!messages)
            return;
        const localBankAccounts = await loadBankAccounts();
        localBankAccounts.forEach((bankAccount) => {
            bankAccounts[bankAccount.id] = bankAccount;
            addBankAccountToSidebar(messages, bankAccount);
        });
        sidebarToggle.addEventListener('click', () => toggleSidebar(sidebar));
    }
    catch (error) {
        console.error("Error loading sidebar:", error);
    }
}
function toggleSidebar(sidebar) {
    sidebar.classList.toggle("collapsed");
    const allSubItems = sidebar.querySelectorAll('.navSubitem');
    allSubItems.forEach(item => item.classList.toggle('collapsed'));
    const mainContainer = document.getElementById("content");
    if (sidebar.classList.contains("collapsed")) {
        mainContainer.style.width = "calc(100% - 120px)";
    }
    else {
        mainContainer.style.width = "calc(100% - 300px)";
    }
}
function addBankAccountToSidebar(messages, bankAccount) {
    const { id, name } = bankAccount;
    const isSavings = "interestRate" in bankAccount && bankAccount.interestRate !== undefined;
    const topNav = document.getElementById('topNav');
    if (!topNav)
        return;
    const accountItem = createAndAppendElement(topNav, 'li', 'navItem account', '', { id: id.toString() });
    const accountLink = createAndAppendElement(accountItem, 'a', 'navLink', '', {
        href: '/bankAccountOverview',
        'data-ajax': 'true',
    });
    createAndAppendElement(accountLink, 'span', isSavings ? 'bi bi-piggy-bank' : 'bi bi-bank');
    createAndAppendElement(accountLink, 'span', 'navLabel', name);
    createAndAppendElement(accountItem, 'span', 'navTooltip', name);
    const sublist = createSublist(messages, accountItem);
    accountLink.addEventListener('click', (event) => {
        event.preventDefault();
        toggleSublistVisibility(sublist, topNav);
    });
}
function createSublist(messages, parent) {
    const sublist = createAndAppendElement(parent, 'ul', 'navSublist hidden');
    const subItems = [
        { name: messages["dashboard"], href: '/bankAccountOverview', icon: 'bi bi-border-style' },
        { name: messages["transactions"], href: '/transactions', icon: 'bi bi-receipt' },
        { name: messages["categories"], href: '/categories', icon: 'bi bi-tag-fill' },
        { name: messages["contracts"], href: '/contracts', icon: 'bi bi-file-earmark-fill' }
    ];
    subItems.forEach(({ name, href, icon }) => {
        const subItem = createAndAppendElement(sublist, 'li', 'navSubitem');
        const subItemLink = createAndAppendElement(subItem, 'a', 'navSubLink', '', { href, 'data-ajax': 'true' });
        createAndAppendElement(subItemLink, 'span', icon);
        createAndAppendElement(subItemLink, 'span', 'navLabel', name);
    });
    return sublist;
}
function toggleSublistVisibility(sublist, container) {
    const allSublist = container.querySelectorAll('.navSublist');
    allSublist.forEach(list => {
        if (list !== sublist)
            list.classList.add('hidden');
    });
    sublist.classList.toggle('hidden');
}
function initStaticLinks() {
    const staticLinks = document.querySelectorAll('.navLink[data-ajax="true"]');
    staticLinks.forEach(link => {
        link.addEventListener('click', () => {
            document.querySelectorAll('.navSublist').forEach(list => list.classList.add('hidden'));
        });
    });
}
//# sourceMappingURL=sidebar.js.map