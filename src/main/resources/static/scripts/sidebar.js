async function loadSidebar() {
    try {
        const sidebar = document.querySelector('.sidebar');
        const sidebarToggle = document.querySelector('.sidebarToggle');
        const content = document.querySelector('.content');
        const topNav = document.getElementById('topNav');

        if (!sidebar || !sidebarToggle || !content || !topNav) {
            console.error("Required DOM elements for the sidebar are missing.");
            return;
        }

        initStaticLinks();

        await fetchLocalization("sidebar");

        const bankAccounts = await loadBankAccounts();

        bankAccounts.forEach(bankAccount => {
            addBankAccountToSidebar(bankAccount);
        });

        sidebarToggle.addEventListener('click', () => toggleSidebar(sidebar, content));
    } catch (error) {
        console.error("Error loading sidebar:", error);
    }
}

function toggleSidebar(sidebar, content) {
    sidebar?.classList.toggle("collapsed");
    content?.classList.toggle("fullScreen");

    const allSubItems = sidebar?.querySelectorAll('.navSubitem') ?? [];
    allSubItems.forEach(item => item.classList.toggle('collapsed'));
}

function addBankAccountToSidebar({name, id, interestRate}) {
    const topNav = document.getElementById('topNav');

    const isSavings = interestRate != null;

    const accountItem = createElement('li', 'navItem account', '', {id});
    const accountLink = createElement('a', 'navLink', '', {href: '/bankAccountOverview', 'data-ajax': 'true'});
    accountItem.appendChild(accountLink);

    accountLink.appendChild(createElement('span', isSavings ? 'bi bi-piggy-bank' : 'bi bi-bank'));
    accountLink.appendChild(createElement('span', 'navLabel', name));

    const toolTip = createElement('span', 'navTooltip', name);
    accountItem.appendChild(toolTip);

    const sublist = createSublist();
    accountItem.appendChild(sublist);
    topNav.appendChild(accountItem);

    accountLink.addEventListener('click', (event) => {
        event.preventDefault();
        toggleSublistVisibility(sublist, topNav);
    });
}

function createSublist() {
    const sublist = createElement('ul', 'navSublist hidden');

    const subItems = [
        {name: 'Overview', href: '/bankAccountOverview', icon: 'bi bi-border-style'},
        {name: 'Transactions', href: '/transactions', icon: 'bi bi-receipt'},
        {name: 'Categories', href: '/bankAccount/categories', icon: 'bi bi-tag-fill'},
        {name: 'Counterparties', href: '/bankAccount/counterparties', icon: 'bi bi-person-fill'},
        {name: 'Contracts', href: '/bankAccount/contracts', icon: 'bi bi-file-earmark-fill'}
    ];

    subItems.forEach(({name, href, icon}) => {
        const subItem = createElement('li', 'navSubitem');
        const subItemLink = createElement('a', 'navSubLink', '', {href, 'data-ajax': 'true'});

        subItemLink.appendChild(createElement('span', icon));
        subItemLink.appendChild(createElement('span', 'navLabel', name));

        subItem.appendChild(subItemLink);
        sublist.appendChild(subItem);
    });

    return sublist;
}

function toggleSublistVisibility(sublist, container) {
    const allSublist = container.querySelectorAll('.navSublist');
    allSublist.forEach(list => {
        if (list !== sublist) list.classList.add('hidden');
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