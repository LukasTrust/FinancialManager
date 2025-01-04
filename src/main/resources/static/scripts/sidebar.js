document.addEventListener('DOMContentLoaded', () => {
    const sidebar = document.querySelector('.sidebar');
    const sidebarToggle = document.querySelector('.sidebarToggle');
    const mainContent = document.querySelector('.mainContent');

    sidebarToggle.addEventListener('click', () => {
        toggleSidebar(sidebar, mainContent);
    })
});

function toggleSidebar(sidebar, mainContent) {
    sidebar.classList.toggle("collapsed");
    mainContent.classList.toggle("fullScreen");
}