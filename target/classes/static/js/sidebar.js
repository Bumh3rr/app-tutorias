function toggleGroup(groupId, chevId) {
    const children = document.getElementById(groupId);
    const chev = document.getElementById(chevId);
    if (!children) return;
    const isOpen = children.classList.contains('open');
    children.classList.toggle('open', !isOpen);
    if (chev) chev.classList.toggle('open', !isOpen);
}

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const icon = document.getElementById('toggle-icon');
    const collapsed = sidebar.classList.toggle('collapsed');
    if (icon) {
        icon.innerHTML = collapsed
            ? '<polyline points="9 18 15 12 9 6"/>'
            : '<polyline points="15 18 9 12 15 6"/>';
    }
    localStorage.setItem('sidebarCollapsed', collapsed ? '1' : '0');
}

document.addEventListener('DOMContentLoaded', function () {
    // Restaurar estado del sidebar
    const collapsed = localStorage.getItem('sidebarCollapsed') === '1';
    const sidebar = document.getElementById('sidebar');
    const icon = document.getElementById('toggle-icon');
    if (sidebar && collapsed) {
        sidebar.classList.add('collapsed');
        if (icon) icon.innerHTML = '<polyline points="9 18 15 12 9 6"/>';
    }

    // Marcar ruta activa automáticamente
    const path = window.location.pathname;
    document.querySelectorAll('.nav-item[href], .nav-child[href]').forEach(link => {
        if (link.getAttribute('href') && path.startsWith(link.getAttribute('href')) && link.getAttribute('href') !== '/') {
            link.classList.add('active-route');
            // Abrir el grupo padre si es un nav-child
            const parent = link.closest('.nav-children');
            if (parent) {
                parent.classList.add('open');
                const chevId = parent.id.replace('g-', 'chev-');
                const chev = document.getElementById(chevId);
                if (chev) chev.classList.add('open');
            }
        }
    });
});