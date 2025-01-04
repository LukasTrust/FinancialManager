document.addEventListener('DOMContentLoaded', async () => {
    const sidebar = document.querySelector('.sidebar');
    const sidebarToggle = document.querySelector('.sidebarToggle');
    const mainContent = document.querySelector('.mainContent');

    // Load localization messages
    const userLocale = navigator.language || 'en';
    await fetchLocalization("general", userLocale);

    sidebarToggle.addEventListener('click', () => {
        toggleSidebar(sidebar, mainContent);
    })
});

function toggleSidebar(sidebar, mainContent) {
    sidebar.classList.toggle("collapsed");
    mainContent.classList.toggle("fullScreen");
}