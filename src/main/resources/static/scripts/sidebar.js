document.addEventListener('DOMContentLoaded', async () => {
    const sidebar = document.querySelector('.sidebar');
    const sidebarToggle = document.querySelector('.sidebarToggle');
    const content = document.querySelector('.content');

    // Load localization messages
    const userLocale = navigator.language || 'en';
    await fetchLocalization("general", userLocale);

    sidebarToggle.addEventListener('click', () => {
        toggleSidebar(sidebar, content);
    })
});

function toggleSidebar(sidebar, content) {
    sidebar.classList.toggle("collapsed");
    content.classList.toggle("fullScreen");
}