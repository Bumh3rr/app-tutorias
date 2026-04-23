function cambiarPageSize(pageSize) {
    const url = new URL(window.location.href);
    url.searchParams.set('pageSize', pageSize);
    url.searchParams.set('page', '0');
    window.location.href = url.toString();
}