document.addEventListener('DOMContentLoaded', () => {
    const sidebar = document.querySelector('.sidebar');
    const sidebarToggle = document.querySelector('.sidebarToggle');

    sidebarToggle.addEventListener('click', () => {
        toggleSidebar(sidebar, sidebarToggle);
    })
});

function toggleSidebar(sidebar, sidebarToggle) {
    sidebar.classList.toggle("collapsed");
}